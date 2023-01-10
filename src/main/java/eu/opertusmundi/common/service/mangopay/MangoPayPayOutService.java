package eu.opertusmundi.common.service.mangopay;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.mangopay.core.Money;
import com.mangopay.core.enumerations.CurrencyIso;
import com.mangopay.core.enumerations.PayOutPaymentType;
import com.mangopay.core.enumerations.PayoutMode;
import com.mangopay.core.enumerations.TransactionType;
import com.mangopay.entities.PayOut;
import com.mangopay.entities.subentities.PayOutPaymentDetailsBankWire;

import eu.opertusmundi.common.domain.AccountEntity;
import eu.opertusmundi.common.domain.CustomerProfessionalEntity;
import eu.opertusmundi.common.domain.PayOutEntity;
import eu.opertusmundi.common.model.EnumSortingOrder;
import eu.opertusmundi.common.model.PageResultDto;
import eu.opertusmundi.common.model.account.EnumCustomerType;
import eu.opertusmundi.common.model.payment.EnumPayOutSortField;
import eu.opertusmundi.common.model.payment.EnumTransactionStatus;
import eu.opertusmundi.common.model.payment.PayOutCommandDto;
import eu.opertusmundi.common.model.payment.PayOutDto;
import eu.opertusmundi.common.model.payment.PayOutStatusUpdateCommand;
import eu.opertusmundi.common.model.payment.PaymentException;
import eu.opertusmundi.common.model.payment.PaymentMessageCode;
import eu.opertusmundi.common.repository.AccountRepository;
import eu.opertusmundi.common.repository.PayOutRepository;

@Service
@Transactional
public class MangoPayPayOutService extends BaseMangoPayService implements PayOutService {

    private static final Logger logger = LoggerFactory.getLogger(MangoPayPayOutService.class);

    private final PayOutRepository      payOutRepository;
    private final PayOutWorkflowService payOutWorkflowService;
    private final WalletService         walletService;

    @Autowired
    public MangoPayPayOutService(
        AccountRepository       accountRepository,
        PayOutRepository        payOutRepository,
        PayOutWorkflowService   payOutWorkflowService,
        WalletService           walletService
    ) {
        super(accountRepository);

        this.payOutRepository      = payOutRepository;
        this.payOutWorkflowService = payOutWorkflowService;
        this.walletService         = walletService;
    }

    @Override
    public PayOutDto createPayOutAtOpertusMundi(PayOutCommandDto command) throws PaymentException {
        try {
            // Account with provider role must exist
            final AccountEntity              account  = this.getAccount(command.getProviderKey());
            final CustomerProfessionalEntity provider = account.getProfile().getProvider();

            if (provider == null) {
                throw new PaymentException(
                    PaymentMessageCode.SERVER_ERROR,
                    String.format("[MANGOPAY] Provider was not found for account [key=%s]", command.getProviderKey())
                );
            }

            // No pending PayOut records must exist
            final long pending = this.payOutRepository.countProviderPendingPayOuts(command.getProviderKey());
            if (pending != 0) {
                throw new PaymentException(
                    PaymentMessageCode.VALIDATION_ERROR,
                    "Pending PayOut has been found. Wait until the current operation is completed"
                );
            }

            // Refresh provider's wallet from the payment provider
            this.walletService.refreshUserWallets(command.getProviderKey(), EnumCustomerType.PROVIDER);

            // Funds must exist
            if (provider.getWalletFunds().compareTo(command.getDebitedFunds()) < 0) {
                throw new PaymentException(PaymentMessageCode.VALIDATION_ERROR, "Not enough funds. Check wallet balance");
            }
            // Fees are applied in Transfers.
            command.setFees(BigDecimal.ZERO);
            // Set bank account
            command.setBankAccount(provider.getBankAccount().clone());

            // Update provider pending PayOut funds
            provider.setPendingPayoutFunds(provider.getPendingPayoutFunds().add(command.getDebitedFunds()));
            provider.setPendingPayoutFundsUpdatedOn(ZonedDateTime.now());
            this.accountRepository.saveAndFlush(account);

            final PayOutDto payout = this.payOutRepository.createPayOut(command);

            // Start PayOut workflow instance
            this.payOutWorkflowService.start(command.getAdminUserKey(), payout.getKey());

            return payout;
        } catch (final Exception ex) {
            throw this.wrapException("Create PayOut", ex, command);
        }
    }

