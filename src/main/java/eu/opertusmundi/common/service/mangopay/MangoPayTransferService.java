package eu.opertusmundi.common.service.mangopay;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.mangopay.core.Money;
import com.mangopay.core.enumerations.CurrencyIso;
import com.mangopay.entities.Transfer;

import eu.opertusmundi.common.domain.AccountEntity;
import eu.opertusmundi.common.domain.CustomerEntity;
import eu.opertusmundi.common.domain.PayInEntity;
import eu.opertusmundi.common.domain.PayInItemEntity;
import eu.opertusmundi.common.domain.PayInOrderItemEntity;
import eu.opertusmundi.common.domain.PayInServiceBillingItemEntity;
import eu.opertusmundi.common.model.EnumSetting;
import eu.opertusmundi.common.model.account.EnumCustomerType;
import eu.opertusmundi.common.model.order.EnumOrderStatus;
import eu.opertusmundi.common.model.payment.EnumTransactionStatus;
import eu.opertusmundi.common.model.payment.PaymentException;
import eu.opertusmundi.common.model.payment.PaymentMessageCode;
import eu.opertusmundi.common.model.payment.TransferDto;
import eu.opertusmundi.common.repository.AccountRepository;
import eu.opertusmundi.common.repository.PayInItemHistoryRepository;
import eu.opertusmundi.common.repository.PayInRepository;
import eu.opertusmundi.common.repository.ServiceBillingRepository;
import eu.opertusmundi.common.repository.SettingRepository;

@Service
@Transactional
public class MangoPayTransferService extends BaseMangoPayService implements TransferService {

    private static final Logger logger = LoggerFactory.getLogger(MangoPayTransferService.class);

    @Autowired
    private final PayInRepository            payInRepository;
    private final PayInItemHistoryRepository payInItemHistoryRepository;
    private final ServiceBillingRepository   serviceBillingRepository;
    private final SettingRepository          settingRepository;
    private final WalletService              walletService;

    @Autowired
    public MangoPayTransferService(
        AccountRepository             accountRepository,
        PayInRepository               payInRepository,
        PayInItemHistoryRepository    payInItemHistoryRepository,
        ServiceBillingRepository      serviceBillingRepository,
        SettingRepository             settingRepository,
        WalletService                 walletService
    ) {
        super(accountRepository);

        this.payInRepository            = payInRepository;
        this.payInItemHistoryRepository = payInItemHistoryRepository;
        this.serviceBillingRepository   = serviceBillingRepository;
        this.settingRepository          = settingRepository;
        this.walletService              = walletService;
    }

