package eu.opertusmundi.common.service;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

import eu.opertusmundi.common.model.ServiceException;
import eu.opertusmundi.common.model.email.EnumMailType;
import eu.opertusmundi.common.model.message.EnumNotificationType;
import eu.opertusmundi.common.model.order.AcceptOrderContractCommand;
import eu.opertusmundi.common.model.order.ConsumerOrderDto;
import eu.opertusmundi.common.model.order.OrderConfirmCommandDto;
import eu.opertusmundi.common.model.order.OrderDeliveryCommand;
import eu.opertusmundi.common.model.order.OrderException;
import eu.opertusmundi.common.model.order.OrderShippingCommandDto;
import eu.opertusmundi.common.model.order.ProviderOrderDto;
import eu.opertusmundi.common.model.order.UploadOrderContractCommand;
import eu.opertusmundi.common.model.payment.EnumTransactionStatus;

public interface OrderFulfillmentService {

    /**
     * Accept an order
     *
     * @param command
     * @return
     * @throws OrderException if order not found or order status is invalid
     */
    ProviderOrderDto acceptOrderByProvider(OrderConfirmCommandDto command) throws OrderException;

    /**
     * Reject an order
     *
     * @param command
     * @return
     * @throws OrderException if order not found or order status is invalid
     */
    ProviderOrderDto rejectOrderByProvider(OrderConfirmCommandDto command) throws OrderException;

    /**
     * Fill out the contract with the consumer's info
     *
     * @param command
     * @param input
     * @param sendNotification
     *
     * @return
     * @throws OrderException if order not found or order status is invalid
     */
    ProviderOrderDto uploadContractByProvider(UploadOrderContractCommand command, InputStream input, boolean sendNotification) throws OrderException;

    /**
     * Resolve path of an order uploaded contract
     *
     * @param providerKey
     * @param orderKey
     * @return
     */
    Path resolveOrderContractPath(UUID providerKey, UUID orderKey);

    /**
     * Accept the filled out by provider contact
     *
     * @param command
     * @return
     * @throws OrderException if order not found or order status is invalid
     */
    ConsumerOrderDto acceptContractByConsumer(AcceptOrderContractCommand command) throws OrderException;

    /**
     * Initialize a workflow instance to process the referenced PayIn.
     *
     * The PayIn total amount must be 0 e.g. an asset with FREE pricing model
     *
     * @param payInKey
     * @return
     */
    String startOrderWithoutPayInWorkflow(UUID payInKey);

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
    String startOrderWithPayInWorkflow(UUID payInKey, String payInId, EnumTransactionStatus payInStatus);

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
    ConsumerOrderDto receiveOrderByConsumer(OrderDeliveryCommand command) throws OrderException;

    /**
     * Update the profile of a user after a PayIn is successfully processed
     *
     * @param payInKey
     * @throws Exception if order status update fails
     */
    void registerConsumerAssets(UUID payInKey) throws Exception;

    /**
     * Send order status by mail
     *
     * @param mailType
     * @param recipientKey
     * @param orderKey
     * @throws ServiceException if send mail fails
     */
    void sendOrderStatusByMail(EnumMailType mailType, UUID recipientKey, UUID orderKey);

    /**
     * Send order status by notification
     *
     * @param notificationType
     * @param recipientKey
     * @param variables
     * @param idempotentKey
     * @throws ServiceException if send notification fails
     */
    void sendOrderStatusByNotification(EnumNotificationType notificationType, UUID recipientKey, Map<String, Object>  variables, String idempotentKey);

}
