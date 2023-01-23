package eu.opertusmundi.common.service.mangopay;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

import org.camunda.bpm.engine.rest.dto.VariableValueDto;
import org.camunda.bpm.engine.rest.dto.runtime.ProcessInstanceDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.mangopay.core.enumerations.EventType;
import com.mangopay.core.enumerations.InitialTransactionType;
import com.mangopay.core.enumerations.TransactionStatus;
import com.mangopay.entities.Refund;

import eu.opertusmundi.common.domain.PayInEntity;
import eu.opertusmundi.common.domain.PayInItemEntity;
import eu.opertusmundi.common.domain.PayInOrderItemEntity;
import eu.opertusmundi.common.domain.PayInServiceBillingItemEntity;
import eu.opertusmundi.common.domain.PayOutEntity;
import eu.opertusmundi.common.domain.RefundEntity;
import eu.opertusmundi.common.model.EnumReferenceType;
import eu.opertusmundi.common.model.ServiceException;
import eu.opertusmundi.common.model.account.EnumPayoffStatus;
import eu.opertusmundi.common.model.payment.EnumPaymentItemType;
import eu.opertusmundi.common.model.payment.EnumRefundReasonType;
import eu.opertusmundi.common.model.payment.EnumTransactionNature;
import eu.opertusmundi.common.model.payment.EnumTransactionStatus;
import eu.opertusmundi.common.model.payment.EnumTransactionType;
import eu.opertusmundi.common.model.payment.PaymentException;
import eu.opertusmundi.common.model.payment.PaymentMessageCode;
import eu.opertusmundi.common.model.payment.TransactionDto;
import eu.opertusmundi.common.model.workflow.EnumProcessInstanceVariable;
import eu.opertusmundi.common.model.workflow.EnumWorkflow;
import eu.opertusmundi.common.repository.AccountRepository;
import eu.opertusmundi.common.repository.CustomerRepository;
import eu.opertusmundi.common.repository.PayInItemHistoryRepository;
import eu.opertusmundi.common.repository.PayInRepository;
import eu.opertusmundi.common.repository.PayOutRepository;
import eu.opertusmundi.common.repository.RefundRepository;
import eu.opertusmundi.common.util.BpmEngineUtils;
import eu.opertusmundi.common.util.BpmInstanceVariablesBuilder;
import eu.opertusmundi.common.util.MangopayUtils;

@Service
@Transactional
public class MangoPayRefundService extends BaseMangoPayService implements RefundService {

    private static final Logger logger = LoggerFactory.getLogger(MangoPayRefundService.class);

    private final BpmEngineUtils             bpmEngine;
    private final CustomerRepository         customerRepository;
    private final PayInRepository            payInRepository;
    private final PayInItemHistoryRepository payInItemHistoryRepository;
    private final PayOutRepository           payOutRepository;
    private final RefundRepository           refundRepository;

    @Autowired
    public MangoPayRefundService(
        AccountRepository           accountRepository,
        BpmEngineUtils              bpmEngine,
        CustomerRepository          customerRepository,
        PayInRepository             payInRepository,
        PayInItemHistoryRepository  payInItemHistoryRepository,
        PayOutRepository            payOutRepository,
        RefundRepository            refundRepository
    ) {
        super(accountRepository);

        this.bpmEngine                  = bpmEngine;
        this.customerRepository         = customerRepository;
        this.payInRepository            = payInRepository;
        this.payInItemHistoryRepository = payInItemHistoryRepository;
        this.payOutRepository           = payOutRepository;
        this.refundRepository           = refundRepository;
    }