    @Override
    public List<TransferDto> createTransfer(UUID userKey, UUID payInKey) throws PaymentException {
        try {
            final var topioAccountSetting = this.settingRepository.findOne(EnumSetting.TOPIO_ACCOUNT_ID);
            final var feePercentSetting   = this.settingRepository.findOne(EnumSetting.TOPIO_FEE_PERCENT);
            final var topioAccountId      = Integer.parseInt(topioAccountSetting.getValue());
            final var feePercent          = BigDecimal.valueOf(Long.parseLong(feePercentSetting.getValue()));
            final var transfers           = new ArrayList<TransferDto>();

            // PayIn must exist with a transaction status SUCCEEDED
            final PayInEntity payIn = this.payInRepository.findOneEntityByKey(payInKey).orElse(null);

            if (payIn == null) {
                throw new PaymentException(
                    PaymentMessageCode.SERVER_ERROR,
                    String.format("[MANGOPAY] PayIn was not found [key=%s]", payInKey)
                );
            }
            if (payIn.getStatus() != EnumTransactionStatus.SUCCEEDED) {
                throw new PaymentException(PaymentMessageCode.SERVER_ERROR, String.format(
                    "[MANGOPAY] PayIn invalid status [key=%s, status=%s, expected=%s]",
                    payInKey, payIn.getStatus(), EnumTransactionStatus.SUCCEEDED
                ));
            }

            // Get debit customer
            final AccountEntity  debitAccount   = payIn.getConsumer();
            final CustomerEntity debitCustomer  = debitAccount.getProfile().getConsumer();

            if (debitCustomer == null) {
                throw new PaymentException(PaymentMessageCode.SERVER_ERROR, String.format(
                    "[MANGOPAY] Debit customer for PayIn was not found [key=%s]",
                    payInKey
                ));
            }

            // Process every item
            for (final PayInItemEntity item : payIn.getItems()) {
                if (!StringUtils.isBlank(item.getTransfer())) {
                    // If a valid transfer transaction identifier exists, this
                    // is a retry operation
                    transfers.add(item.toTransferDto(true));
                    continue;
                }
                final String idempotencyKey = item.getTransferKey().toString();
                TransferDto  transfer       = null;
                switch (item.getType()) {
                    case ORDER :
                        transfer = this.createTransferForOrder(
                            idempotencyKey, payInKey, (PayInOrderItemEntity) item, debitCustomer, feePercent
                        );
                        break;
                    case SERVICE_BILLING :
                        transfer = this.createTransferForServiceBilling(
                            idempotencyKey, payInKey, (PayInServiceBillingItemEntity) item, debitCustomer,
                            feePercent, topioAccountId
                        );
                        break;
                    default :
                        throw new PaymentException(PaymentMessageCode.SERVER_ERROR, String.format(
                            "[MANGOPAY] PayIn item type not supported [key=%s, index=%d, type=%s]",
                            payInKey, item.getId(), item.getType()
                        ));
                }

                if (transfer != null) {
                    // Update item
                    item.updateTransfer(transfer);

                    // If transfer is successful, update item history record
                    if (transfer.getStatus() == EnumTransactionStatus.SUCCEEDED) {
                        switch (item.getType()) {
                            case ORDER :
                                // For order items, transfer data is stored with
                                // payment analytics data
                                this.payInItemHistoryRepository.updateTransfer(item.getId(), transfer);
                                break;

                            case SERVICE_BILLING :
                                // For service billing items, transfer data is
                                // stored with the billing record
                                final var serviceItem = (PayInServiceBillingItemEntity) item;
                                this.serviceBillingRepository.updateTransfer(serviceItem.getServiceBilling().getId(), transfer);
                                break;

                            default :
                                throw new PaymentException(PaymentMessageCode.SERVER_ERROR, String.format(
                                    "[MANGOPAY] PayIn item type not supported [key=%s, index=%d, type=%s]",
                                    payInKey, item.getId(), item.getType())
                                );
                        }
                    }

                    transfer.setKey(item.getTransferKey());
                    transfers.add(transfer);

                    // Update provider wallet
                    if (transfer.getStatus() == EnumTransactionStatus.SUCCEEDED) {
                        final UUID providerKey = switch (item.getType()) {
                            case ORDER ->
                                item.getProvider().getKey();

                            case SERVICE_BILLING -> {
                                final var serviceItem = (PayInServiceBillingItemEntity) item;
                                yield switch (serviceItem.getServiceBilling().getType()) {
                                    case SUBSCRIPTION ->
                                        serviceItem.getServiceBilling().getSubscription().getProvider().getKey();

                                    case PRIVATE_OGC_SERVICE ->
                                        this.accountRepository.findById(topioAccountId).get().getKey();
                                };
                            }
                        };
                        this.walletService.updateCustomerWalletFunds(providerKey, EnumCustomerType.PROVIDER);
                    }
                }
            }

            this.payInRepository.saveAndFlush(payIn);

            // Update consumer wallet if at least one transfer exists
            if (!transfers.isEmpty()) {
                this.walletService.updateCustomerWalletFunds(debitAccount.getKey(), EnumCustomerType.CONSUMER);
            }

            return transfers;
        } catch (final Exception ex) {
            throw this.wrapException(String.format("PayIn transfer creation has failed [key=%s]", payInKey), ex);
        }
    }

