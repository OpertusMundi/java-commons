package eu.opertusmundi.common.repository;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import eu.opertusmundi.common.domain.AccountEntity;
import eu.opertusmundi.common.domain.CartEntity;
import eu.opertusmundi.common.domain.OrderEntity;
import eu.opertusmundi.common.domain.OrderItemEntity;
import eu.opertusmundi.common.domain.OrderStatusEntity;
import eu.opertusmundi.common.domain.PayInEntity;
import eu.opertusmundi.common.model.EnumReferenceType;
import eu.opertusmundi.common.model.catalogue.client.EnumContractType;
import eu.opertusmundi.common.model.order.ConsumerOrderDto;
import eu.opertusmundi.common.model.order.EnumOrderStatus;
import eu.opertusmundi.common.model.order.HelpdeskOrderDto;
import eu.opertusmundi.common.model.order.OrderCommand;
import eu.opertusmundi.common.model.order.OrderDeliveryCommand;
import eu.opertusmundi.common.model.order.OrderDto;
import eu.opertusmundi.common.model.order.OrderException;
import eu.opertusmundi.common.model.order.OrderMessageCode;
import eu.opertusmundi.common.model.order.OrderShippingCommandDto;
import eu.opertusmundi.common.model.order.ProviderOrderDto;
import eu.opertusmundi.common.util.MangopayUtils;
import io.jsonwebtoken.lang.Assert;

@Repository
@Transactional(readOnly = true)
public interface OrderRepository extends JpaRepository<OrderEntity, Integer> {

    @Query("SELECT a FROM Account a WHERE a.id = :id")
    Optional<AccountEntity> findAccountById(@Param("id") Integer id);

    @Query("SELECT a FROM Account a WHERE a.key = :key")
    Optional<AccountEntity> findAccountByKey(@Param("key") UUID key);

    @Query("SELECT c FROM Cart c WHERE c.id = :id")
    Optional<CartEntity> findCartById(@Param("id") Integer id);

    @Query("SELECT o FROM Order o WHERE o.key = :key")
    Optional<OrderEntity> findOrderEntityByKey(@Param("key") UUID key);

    /**
     * Find an order created by a specific consumer
     *
     * This method does not return orders with status <b>CREATED</b>.
     *
     * @param consumerKey
     * @param orderKey
     * @return
     */
    @Query("SELECT o FROM Order o "
         + "WHERE o.key = :orderKey and o.consumer.key = :consumerKey and o.status <> 'CREATED'"
    )
    Optional<OrderEntity> findEntityByKeyAndConsumerAndStatusNotCreated(
        @Param("consumerKey") UUID consumerKey, @Param("orderKey") UUID orderKey
    );

    /**
     * Find an order created by a specific consumer
     *
     *
     * @param consumerKey
     * @param orderKey
     * @return
     */
    @Query("SELECT o FROM Order o "
         + "WHERE o.key = :orderKey and o.consumer.key = :consumerKey"
    )
    Optional<OrderEntity> findEntityByKeyAndConsumerKey(UUID consumerKey, UUID orderKey);

    /**
     * Find an order created by a specific consumer
     *
     *
     * @param consumerKey
     * @param orderKey
     * @return
     */
    default Optional<ConsumerOrderDto> findObjectByKeyAndConsumerKey(UUID consumerKey, UUID orderKey) {
        return this.findEntityByKeyAndConsumerKey(consumerKey, orderKey).map(o -> o.toConsumerDto(true, false));
    }

    /**
     * Find an order linked to a specific provider.
     *
     * Orders support only a single item. Provider orders are the ones that
     * reference an item with the same provider.
     *
     * This method does not return orders with status <b>CREATED</b>.
     *
     * @param providerKey
     * @param orderKey
     * @return
     */
    @Query("SELECT distinct i.order FROM OrderItem i "
         + "WHERE i.order.key = :orderKey and i.provider.key = :providerKey and i.order.status <> 'CREATED'"
    )
    Optional<OrderEntity> findOrderEntityByKeyAndProviderKey(UUID providerKey, UUID orderKey);

    @Query("SELECT  distinct o "
         + "FROM    Order o INNER JOIN o.items i "
         + "WHERE   (o.consumer.key = :consumerKey or cast(:consumerKey as org.hibernate.type.UUIDCharType) is null) and "
         + "        (i.provider.key = :providerKey or cast(:providerKey as org.hibernate.type.UUIDCharType) is null) and "
         + "        (:referenceNumber is null or o.referenceNumber like :referenceNumber) and "
         + "        (:consumer is null or o.consumer.email like :consumer) and "
         + "        (o.status in :status or :status is null)"
    )
    Page<OrderEntity> findAll(
        @Param("consumerKey") UUID consumerKey,
        @Param("providerKey") UUID providerKey,
        @Param("referenceNumber")String referenceNumber,
        @Param("status") Set<EnumOrderStatus> status,
        @Param("consumer") String consumer,
        Pageable pageable
    );

