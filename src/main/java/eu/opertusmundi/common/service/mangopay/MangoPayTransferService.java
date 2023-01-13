package eu.opertusmundi.common.service.mangopay;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
import eu.opertusmundi.common.domain.TransferEntity;
import eu.opertusmundi.common.model.EnumSetting;
import eu.opertusmundi.common.model.account.EnumCustomerType;
import eu.opertusmundi.common.model.order.EnumOrderStatus;
import eu.opertusmundi.common.model.payment.EnumTransactionNature;
import eu.opertusmundi.common.model.payment.EnumTransactionStatus;
import eu.opertusmundi.common.model.payment.EnumTransactionType;
import eu.opertusmundi.common.model.payment.PaymentException;
import eu.opertusmundi.common.model.payment.PaymentMessageCode;
import eu.opertusmundi.common.model.payment.TransferDto;
import eu.opertusmundi.common.repository.AccountRepository;
import eu.opertusmundi.common.repository.CustomerRepository;
import eu.opertusmundi.common.repository.PayInItemHistoryRepository;
import eu.opertusmundi.common.repository.PayInRepository;
import eu.opertusmundi.common.repository.ServiceBillingRepository;
import eu.opertusmundi.common.repository.SettingRepository;
import eu.opertusmundi.common.repository.TransferRepository;

@Service
@Transactional
public class MangoPayTransferService extends BaseMangoPayService implements TransferService {

    private static final Logger logger = LoggerFactory.getLogger(MangoPayTransferService.class);

    private final CustomerRepository         customerRepository;
    private final PayInRepository            payInRepository;
    private final PayInItemHistoryRepository payInItemHistoryRepository;
    private final ServiceBillingRepository   serviceBillingRepository;
    private final SettingRepository          settingRepository;
    private final TransferRepository         transferRepository;
    private final WalletService              walletService;