    private TransferDto createTransferForOrder(
        String idempotencyKey, UUID payInKey, PayInOrderItemEntity item, CustomerEntity debitCustomer, BigDecimal feePercent
    ) throws Exception {
        Assert.isTrue(item.getOrder() != null, "Expected a non-null order");
        Assert.isTrue(item.getOrder().getStatus() == EnumOrderStatus.SUCCEEDED, "Expected order status to be SUCCEEDED");
        Assert.isTrue(item.getOrder().getItems() != null, "Expected a non-null items collection");
        Assert.isTrue(item.getOrder().getItems().size() == 1, "Expected only a single item in the order");

        // Get credit customer
        final AccountEntity  creditAccount  = item.getOrder().getItems().get(0).getProvider();
        final CustomerEntity creditCustomer = creditAccount.getProfile().getProvider();
        final BigDecimal     amount         = item.getOrder().getTotalPrice().multiply(BigDecimal.valueOf(100L));
        final BigDecimal     fees           = item.getOrder().getTotalPrice()
            .multiply(feePercent)
            .divide(BigDecimal.valueOf(100L))
            .setScale(2, RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(100L));

        if (creditCustomer == null) {
            throw new PaymentException(PaymentMessageCode.SERVER_ERROR, String.format(
                "[MANGOPAY] Credit customer for PayIn item was not found [key=%s, index=%d]",
                payInKey, item.getIndex()
            ));
        }

        // Check if this is a retry operation
        Transfer transferResponse = this.<Transfer>getResponse(idempotencyKey);

        // Create a new transfer if needed
        if (transferResponse == null) {
            final Transfer transferRequest = this.createTransfer(
                idempotencyKey, debitCustomer, creditCustomer, amount, fees
            );

            transferResponse = this.api.getTransferApi().create(idempotencyKey, transferRequest);
        }

        final TransferDto result = this.transferResponseToDto(transferResponse);

        return result;
    }

    private TransferDto createTransferForServiceBilling(
        String idempotencyKey, UUID payInKey, PayInServiceBillingItemEntity item, CustomerEntity debitCustomer,
        BigDecimal feePercent, Integer topioAccountId
    ) throws Exception {
        Assert.isTrue(item.getServiceBilling() != null, "Expected a non-null subscription billing record");

        // Get credit customer
        final AccountEntity creditAccount = switch (item.getServiceBilling().getType()) {
            case SUBSCRIPTION ->
                item.getServiceBilling().getSubscription().getProvider();

            case PRIVATE_OGC_SERVICE ->
                this.accountRepository.findById(topioAccountId).orElse(null);

            default ->
                throw new PaymentException(PaymentMessageCode.SERVER_ERROR, String.format(
                    "[MANGOPAY] Service billing item type not supported [payInKey=%s, index=%d, type=%s]",
                    payInKey, item.getId(), item.getServiceBilling().getType())
                );
        };

        final var creditCustomer = creditAccount.getProfile().getProvider();
        final var amount         = item.getServiceBilling().getTotalPrice().multiply(BigDecimal.valueOf(100L));
        final var fees           = switch (item.getServiceBilling().getType()) {
            case SUBSCRIPTION -> item.getServiceBilling().getTotalPrice()
                .multiply(feePercent)
                .divide(BigDecimal.valueOf(100L))
                .setScale(2, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100L));

             // For private OGC services, the provider
             // is the Topio platform; Hence all funds
             // are credited to the platform wallet as
             // fees
            case PRIVATE_OGC_SERVICE ->
                amount;

            default ->
                throw new PaymentException(PaymentMessageCode.SERVER_ERROR, String.format(
                    "[MANGOPAY] Service billing item type not supported [payInKey=%s, index=%d, type=%s]",
                    payInKey, item.getId(), item.getServiceBilling().getType())
                );
        };

        if (creditCustomer == null) {
            throw new PaymentException(PaymentMessageCode.SERVER_ERROR, String.format(
                "[MANGOPAY] Credit customer for PayIn item was not found [key=%s, index=%d]",
                payInKey, item.getIndex()
            ));
        }