    /**
     * Query consumer orders
     *
     * This method does not return orders with status <b>CREATED</b>.
     *
     * @param consumerKey
     * @param referenceNumber
     * @param status
     * @param pageable
     * @return
     */
     @Query("SELECT  distinct o "
          + "FROM    Order o "
          + "WHERE   (o.consumer.key = :consumerKey) and "
          + "        (:referenceNumber is null or o.referenceNumber like :referenceNumber) and "
          + "        (o.status in :status or :status is null) and (o.status <> 'CREATED')"
     )
     Page<OrderEntity> findAllForConsumer(
         @Param("consumerKey") UUID consumerKey,
         @Param("referenceNumber")String referenceNumber,
         @Param("status") Set<EnumOrderStatus> status,
         Pageable pageable
     );

     /**
      * Query provider orders
      *
      * Orders support only a single item. Provider orders are the ones that
      * reference an item with the same provider.
      *
      * This method does not return orders with status <b>CREATED</b>.
      *
      * @param providerKey
      * @param referenceNumber
      * @param status
      * @param pageable
      * @return
      */
     @Query("SELECT  distinct o "
          + "FROM    Order o INNER JOIN o.items i "
          + "WHERE   (i.provider.key = :providerKey) and "
          + "        (:referenceNumber is null or o.referenceNumber like :referenceNumber) and "
          + "        (o.status in :status or :status is null) and (o.status <> 'CREATED')"
     )
     Page<OrderEntity> findAllForProvider(
         @Param("providerKey") UUID providerKey,
         @Param("referenceNumber")String referenceNumber,
         @Param("status") Set<EnumOrderStatus> status,
         Pageable pageable
     );

    @Query("SELECT p FROM PayIn p WHERE p.payIn = :payIn")
    Optional<PayInEntity> findPayInById(@Param("payIn") String payIn);

    default Page<OrderDto> findAllObjects(
        UUID consumerKey, UUID providerKey, String referenceNumber,
        Set<EnumOrderStatus> status, String consumer,
        Pageable pageable,
        boolean includeDetails, boolean includeHelpdeskData
    ) {
        return this.findAll(
            consumerKey,
            providerKey,
            StringUtils.isBlank(referenceNumber) ? null : referenceNumber,
            status != null && status.size() > 0 ? status : null,
            StringUtils.isBlank(consumer) ? null : consumer,     
            pageable
        ).map(e -> e.toHelpdeskDto(includeDetails));
    }

    default Page<ConsumerOrderDto> findAllObjectsForConsumer(
        UUID consumerKey, String referenceNumber, Set<EnumOrderStatus> status,
        Pageable pageable,
        boolean includeItemDetails, boolean includeProviderDetails
    ) {
        return this.findAllForConsumer(
            consumerKey,
            StringUtils.isBlank(referenceNumber) ? null : referenceNumber,
            status != null && status.size() > 0 ? status : null,
            pageable
        ).map(e -> e.toConsumerDto(includeItemDetails, includeProviderDetails));
    }

    default Page<ProviderOrderDto> findAllObjectsForProvider(
        UUID providerKey, String referenceNumber, Set<EnumOrderStatus> status,
        Pageable pageable,
        boolean includeDetails, boolean includeHelpdeskData
    ) {
        return this.findAllForProvider(
            providerKey,
            StringUtils.isBlank(referenceNumber) ? null : referenceNumber,
            status != null && status.size() > 0 ? status : null,
            pageable
        ).map(e -> e.toProviderDto(includeDetails));
    }

    default Optional<HelpdeskOrderDto> findOrderObjectByKey(UUID key) {
        return this.findOrderEntityByKey(key).map(o -> o.toHelpdeskDto(true));
    }

    default Optional<ConsumerOrderDto> findOrderObjectByKeyAndConsumer(UUID consumerKey, UUID orderKey) {
        return this.findEntityByKeyAndConsumerAndStatusNotCreated(consumerKey, orderKey).map(o -> o.toConsumerDto(true, true));
    }

    default Optional<ProviderOrderDto> findOrderObjectByKeyAndProvider(UUID providerKey, UUID orderKey) {
        return this.findOrderEntityByKeyAndProviderKey(providerKey, orderKey).map(o -> o.toProviderDto(true));
    }

