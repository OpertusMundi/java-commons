package eu.opertusmundi.common.service;

import java.util.UUID;

import eu.opertusmundi.common.model.payment.EnumTransactionStatus;

public interface OrderFulfillmentService {

    /**
     * Initialize a workflow instance to process the referenced PayIn
     *
     * @param payInKey
     * @param payInId
     * @param payInStatus
     * @return
     */
    String start(UUID payInKey, String payInId, EnumTransactionStatus payInStatus);

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
     * @throws Exception if order status update fails
     */
    void updateConsumer(UUID payInKey) throws Exception;

}
