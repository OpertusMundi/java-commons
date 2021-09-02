package eu.opertusmundi.common.service;

import java.util.UUID;

import eu.opertusmundi.common.model.order.ConsumerOrderDto;
import eu.opertusmundi.common.model.order.OrderConfirmCommandDto;
import eu.opertusmundi.common.model.order.OrderDeliveryCommandDto;
import eu.opertusmundi.common.model.order.OrderException;
import eu.opertusmundi.common.model.order.OrderShippingCommandDto;
import eu.opertusmundi.common.model.order.ProviderOrderDto;
import eu.opertusmundi.common.model.payment.EnumTransactionStatus;

public interface OrderFulfillmentService {

    /**
     * Accept an order
     *
     * @param command
     * @return
     * @throws OrderException if order not found or order status is invalid
     */
    ProviderOrderDto acceptOrder(OrderConfirmCommandDto command) throws OrderException;

    /**
     * Reject an order
     *
     * @param command
     * @return
     * @throws OrderException if order not found or order status is invalid
     */
    ProviderOrderDto rejectOrder(OrderConfirmCommandDto command) throws OrderException;

    /**
     * Initialize a workflow instance to process the referenced PayIn.
     *
     * This method is applicable for PayIns that refer to an order record.
     *
     * @param payInKey
     * @param payInId
     * @param payInStatus
     * @return
     */
    String startOrderWorkflow(UUID payInKey, String payInId, EnumTransactionStatus payInStatus);

    /**
     * Update PayIn status in an existing workflow instance
     *
     * @param payInKey
     * @param status
     */
    void sendPayInStatusUpdateMessage(UUID payInKey, EnumTransactionStatus status);

    /**
     * Confirm order shipping by a provider
     *
     * @param command
     * @return
     * @throws OrderException if order not found or order status is invalid
     */
    ProviderOrderDto sendOrderByProvider(OrderShippingCommandDto command) throws OrderException;

    /**
     * Confirm order delivery by a consumer
     *
     * @param command
     * @return
     * @throws OrderException if order not found or order status is invalid
     */
    ConsumerOrderDto receiveOrderByConsumer(OrderDeliveryCommandDto command) throws OrderException;

    /**
     * Update the profile of a user after a PayIn is successfully processed
     *
     * @param payInKey
     * @throws Exception if order status update fails
     */
    void updateConsumer(UUID payInKey) throws Exception;

}