    @Transactional(readOnly = false)
    default OrderDto create(OrderCommand command) throws Exception {
        final AccountEntity consumer = this.findAccountById(command.getUserId()).orElse(null);
        final AccountEntity provider = this.findAccountByKey(command.getAsset().getPublisherId()).orElse(null);
        final OrderEntity   order    = new OrderEntity();
        final String        country  = command.getLocation().getCountry();

        order.setCart(command.getCartId());
        order.setConsumer(consumer);
        order.setCountry(country);
        order.setCreatedOn(ZonedDateTime.now());
        order.setCurrency(command.getQuotation().getQuotation().getCurrency().toString());
        order.setDeliveryMethod(command.getDeliveryMethod());
        order.setStatusUpdatedOn(order.getCreatedOn());
        order.setTaxPercent(command.getQuotation().getQuotation().getTaxPercent());
        order.setTotalPrice(command.getQuotation().getQuotation().getTotalPrice());
        order.setTotalPriceExcludingTax(command.getQuotation().getQuotation().getTotalPriceExcludingTax());
        order.setTotalTax(command.getQuotation().getQuotation().getTax());
        order.setVettingRequired(command.isVettingRequired());

        // Set order status
        EnumOrderStatus statusValue = EnumOrderStatus.CREATED;
        if (command.isVettingRequired()) {
            statusValue = EnumOrderStatus.PENDING_PROVIDER_APPROVAL;
        } else if (command.isContractUploadingRequired()) {
            statusValue = EnumOrderStatus.PENDING_PROVIDER_CONTRACT_UPLOAD;
        }
        order.setStatus(statusValue);

        final OrderStatusEntity status = new OrderStatusEntity();
        status.setOrder(order);
        status.setStatus(order.getStatus());
        status.setStatusUpdatedBy(consumer);
        status.setStatusUpdatedOn(order.getCreatedOn());

        order.getStatusHistory().add(status);

        final OrderItemEntity item = new OrderItemEntity();
        item.setAssetId(command.getAsset().getId());
        item.setAssetVersion(command.getAsset().getVersion());
        item.setContractType(command.getContractType());
        item.setContractTemplateId(command.getContractType() == EnumContractType.MASTER_CONTRACT ? command.getAsset().getContractTemplateId() : null);
        item.setContractTemplateVersion(command.getContractType() == EnumContractType.MASTER_CONTRACT ? command.getAsset().getContractTemplateVersion() : null);
        item.setDescription(command.getAsset().getTitle());
        item.setIndex(1);
        item.setOrder(order);
        item.setPricingModel(command.getQuotation());
        item.setProvider(provider);
        item.setSegment(command.getAsset().getTopicCategory().stream().findFirst().orElse(null));
        item.setTotalPrice(command.getQuotation().getQuotation().getTotalPrice());
        item.setTotalPriceExcludingTax(command.getQuotation().getQuotation().getTotalPriceExcludingTax());
        item.setTotalTax(command.getQuotation().getQuotation().getTax());
        item.setType(command.getAsset().getType().getOrderItemType());

        order.getItems().add(item);

        this.saveAndFlush(order);

        // Compute reference number from database key
        order.setReferenceNumber(MangopayUtils.createReferenceNumber(EnumReferenceType.ORDER, order.getId()));

        this.saveAndFlush(order);

        return order.toConsumerDto(true, true);
    }

    @Transactional(readOnly = false)
    default void setPayIn(UUID orderKey, String payInProviderId) throws Exception {
        this.setPayIn(orderKey, payInProviderId, null);
    }

    @Transactional(readOnly = false)
    default void setPayIn(UUID orderKey, String payInProviderId, UUID accountKey) throws Exception {
        final AccountEntity account = this.findAccountByKey(accountKey).orElse(null);
        final OrderEntity order = this.findOrderEntityByKey(orderKey).orElse(null);
        final PayInEntity payIn = this.findPayInById(payInProviderId).orElse(null);

        Assert.notNull(order, "Expected a non-null order");
        Assert.notNull(payIn, "Expected a non-null payIn");
		Assert.isTrue(
				order.getStatus() == EnumOrderStatus.CREATED ||
				order.getStatus() == EnumOrderStatus.PROVIDER_ACCEPTED ||
				order.getStatus() == EnumOrderStatus.CONTRACT_IS_SIGNED,
				"Expected order status in [CREATED, PROVIDER_ACCEPTED, CONTRACT_IS_SIGNED]");
        // Update order
        order.setPayin(payIn);
        order.setReferenceNumber(payIn.getReferenceNumber());
        order.setStatus(EnumOrderStatus.CHARGED);

        // Update order status history
        final OrderStatusEntity status = new OrderStatusEntity();
        status.setOrder(order);
        status.setStatus(order.getStatus());
        status.setStatusUpdatedOn(payIn.getCreatedOn());
        status.setStatusUpdatedBy(account);

        order.getStatusHistory().add(status);
    }