    @Override
    public String start(UUID userKey, String eventType, String refundId) throws ServiceException {
        Assert.hasText(refundId, "Expected a non-empty refund identifier");

        final EnumWorkflow workflow = EnumWorkflow.CONSUMER_REFUND;

        try {
            final String       businessKey = this.createProcessInstanceBusinessKey(eventType, refundId);
            ProcessInstanceDto instance    = this.bpmEngine.findInstance(businessKey);

            if (instance == null) {
                final Map<String, VariableValueDto> variables = BpmInstanceVariablesBuilder.builder()
                    .variableAsString(EnumProcessInstanceVariable.START_USER_KEY.getValue(), userKey == null ? "" : userKey.toString())
                    .variableAsString("refundId", refundId)
                    .variableAsString("eventType", eventType)
                    .build();

                instance = this.bpmEngine.startProcessDefinitionByKey(workflow, businessKey, variables, true);
            }

            return instance.getId();
        } catch(final Exception ex) {
            logger.error(
                "Failed to start workflow instance [workflow={}, refundId={}, ex={}]",
                workflow, refundId, ex.getMessage()
            );

            throw new ServiceException(PaymentMessageCode.SERVER_ERROR, "Failed to start refund workflow instance");
        }
    }

    @Override
    public TransactionDto createRefund(String eventType, String refundId) throws PaymentException {
        final var e = EventType.valueOf(eventType);

        switch (e) {
            case PAYIN_REFUND_SUCCEEDED :
            case PAYIN_REFUND_FAILED :
                return this.createPayInRefund(e, refundId);

            case TRANSFER_REFUND_SUCCEEDED :
            case TRANSFER_REFUND_FAILED :
                return this.createTransferRefund(e, refundId);

            case PAYOUT_REFUND_SUCCEEDED :
            case PAYOUT_REFUND_FAILED :
                return this.createPayOutRefund(e, refundId);

            default :
                throw new PaymentException(
                    PaymentMessageCode.ENUM_MEMBER_NOT_SUPPORTED,
                    String.format("Refund event type not supported [eventType=%s, refundId=%s]", eventType, refundId)
                );
        }
    }

    private TransactionDto createPayInRefund(EventType eventType, String refundId) throws PaymentException {
        try {
            final var refundObject = this.ensureRefund(refundId, InitialTransactionType.PAYIN);
            var       payInEntity  = this.ensurePayIn(refundObject.getInitialTransactionId());
            if (refundObject.getStatus() != TransactionStatus.SUCCEEDED) {
                return payInEntity.toHelpdeskDto(true);
            }

            final var refundEntity = this.createRefund(payInEntity.getKey(), refundObject);
            payInEntity.setRefund(refundEntity);
            payInEntity = this.payInRepository.saveAndFlush(payInEntity);

            // Refund service billing records
            this.refundPayInOrderItem(refundEntity, payInEntity);
            this.refundPayInServiceBilling(refundEntity, payInEntity);
            payInEntity = this.payInRepository.saveAndFlush(payInEntity);

            return payInEntity.toHelpdeskDto(true);
        } catch (final PaymentException ex) {
            throw ex;
        } catch (final Exception ex) {
            throw this.wrapException("PayIn Refund", ex, refundId);
        }
    }

    /**
     * Refunds an order item for a PayIn refund.
     *
     * <p>
     * A PayIn always refers to a single order item. The item transfer is updated
     * during a Transfer refund. This method updates only the table used for
     * analytics.
     *
     * @param refund
     * @param payIn
     */
    private void refundPayInOrderItem(RefundEntity refund, PayInEntity payIn) {
        payIn.getItems().stream()
            .filter(i -> i.getType() == EnumPaymentItemType.ORDER)
            .map(e -> (PayInOrderItemEntity) e)
            .forEach(i -> {
                this.payInItemHistoryRepository.refundTransfer(i.getId());
            });
    }

    /**
     * Refunds all service billing records for a PayIn refund.
     *
     * @param refund
     * @param payIn
     */
    private void refundPayInServiceBilling(RefundEntity refund, PayInEntity payIn) {
        payIn.getItems().stream()
            .filter(i -> i.getType() == EnumPaymentItemType.SERVICE_BILLING)
            .map(e -> (PayInServiceBillingItemEntity) e)
            .map(e -> e.getServiceBilling())
            .forEach(i -> {
                i.setStatus(EnumPayoffStatus.DUE);
                i.setPayin(null);
                i.resetTransfer();
            });
    }