    @Override
    @Retryable(include = {PaymentException.class}, maxAttempts = 3, backoff = @Backoff(delay = 2000, maxDelay = 3000))
    public PayOutDto createPayOutAtProvider(UUID payOutKey) throws PaymentException {
        try {
            final PayOutEntity payOut = this.payOutRepository.findOneEntityByKey(payOutKey).orElse(null);

            if (payOut == null) {
                throw new PaymentException(
                    PaymentMessageCode.SERVER_ERROR,
                    String.format("[MANGOPAY] PayOut was not found [key=%s]", payOutKey)
                );
            }

            final String idempotencyKey = payOut.getKey().toString();

            // Check if this is a retry operation
            PayOut payoutResponse = this.<PayOut>getResponse(idempotencyKey);

            // Create a new PayPout if needed
            if (payoutResponse == null) {
                final PayOut payOutRequest = this.createPayOut(payOut);

                payoutResponse = this.api.getPayOutApi().create(idempotencyKey, payOutRequest);
            }

            final PayOutStatusUpdateCommand update = PayOutStatusUpdateCommand.builder()
                .createdOn(this.timestampToDate(payoutResponse.getCreationDate()))
                .executedOn(this.timestampToDate(payoutResponse.getExecutionDate()))
                .key(payOutKey)
                .providerPayOutId(payoutResponse.getId())
                .resultCode(payoutResponse.getResultCode())
                .resultMessage(payoutResponse.getResultMessage())
                .status(EnumTransactionStatus.from(payoutResponse.getStatus()))
                .build();

            this.payOutRepository.updatePayOutStatus(update);

            // Pending funds will be updated once the web hook event is received

            return payOut.toDto(true);
        } catch (final Exception ex) {
            throw this.wrapException("Create MANGOPAY PayOut", ex, payOutKey);
        }
    }

    @Override
    public void sendPayOutStatusUpdateMessage(String payOutId) throws PaymentException {
        try {
            final PayOutEntity payOutEntity = this.ensurePayOut(payOutId);

            final PayOut payOutObject = this.api.getPayOutApi().get(payOutId);

            // Update workflow instance only if status has been modified
            if (payOutEntity.getStatus() != EnumTransactionStatus.from(payOutObject.getStatus())) {
                this.payOutWorkflowService.sendStatusUpdateMessage(
                    payOutEntity.getKey(),
                    EnumTransactionStatus.from(payOutObject.getStatus())
                );
            }
        } catch (final Exception ex) {
            throw this.wrapException("Update PayIn", ex, payOutId);
        }
    }

    @Override
    public PayOutDto updatePayOut(UUID payOutKey, String payOutId) throws PaymentException {
        try {
            // Ensure that the PayOut record exists in our database
            final PayOutEntity payOutEntity = this.ensurePayOut(payOutId);

            Assert.isTrue(payOutKey.equals(payOutEntity.getKey()), String.format(
                "Expected PayOut entity key to match parameter payOutKey [key=%s, payOutKey=%s]" ,
                payOutEntity.getKey(), payOutKey
            ));

            // Fetch PayIn object from the Payment Provider (MANGOPAY)
            final PayOut payOutObject = this.api.getPayOutApi().get(payOutId);

            // Update PayIn local instance only
            final PayOutStatusUpdateCommand command = PayOutStatusUpdateCommand.builder()
                .createdOn(this.timestampToDate(payOutObject.getCreationDate()))
                .executedOn(this.timestampToDate(payOutObject.getExecutionDate()))
                .key(payOutEntity.getKey())
                .providerPayOutId(payOutObject.getId())
                .resultCode(payOutObject.getResultCode())
                .resultMessage(payOutObject.getResultMessage())
                .status(EnumTransactionStatus.from(payOutObject.getStatus()))
                .build();

            final PayOutDto result = this.payOutRepository.updatePayOutStatus(command);

            // Update provider pending PayOut funds
            final AccountEntity              account  = payOutEntity.getProvider();
            final CustomerProfessionalEntity provider = account.getProfile().getProvider();

            provider.setPendingPayoutFunds(provider.getPendingPayoutFunds().subtract(payOutEntity.getDebitedFunds()));
            provider.setPendingPayoutFundsUpdatedOn(ZonedDateTime.now());

            this.accountRepository.saveAndFlush(account);

            // Update provider wallet
            this.walletService.refreshUserWallets(payOutEntity.getProvider().getKey(), EnumCustomerType.PROVIDER);

            return result;
        } catch (final Exception ex) {
            throw this.wrapException("Update PayIn", ex, payOutId);
        }
    }

