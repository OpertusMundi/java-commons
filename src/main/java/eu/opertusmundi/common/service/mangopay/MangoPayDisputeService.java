package eu.opertusmundi.common.service.mangopay;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mangopay.core.enumerations.InitialTransactionType;
import com.mangopay.entities.Dispute;

import eu.opertusmundi.common.domain.DisputeEntity;
import eu.opertusmundi.common.domain.PayInEntity;
import eu.opertusmundi.common.model.payment.DisputeDto;
import eu.opertusmundi.common.model.payment.EnumDisputeReasonType;
import eu.opertusmundi.common.model.payment.EnumDisputeStatus;
import eu.opertusmundi.common.model.payment.EnumDisputeType;
import eu.opertusmundi.common.model.payment.EnumTransactionType;
import eu.opertusmundi.common.model.payment.PaymentException;
import eu.opertusmundi.common.model.payment.PaymentMessageCode;
import eu.opertusmundi.common.repository.AccountRepository;
import eu.opertusmundi.common.repository.DisputeRepository;
import eu.opertusmundi.common.repository.PayInRepository;

@Service
@Transactional
public class MangoPayDisputeService extends BaseMangoPayService implements DisputeService {

    private static final Logger logger = LoggerFactory.getLogger(MangoPayDisputeService.class);

    private final DisputeRepository          disputeRepository;
    private final PayInRepository            payInRepository;

    @Autowired
    public MangoPayDisputeService(
        AccountRepository       accountRepository,
        DisputeRepository       disputeRepository,
        PayInRepository         payInRepository
    ) {
        super(accountRepository);

        this.disputeRepository = disputeRepository;
        this.payInRepository   = payInRepository;
    }

    @Override
    public DisputeDto createDispute(String eventType, String disputeId) throws PaymentException {
        try {
            final var disputeObject = this.ensureDispute(disputeId, InitialTransactionType.PAYIN);
            final var payInEntity   = this.ensurePayIn(disputeObject.getInitialTransactionId());
            final var disputeEntity = this.createDispute(disputeObject, payInEntity);

            return disputeEntity.toDto(true, true);
        } catch (final PaymentException ex) {
            throw ex;
        } catch (final Exception ex) {
            throw this.wrapException("PayIn Dispute", ex, disputeId);
        }
    }

    private DisputeEntity createDispute(Dispute dispute, PayInEntity payInEntity) throws PaymentException {
        try {
            DisputeEntity e = this.disputeRepository.findOneByTransactionId(dispute.getId()).orElse(null);
            if (e == null) {
                e = new DisputeEntity();
            }

            e.setContestDeadlineDate(timestampToDate(dispute.getContestDeadlineDate()));
            e.setContestedFunds(BigDecimal.valueOf(dispute.getContestedFunds().getAmount()).divide(BigDecimal.valueOf(100L)));
            e.setCreationDate(timestampToDate(dispute.getCreationDate()));
            e.setDisputedFunds(BigDecimal.valueOf(dispute.getDisputedFunds().getAmount()).divide(BigDecimal.valueOf(100L)));
            e.setInitialTransactionId(dispute.getInitialTransactionId());
            e.setInitialTransactionKey(payInEntity.getKey());
            e.setInitialTransactionRefNumber(payInEntity.getReferenceNumber());
            e.setInitialTransactionType(EnumTransactionType.from(dispute.getInitialTransactionType()));
            e.setPayin(payInEntity);
            e.setReasonMessage(dispute.getDisputeReason().getDisputeReasonMessage());
            e.setReasonType(EnumDisputeReasonType.from(dispute.getDisputeReason().getDisputeReasonType()));
            e.setRepudiationId(dispute.getRepudiationId());
            e.setResultCode(dispute.getResultCode());
            e.setResultMessage(dispute.getResultMessage());
            e.setStatus(EnumDisputeStatus.from(dispute.getStatus()));
            e.setStatusMessage(dispute.getStatusMessage());
            e.setTransactionId(dispute.getId());
            e.setType(EnumDisputeType.from(dispute.getDisputeType()));

            e = this.disputeRepository.saveAndFlush(e);

            return e;
        } catch (final PaymentException ex) {
            throw ex;
        }
    }

    private Dispute ensureDispute(String resourceId, InitialTransactionType type) throws PaymentException {
        try {
            final Dispute dispute = this.api.getDisputeApi().get(resourceId);
            if (dispute == null) {
                throw new PaymentException(
                    PaymentMessageCode.RESOURCE_NOT_FOUND,
                    String.format("[OpertusMundi] Dispute was not found [resourceId=%s]", resourceId)
                );
            }
            if (dispute.getInitialTransactionType() != type) {
                throw new PaymentException(
                    PaymentMessageCode.REFUND_INVALID_TYPE,
                    String.format("[OpertusMundi] Invalid dispute transaction type [expected=%s, found=%s]", type, dispute.getInitialTransactionType())
                );
            }
            return dispute;
        } catch (final PaymentException ex) {
            throw ex;
        } catch (final Exception ex) {
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

    private PaymentException wrapException(String operation, Exception ex, Object command) {
        return super.wrapException(operation, ex, command, logger);
    }
}