    @Autowired
    public MangoPayTransferService(
        AccountRepository             accountRepository,
        CustomerRepository            customerRepository,
        PayInRepository               payInRepository,
        PayInItemHistoryRepository    payInItemHistoryRepository,
        ServiceBillingRepository      serviceBillingRepository,
        SettingRepository             settingRepository,
        TransferRepository            transferRepository,
        WalletService                 walletService
    ) {
        super(accountRepository);

        this.customerRepository         = customerRepository;
        this.payInRepository            = payInRepository;
        this.payInItemHistoryRepository = payInItemHistoryRepository;
        this.serviceBillingRepository   = serviceBillingRepository;
        this.settingRepository          = settingRepository;
        this.transferRepository         = transferRepository;
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
                if (item.getTransfer() != null) {
                    // If a valid transfer transaction identifier exists, this
                    // is a retry operation
                    transfers.add(item.getTransfer().toDto(true));
                    continue;
                }
                final String idempotencyKey = item.getKey().toString();
                Transfer     transfer       = null;
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
                    final var transferEntity = this.createTopioTransfer(transfer);

                    // If the transfer is successful, update (a) the payIn item,
                    // (b) the provider wallets and (c) analytics
                    if (transferEntity.getTransactionStatus() == EnumTransactionStatus.SUCCEEDED) {
                        item.setTransfer(transferEntity);

                        switch (item.getType()) {
                            case ORDER :
                                // Update analytics
                                this.payInItemHistoryRepository.updateTransfer(item.getId(), transferEntity);
                                break;

                            case SERVICE_BILLING :
                                // For service billing items, analytics data is
                                // stored within the service billing entity
                                final var serviceItem = (PayInServiceBillingItemEntity) item;
                                this.serviceBillingRepository.updateTransfer(serviceItem.getServiceBilling().getId(), transferEntity);
                                break;

                            default :
                                throw new PaymentException(PaymentMessageCode.SERVER_ERROR, String.format(
                                    "[MANGOPAY] PayIn item type not supported [key=%s, index=%d, type=%s]",
                                    payInKey, item.getId(), item.getType())
                                );
                        }

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
                        this.walletService.refreshUserWallets(providerKey, EnumCustomerType.PROVIDER);
                    }
                    if (transferEntity.getTransactionStatus() == EnumTransactionStatus.FAILED) {
                        // Reset payIn item key to create a new Transfer on next
                        // try since the key is used as the idempotent key for
                        // the MANGOPAY operation
                        item.setKey(UUID.randomUUID());
                    }

                    transfers.add(transferEntity.toDto(true));
                }
            }

            this.payInRepository.saveAndFlush(payIn);

            // Update consumer wallet if at least one transfer exists
            if (!transfers.isEmpty()) {
                this.walletService.refreshUserWallets(debitAccount.getKey(), EnumCustomerType.CONSUMER);
            }

            return transfers;
        } catch (final Exception ex) {
            throw this.wrapException(String.format("PayIn transfer creation has failed [key=%s]", payInKey), ex);
        }
    }

    private Transfer createTransferForOrder(
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
        Transfer transfer = this.<Transfer>getResponse(idempotencyKey);

        // Create a new transfer if needed
        if (transfer == null) {
            final Transfer transferRequest = this.createMangopayTransfer(
                idempotencyKey, debitCustomer, creditCustomer, amount, fees
            );

            transfer = this.api.getTransferApi().create(idempotencyKey, transferRequest);
        }

        return transfer;
    }

    private Transfer createTransferForServiceBilling(
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
        Transfer transfer = this.<Transfer>getResponse(idempotencyKey);

        // Create a new transfer if needed
        if (transfer == null) {
            final Transfer transferRequest = this.createMangopayTransfer(
                idempotencyKey, debitCustomer, creditCustomer, amount, fees
            );

            transfer = this.api.getTransferApi().create(idempotencyKey, transferRequest);
        }

        return transfer;
    }

    @Override
    public void updateTransfer(String transferId) throws PaymentException {
        try {
            final PayInItemEntity payInItemEntity = this.ensurePayInItemTransfer(transferId);
            final PayInEntity     payInEntity     = payInItemEntity.getPayin();
            TransferEntity        transferEntity  = payInItemEntity.getTransfer();

            final Transfer transferObject = this.api.getTransferApi().get(transferId);

            // Handle redundant updates
            if (transferEntity.getTransactionStatus() == EnumTransactionStatus.from(transferObject.getStatus())) {
                return;
            }

            transferEntity = this.createTopioTransfer(transferObject);
            payInItemEntity.setTransfer(transferEntity);

            this.payInRepository.saveAndFlush(payInEntity);

            // Always update history when a transfer is updated. A failed
            // transfer may be retried and succeed.
            payInItemHistoryRepository.updateTransfer(payInItemEntity.getId(), transferEntity);
        } catch (final Exception ex) {
            throw this.wrapException("Update Transfer", ex, transferId);
        }
    }

    private Transfer createMangopayTransfer(
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

    private TransferEntity createTopioTransfer(Transfer transfer) throws PaymentException {
        try {
            TransferEntity e = this.transferRepository.findOneByTransactionId(transfer.getId()).orElse(null);
            if (e == null) {
                e = new TransferEntity();
            }

            final var consumer = this.customerRepository.findCustomerByProviderWalletId(transfer.getDebitedWalletId())
                    .map(c -> c.getAccount()).orElse(null);
            final var provider = this.customerRepository.findCustomerByProviderWalletId(transfer.getCreditedWalletId())
                    .map(c -> c.getAccount()).orElse(null);

            e.setAuthorId(transfer.getAuthorId());
            e.setConsumer(consumer);
            e.setCreationDate(timestampToDate(transfer.getCreationDate()));
            e.setCreditedFunds(BigDecimal.valueOf(transfer.getCreditedFunds().getAmount()).divide(BigDecimal.valueOf(100L)));
            e.setCreditedUserId(transfer.getCreditedUserId());
            e.setCreditedWalletId(transfer.getDebitedWalletId());
            e.setCurrency(transfer.getCreditedFunds().getCurrency().toString());
            e.setDebitedFunds(BigDecimal.valueOf(transfer.getDebitedFunds().getAmount()).divide(BigDecimal.valueOf(100L)));
            e.setDebitedWalletId(transfer.getDebitedWalletId());
            e.setExecutionDate(timestampToDate(transfer.getExecutionDate()));
            e.setFees(BigDecimal.valueOf(transfer.getFees().getAmount()).divide(BigDecimal.valueOf(100L)));
            e.setProvider(provider);
            e.setResultCode(transfer.getResultCode());
            e.setResultMessage(transfer.getResultMessage());
            e.setTransactionId(transfer.getId());
            e.setTransactionNature(EnumTransactionNature.from(transfer.getNature()));
            e.setTransactionStatus(EnumTransactionStatus.from(transfer.getStatus()));
            e.setTransactionType(EnumTransactionType.from(transfer.getType()));

            e = this.transferRepository.saveAndFlush(e);

            return e;
        } catch (final PaymentException ex) {
            throw ex;
        }
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