    private TransactionDto createTransferRefund(EventType eventType, String refundId) throws PaymentException {
        try {
            final var refundObject    = this.ensureRefund(refundId, InitialTransactionType.TRANSFER);
            final var payInItemEntity = this.ensureTransferPayInItem(refundObject.getInitialTransactionId());
            var       payInEntity     = payInItemEntity.getPayin();
            if (refundObject.getStatus() != TransactionStatus.SUCCEEDED) {
                return payInItemEntity.getPayin().toHelpdeskDto(true);
            }

            final var refundEntity = this.createRefund(payInEntity.getKey(), refundObject);
            payInItemEntity.getTransfer().setRefund(refundEntity);
            payInEntity = this.payInRepository.saveAndFlush(payInEntity);

            // Refund order item or service billing record
            this.refundTransferOrderItem(refundEntity, payInEntity);
            this.refundTransferServiceBilling(refundEntity, payInEntity);
            payInEntity = this.payInRepository.saveAndFlush(payInEntity);

            return payInEntity.toHelpdeskDto(true);
        } catch (final PaymentException ex) {
            throw ex;
        } catch (final Exception ex) {
            throw this.wrapException("PayIn Refund", ex, refundId);
        }
    }

    /**
     * Refunds an order item for a Transfer refund.
     *
     * <p>
     * A transfer always refers to a single PayIn item. This method handles
     * PayIn items of type {@link EnumPaymentItemType#ORDER}.
     *
     * @param refund
     * @param payIn
     */
    private void refundTransferOrderItem(RefundEntity refund, PayInEntity payIn) {
        payIn.getItems().stream()
            .filter(i -> i.getType() == EnumPaymentItemType.ORDER)
            .map(e -> (PayInOrderItemEntity) e)
            .forEach(i -> {
                this.payInItemHistoryRepository.refundTransfer(i.getId());
            });
    }

    /**
     * Refunds a service billing record for a transfer refund.
     *
     * <p>
     * A transfer always refers to a single PayIn item. This method handles
     * PayIn items of type {@link EnumPaymentItemType#SERVICE_BILLING}.
     *
     * @param refund
     * @param payIn
     */
    private void refundTransferServiceBilling(RefundEntity refund, PayInEntity payIn) {
        payIn.getItems().stream()
            .filter(i -> i.getType() == EnumPaymentItemType.SERVICE_BILLING)
            .map(e -> (PayInServiceBillingItemEntity) e)
            .forEach(i -> {
                i.getServiceBilling().setRefunded(true);
            });
    }

    private TransactionDto createPayOutRefund(EventType eventType, String refundId) throws PaymentException {
        try {
            final var refundObject = this.ensureRefund(refundId, InitialTransactionType.PAYOUT);
            final var payOutEntity = this.ensurePayOut(refundObject.getInitialTransactionId());
            if (refundObject.getStatus() != TransactionStatus.SUCCEEDED) {
                return payOutEntity.toDto(true);
            }

            final var refundEntity = this.createRefund(payOutEntity.getKey(), refundObject);
            payOutEntity.setRefund(refundEntity);

            final var updatePayOutEntity = this.payOutRepository.saveAndFlush(payOutEntity);
            return updatePayOutEntity.toDto(true);
        } catch (final Exception ex) {
            throw this.wrapException("PayOut Refund", ex, refundId);
        }
    }

    private String createProcessInstanceBusinessKey(String eventType, String refundId) {
        return String.format("REFUND::%s::%s", eventType, refundId);
    }

