package eu.opertusmundi.common.service;

import java.util.UUID;

import eu.opertusmundi.common.model.payment.EnumTransactionStatus;

public interface PayOutService {

    /**
     * Initialize a workflow instance to process the referenced PayOut
     *
     * @param userKey
     * @param payOutKey
     * @return
     */
    String start(UUID userKey, UUID payOutKey);

    /**
     * Update PayOut status in an existing workflow instance
     *
     * This method is invoked by a MANGOPAY web hook for PAYOUT_REFUND_CREATED,
     * PAYOUT_REFUND_SUCCEEDED, and PAYOUT_REFUND_FAILED events
     *
     * @param payOutKey
     * @param status
     */
    void sendPayOutStatusUpdateMessage(UUID payOutKey, EnumTransactionStatus status);

}