    @Override
    public PayOutDto getProviderPayOut(Integer userId, UUID payOutKey) {
        final PayOutEntity payOut = this.payOutRepository.findOneByAccountIdAndKey(userId, payOutKey).orElse(null);

        return payOut == null ? null : payOut.toDto(false);
    }

    @Override
    public PageResultDto<PayOutDto> findAllProviderPayOuts(
        UUID userKey, EnumTransactionStatus status, int pageIndex, int pageSize, EnumPayOutSortField orderBy, EnumSortingOrder order
    ) {
        final Direction direction = order == EnumSortingOrder.DESC ? Direction.DESC : Direction.ASC;

        final PageRequest        pageRequest = PageRequest.of(pageIndex, pageSize, Sort.by(direction, orderBy.getValue()));
        final Page<PayOutEntity> page        = this.payOutRepository.findAllProviderPayOuts(userKey, status, pageRequest);

        final long           count   = page.getTotalElements();
        final List<PayOutDto> records = page.getContent().stream()
            .map(p -> p.toDto(false))
            .collect(Collectors.toList());

        return PageResultDto.of(pageIndex, pageSize, records, count);
    }


    private PayOut createPayOut(PayOutEntity payOut) {
        final CustomerProfessionalEntity customer = payOut.getProvider().getProvider();

        Assert.notNull(customer, "Expected a non-null provider");

        final String userId        = customer.getPaymentProviderUser();
        final String walletId      = customer.getPaymentProviderWallet();
        final String bankAccountId = payOut.getBankAccount().getId();

        Assert.hasText(userId, "Expected a non-empty provider user id");
        Assert.hasText(walletId, "Expected a non-empty provider wallet id");
        Assert.hasText(bankAccountId, "Expected a non-empty provider bank account id");

        final PayOutPaymentDetailsBankWire details = new PayOutPaymentDetailsBankWire();
        details.setBankAccountId(bankAccountId);
        details.setBankWireRef(payOut.getBankwireRef());
        details.setPayoutModeRequested(PayoutMode.STANDARD);

        final PayOut result = new PayOut();
        result.setAuthorId(userId);
        result.setDebitedFunds(new Money(CurrencyIso.EUR, payOut.getDebitedFunds().multiply(BigDecimal.valueOf(100L)).intValue()));
        result.setDebitedWalletId(walletId);
        result.setFees(new Money(CurrencyIso.EUR, payOut.getPlatformFees().multiply(BigDecimal.valueOf(100L)).intValue()));
        result.setMeanOfPaymentDetails(details);
        result.setPaymentType(PayOutPaymentType.BANK_WIRE);
        result.setTag(payOut.getKey().toString());
        result.setType(TransactionType.PAYOUT);

        return result;
    }

    private PayOutEntity ensurePayOut(String providerPayOutId) {
        final PayOutEntity payOutEntity = this.payOutRepository.findOneByTransactionIdForUpdate(providerPayOutId).orElse(null);

        if(payOutEntity == null) {
            throw new PaymentException(
                PaymentMessageCode.RESOURCE_NOT_FOUND,
                String.format("[OpertusMundi] PayOut [%s] was not found", providerPayOutId)
            );
        }

        return payOutEntity;
    }

    private PaymentException wrapException(String operation, Exception ex, Object command) {
        return super.wrapException(operation, ex, command, logger);
    }

}