    private RefundEntity createRefund(UUID initialTransactionKey, Refund refund) throws PaymentException {
        try {
            RefundEntity e = this.refundRepository.findOneByTransactionId(refund.getId()).orElse(null);
            if (e == null) {
                e = new RefundEntity();
            }

            final var consumer = this.customerRepository.findCustomerByProviderWalletId(refund.getDebitedWalletId())
                .map(c -> c.getAccount())
                .orElse(null);
            final var provider = this.customerRepository.findCustomerByProviderWalletId(refund.getCreditedWalletId())
                .map(c -> c.getAccount())
                .orElse(null);

            e.setAuthorId(refund.getAuthorId());
            e.setConsumer(consumer);
            e.setCreationDate(timestampToDate(refund.getCreationDate()));
            e.setCreditedFunds(BigDecimal.valueOf(refund.getCreditedFunds().getAmount()).divide(BigDecimal.valueOf(100L)));
            e.setCreditedUserId(refund.getCreditedUserId());
            e.setCreditedWalletId(refund.getDebitedWalletId());
            e.setCurrency(refund.getCreditedFunds().getCurrency().toString());
            e.setDebitedFunds(BigDecimal.valueOf(refund.getDebitedFunds().getAmount()).divide(BigDecimal.valueOf(100L)));
            e.setDebitedWalletId(refund.getDebitedWalletId());
            e.setExecutionDate(timestampToDate(refund.getExecutionDate()));
            e.setFees(BigDecimal.valueOf(refund.getFees().getAmount()).divide(BigDecimal.valueOf(100L)));
            e.setInitialTransactionId(refund.getInitialTransactionId());
            e.setInitialTransactionKey(initialTransactionKey);
            e.setInitialTransactionType(EnumTransactionType.from(refund.getInitialTransactionType()));
            e.setProvider(provider);
            e.setRefundReasonMessage(refund.getRefundReason().getRefundReasonMessage());
            e.setRefundReasonType(EnumRefundReasonType.from(refund.getRefundReason().getRefundReasonType()));
            e.setResultCode(refund.getResultCode());
            e.setResultMessage(refund.getResultMessage());
            e.setTransactionId(refund.getId());
            e.setTransactionNature(EnumTransactionNature.from(refund.getNature()));
            e.setTransactionStatus(EnumTransactionStatus.from(refund.getStatus()));
            e.setTransactionType(EnumTransactionType.from(refund.getType()));

            e = this.refundRepository.saveAndFlush(e);

            // Compute reference number from database key
            e.setReferenceNumber(MangopayUtils.createReferenceNumber(EnumReferenceType.REFUND, e.getId()));
            e = this.refundRepository.saveAndFlush(e);

            return e;
        } catch (final PaymentException ex) {
            throw ex;
        }
    }

    private Refund ensureRefund(String resourceId, InitialTransactionType type) throws PaymentException {
        try {
            final Refund refund = this.api.getRefundApi().get(resourceId);
            if (refund == null) {
                throw new PaymentException(
                    PaymentMessageCode.RESOURCE_NOT_FOUND,
                    String.format("[OpertusMundi] Refund was not found [resourceId=%s]", resourceId)
                );
            }
            if (refund.getInitialTransactionType() != type) {
                throw new PaymentException(
                    PaymentMessageCode.REFUND_INVALID_TYPE,
                    String.format("[OpertusMundi] Invalid refund transaction type [expected=%s, found=%s]", type, refund.getInitialTransactionType())
                );
            }
            return refund;
        } catch (final PaymentException ex) {
            throw ex;
        } catch(final Exception ex) {
            throw this.wrapException("[OpertusMundi] Get refund", ex, resourceId);
        }
    }

    private PayInEntity ensurePayIn(String transactionId) {
        final var e = this.payInRepository.findOneByTransactionIdForUpdate(transactionId).orElse(null);

        if(e == null) {
            throw new PaymentException(
                PaymentMessageCode.RESOURCE_NOT_FOUND,
                String.format("[OpertusMundi] PayIn database record was not found [transactionId=%s]", transactionId)
            );
        }

        return e;
    }

    private PayInItemEntity ensureTransferPayInItem(String transactionId) {
        final var e = this.payInRepository.findOnePayInItemByTransferId(transactionId).orElse(null);

        if(e == null) {
            throw new PaymentException(
                PaymentMessageCode.RESOURCE_NOT_FOUND,
                String.format("[OpertusMundi] PayIn item database record was not found [transactionId=%s]", transactionId)
            );
        }

        return e;
    }

    private PayOutEntity ensurePayOut(String transactionId) {
        final var e = this.payOutRepository.findOneByTransactionIdForUpdate(transactionId).orElse(null);

        if(e == null) {
            throw new PaymentException(
                PaymentMessageCode.RESOURCE_NOT_FOUND,
                String.format("[OpertusMundi] PayOut database record was not found [transactionId=%s]", transactionId)
            );
        }

        return e;
    }

    protected PaymentException wrapException(String operation, Exception ex, Object command) {
        return super.wrapException(operation, ex, command, logger);
    }
}