        // Check if this is a retry operation
        Transfer transferResponse = this.<Transfer>getResponse(idempotencyKey);

        // Create a new transfer if needed
        if (transferResponse == null) {
            final Transfer transferRequest = this.createTransfer(
                idempotencyKey, debitCustomer, creditCustomer, amount, fees
            );

            transferResponse = this.api.getTransferApi().create(idempotencyKey, transferRequest);
        }

        final TransferDto result = this.transferResponseToDto(transferResponse);

        return result;
    }

    @Override
    public void updateTransfer(String transferId) throws PaymentException {
        try {
            final PayInItemEntity payInItemEntity = this.ensurePayInItemTransfer(transferId);
            final PayInEntity     payInEntity     = payInItemEntity.getPayin();

            final Transfer    transferResponse = this.api.getTransferApi().get(transferId);
            final TransferDto transferObject   = this.transferResponseToDto(transferResponse);

            // Handle redundant updates
            if (payInItemEntity.getTransferStatus() == transferObject.getStatus()) {
                return;
            }

            // Update item
            payInItemEntity.updateTransfer(transferObject);

            this.payInRepository.saveAndFlush(payInEntity);

            // Always update history when a transfer is updated. A failed
            // transfer may be retried and succeed.
            payInItemHistoryRepository.updateTransfer(payInItemEntity.getId(), transferObject);
        } catch (final Exception ex) {
            throw this.wrapException("Update Transfer", ex, transferId);
        }
    }

    private Transfer createTransfer(
        String idempotentKey, CustomerEntity debitCustomer, CustomerEntity creditCustomer, BigDecimal amount, BigDecimal fees
    ) {
        final String debitUserId    = debitCustomer.getPaymentProviderUser();
        final String debitWalletId  = debitCustomer.getPaymentProviderWallet();
        final String creditUserId   = creditCustomer.getPaymentProviderUser();
        final String creditWalletId = creditCustomer.getPaymentProviderWallet();

        final Transfer result = new Transfer();
        result.setAuthorId(debitUserId);
        result.setCreditedUserId(creditUserId);
        result.setDebitedFunds(new Money(CurrencyIso.EUR, amount.intValueExact()));
        result.setDebitedWalletId(debitWalletId);
        result.setFees(new Money(CurrencyIso.EUR, fees.intValueExact()));
        result.setCreditedUserId(creditUserId);
        result.setCreditedWalletId(creditWalletId);
        result.setTag(idempotentKey);

        return result;
    }


    private TransferDto transferResponseToDto(Transfer transfer) {
        final TransferDto result = new TransferDto();

        result.setCreatedOn(this.timestampToDate(transfer.getCreationDate()));
        result.setCreditedFunds(BigDecimal.valueOf(transfer.getCreditedFunds().getAmount()).divide(BigDecimal.valueOf(100)));
        result.setExecutedOn(this.timestampToDate(transfer.getExecutionDate()));
        result.setFees(BigDecimal.valueOf(transfer.getFees().getAmount()).divide(BigDecimal.valueOf(100)));
        result.setId(transfer.getId());
        result.setResultCode(transfer.getResultCode());
        result.setResultMessage(transfer.getResultMessage());
        result.setStatus(EnumTransactionStatus.from(transfer.getStatus()));

        return result;
    }

    private PayInItemEntity ensurePayInItemTransfer(String transferId) {
        final PayInItemEntity payInItemEntity = this.payInRepository.findOnePayInItemByTransferId(transferId).orElse(null);

        if (payInItemEntity == null) {
            throw new PaymentException(
                PaymentMessageCode.RESOURCE_NOT_FOUND,
                String.format("[OpertusMundi] Transfer [%s] was not found", transferId)
            );
        }

        return payInItemEntity;
    }

    private PaymentException wrapException(String operation, Exception ex) {
        return super.wrapException(operation, ex, null, logger);
    }

    protected PaymentException wrapException(String operation, Exception ex, Object command) {
        return super.wrapException(operation, ex, command, logger);
    }

}
