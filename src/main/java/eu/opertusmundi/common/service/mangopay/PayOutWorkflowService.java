package eu.opertusmundi.common.service.mangopay;

import java.util.UUID;

import eu.opertusmundi.common.model.payment.EnumTransactionStatus;

public interface PayOutWorkflowService {

    /**
     * Initializes a workflow instance to process the referenced PayOut
     *
     * <p>
     * The operation may fail because of (a) a network error, (b) BPM engine
     * service error or (c) database command error. The operation is retried for
     * at most 3 times, with a maximum latency due to attempt delays of 9
     * seconds.
     *
     * @param userKey
     * @param payOutKey
     * @return
     */
    String start(UUID userKey, UUID payOutKey);

    /**
     * Update PayOut status in an existing workflow instance
     *
     * <p>
     * This method is invoked by a MANGOPAY web hook for
     * {@code PAYOUT_REFUND_CREATED}, {@code PAYOUT_REFUND_SUCCEEDED}, and
     * {@code PAYOUT_REFUND_FAILED} events
     *
     * @param payOutKey
     * @param status
     */
    void sendStatusUpdateMessage(UUID payOutKey, EnumTransactionStatus status);

}
