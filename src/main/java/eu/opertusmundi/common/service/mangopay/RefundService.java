package eu.opertusmundi.common.service.mangopay;

import java.util.UUID;

import eu.opertusmundi.common.model.ServiceException;
import eu.opertusmundi.common.model.payment.PaymentException;
import eu.opertusmundi.common.model.payment.TransactionDto;

public interface RefundService {

    /**
     * Initializes a workflow instance to process the referenced refund
     *
     * <p>
     * See {@link #start(UUID, String, String)}
     *
     * @param eventType
     * @param refundId
     * @return
     * @throws ServiceException
     */
    default String start(String eventType, String refundId) throws ServiceException{
        return this.start(null, eventType, refundId);
    }

    /**
     * Initializes a workflow instance to process the referenced refund
     *
     * @param userKey The key of the Helpdesk user who initiated this refund
     * @param eventType Payment provider event type e.g. @{code PAYIN_REFUND_SUCCEEDED}
     * @param refundId Payment provider refund identifier
     * @return The process instance identifier
     * @throws ServiceException
     */
    String start(UUID userKey, String eventType, String refundId) throws ServiceException ;

    /**
     * Create refund database record
     *
     * @param eventType
     * @param refundId
     * @return
     * @throws PaymentException
     */
    TransactionDto createRefund(String eventType, String refundId) throws PaymentException;

}