    /**
     * Update order status
     *
     * This method is idempotent and will update an order only on status
     * changes.
     *
     * @param orderKey
     * @param status
     */
    @Transactional(readOnly = false)
    default void setStatus(UUID orderKey, EnumOrderStatus status) {
        final OrderEntity order = this.findOrderEntityByKey(orderKey).orElse(null);

        // Update only on status changes
        if (order.getStatus() == status) {
            return;
        }

        // Update order
        order.setStatus(status);

        // Update order status history
        final OrderStatusEntity history = new OrderStatusEntity();
        history.setOrder(order);
        history.setStatus(status);
        history.setStatusUpdatedOn(ZonedDateTime.now());

        order.getStatusHistory().add(history);
    }

    @Transactional(readOnly = false)
    default void setContractSignedDate(UUID orderKey, ZonedDateTime contractSignedOn) throws Exception {
        final OrderEntity order = this.findOrderEntityByKey(orderKey).orElse(null);

        Assert.notNull(order, "Expected a non-null order");
        Assert.isTrue(order.getItems().size() == 1, "Expected a single order item");

        final OrderItemEntity item = order.getItems().get(0);
        item.setContractSignedOn(contractSignedOn);

        this.saveAndFlush(order);
    }

    /**
     * Set provider's order acceptance
     *
     * @param providerKey
     * @param orderKey
     * @return
     * @throws Exception
     */
    @Transactional(readOnly = false)
    default ProviderOrderDto acceptOrderByProvider(UUID providerKey, UUID orderKey) throws Exception {
        final OrderEntity order = this.findOrderEntityByKeyAndProviderKey(providerKey, orderKey).orElse(null);

        if (order == null) {
            throw new OrderException(OrderMessageCode.ORDER_NOT_FOUND, "Order not found");
        }

        final OrderItemEntity orderItem = order.getItems().get(0);

        if (orderItem == null) {
            throw new OrderException(OrderMessageCode.ORDER_NOT_FOUND, "Order not found");
        }

        // Update only on status change
        if (order.getStatus() != EnumOrderStatus.PROVIDER_ACCEPTED) {
        	if (order.getStatus() != EnumOrderStatus.PENDING_PROVIDER_APPROVAL) {
                throw new OrderException(OrderMessageCode.ORDER_INVALID_STATUS, String.format(
                    "Invalid order status [expected=%s, found=%s]",
                    EnumOrderStatus.PENDING_PROVIDER_APPROVAL, order.getStatus()
                ));
            }

            // Update status and create history record
            if (orderItem.getContractType() == EnumContractType.MASTER_CONTRACT) {
            	this.setStatus(orderKey, EnumOrderStatus.PROVIDER_ACCEPTED);
            } else {
            	this.setStatus(orderKey, EnumOrderStatus.PENDING_PROVIDER_CONTRACT_UPLOAD);
            }

            this.saveAndFlush(order);
        }

        return order.toProviderDto(true);
    }

    @Transactional(readOnly = false)
    default ProviderOrderDto rejectOrderByProvider(UUID providerKey, UUID orderKey, String reason) throws Exception {
        final OrderEntity order = this.findOrderEntityByKeyAndProviderKey(providerKey, orderKey).orElse(null);

        if (order == null) {
            throw new OrderException(OrderMessageCode.ORDER_NOT_FOUND, "Order not found");
        }

        // Update only on status change
        if (order.getStatus() != EnumOrderStatus.PROVIDER_REJECTED) {
            if (order.getStatus() != EnumOrderStatus.PENDING_PROVIDER_APPROVAL) {
                throw new OrderException(OrderMessageCode.ORDER_INVALID_STATUS, String.format(
                    "Invalid order status [expected=%s, found=%s]",
                    EnumOrderStatus.PENDING_PROVIDER_APPROVAL, order.getStatus()
                ));
            }
            // Update status and create history record
            this.setStatus(orderKey, EnumOrderStatus.PROVIDER_REJECTED);

            order.setProviderRejectionReason(reason);

            this.saveAndFlush(order);
        }

        return order.toProviderDto(true);
    }

