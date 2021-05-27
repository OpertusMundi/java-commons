package eu.opertusmundi.common.service;

import java.util.UUID;

import eu.opertusmundi.common.model.payment.EnumTransactionStatus;

public interface OrderFulfillmentService {

    /**
     * Initialize a workflow instance to process the referenced PayIn
     *
     * @param payInKey
     * @return
     */
    String start(UUID payInKey);

    /**
     * Update PayIn status in an existing workflow instance
     *
     * @param payInKey
     * @param status
     */
    void sendPayInStatusUpdateMessage(UUID payInKey, EnumTransactionStatus status);

    /**
     * Update the profile of a user after a PayIn is successfully processed
     *
     * @param payInKey
     */
    void updateConsumer(UUID payInKey);

}