    @Transactional(readOnly = false)
    default ProviderOrderDto uploadContractByProvider(UUID providerKey, UUID orderKey, boolean updateStatus) throws OrderException {
        final OrderEntity order = this.findOrderEntityByKeyAndProviderKey(providerKey, orderKey).orElse(null);

        if (order == null) {
            throw new OrderException(OrderMessageCode.ORDER_NOT_FOUND, "Order not found");
        }

        final EnumOrderStatus oldStatus = EnumOrderStatus.PENDING_PROVIDER_CONTRACT_UPLOAD;
        final EnumOrderStatus newStatus = EnumOrderStatus.PENDING_CONSUMER_CONTRACT_ACCEPTANCE;

        // Check if contract upload is allowed
        if (order.getStatus() != oldStatus) {
            throw new OrderException(
                OrderMessageCode.ORDER_INVALID_STATUS,
                String.format("Invalid order status [expected=%s, found=%s]", oldStatus, order.getStatus())
            );
        }
        // Update status and create history record
        if (updateStatus) {
            this.setStatus(orderKey, newStatus);
            this.saveAndFlush(order);
        }

        return order.toProviderDto(true);
    }

    @Transactional(readOnly = false)
    default ConsumerOrderDto acceptContractByConsumer(UUID consumerKey, UUID orderKey) throws Exception {
        final OrderEntity order = this.findEntityByKeyAndConsumerAndStatusNotCreated(consumerKey, orderKey)
            .orElse(null);

        if (order == null) {
            throw new OrderException(OrderMessageCode.ORDER_NOT_FOUND, "Order not found");
        }

        final EnumOrderStatus oldStatus = EnumOrderStatus.PENDING_CONSUMER_CONTRACT_ACCEPTANCE;
        final EnumOrderStatus newStatus = EnumOrderStatus.CONTRACT_IS_SIGNED;

        // Check status
        if (order.getStatus() != oldStatus) {
            throw new OrderException(OrderMessageCode.ORDER_INVALID_STATUS, String.format(
                "Invalid order status [expected=%s, found=%s]",
                oldStatus, order.getStatus()
            ));
        }

        // Update status and create history record
        this.setStatus(orderKey, newStatus);

        this.saveAndFlush(order);

        return order.toConsumerDto(true, true);
    }

    @Transactional(readOnly = false)
    default ProviderOrderDto sendOrderByProvider(OrderShippingCommandDto command) throws Exception {
        final OrderEntity order = this.findOrderEntityByKeyAndProviderKey(command.getPublisherKey(), command.getOrderKey()).orElse(null);

        if (order == null) {
            throw new OrderException(OrderMessageCode.ORDER_NOT_FOUND, "Order not found");
        }

        final EnumOrderStatus oldStatus = EnumOrderStatus.PENDING_PROVIDER_SEND_CONFIRMATION;
        final EnumOrderStatus newStatus = EnumOrderStatus.PENDING_CONSUMER_RECEIVE_CONFIRMATION;

        // Update only on status change
        if (order.getStatus() != newStatus) {
            if (order.getStatus() != oldStatus) {
                throw new OrderException(OrderMessageCode.ORDER_INVALID_STATUS, String.format(
                    "Invalid order status [expected=%s, found=%s]",
                    oldStatus, order.getStatus()
                ));
            }
            // Update status and create history record
            this.setStatus(command.getOrderKey(), newStatus);
            // TODO: Update other order properties e.g. tracker URL

            this.saveAndFlush(order);
        }

        return order.toProviderDto(true);
    }

    @Transactional(readOnly = false)
    default ConsumerOrderDto receiveOrderByConsumer(OrderDeliveryCommand command) throws Exception {
        final OrderEntity order = this.findEntityByKeyAndConsumerAndStatusNotCreated(command.getConsumerKey(), command.getOrderKey())
            .orElse(null);

        if (order == null) {
            throw new OrderException(OrderMessageCode.ORDER_NOT_FOUND, "Order not found");
        }

        final EnumOrderStatus oldStatus = EnumOrderStatus.PENDING_CONSUMER_RECEIVE_CONFIRMATION;
        final EnumOrderStatus newStatus = EnumOrderStatus.ASSET_REGISTRATION;

        // Update only on status change
        if (order.getStatus() != newStatus) {
            if (order.getStatus() != oldStatus) {
                throw new OrderException(OrderMessageCode.ORDER_INVALID_STATUS, String.format(
                    "Invalid order status [expected=%s, found=%s]",
                    oldStatus, order.getStatus()
                ));
            }
            // Update status and create history record
            this.setStatus(command.getOrderKey(), newStatus);
            // TODO: Update other order properties e.g. delivery date

            this.saveAndFlush(order);
        }

        return order.toConsumerDto(true, true);
    }

}
