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
import eu.opertusmundi.common.model.order.ConsumerOrderDto;
import eu.opertusmundi.common.model.order.EnumOrderItemType;
import eu.opertusmundi.common.model.order.EnumOrderStatus;
import eu.opertusmundi.common.model.order.HelpdeskOrderDto;
import eu.opertusmundi.common.model.order.OrderCommand;
import eu.opertusmundi.common.model.order.OrderDto;
import eu.opertusmundi.common.model.order.ProviderOrderDto;
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
     * Find a order created by a specific consumer
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
    Optional<OrderEntity> findOrderEntityByKeyAndConsumerKey(@Param("consumerKey") UUID consumerKey, @Param("orderKey") UUID orderKey);

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
    Optional<OrderEntity> findOrderEntityByKeyAndProviderKey(@Param("providerKey") UUID providerKey, @Param("orderKey") UUID orderKey);

    @Query("SELECT  distinct o "
         + "FROM    Order o INNER JOIN o.items i "
         + "WHERE   (o.consumer.key = :consumerKey or cast(:consumerKey as org.hibernate.type.UUIDCharType) is null) and "
         + "        (i.provider.key = :providerKey or cast(:providerKey as org.hibernate.type.UUIDCharType) is null) and "
         + "        (:referenceNumber is null or o.referenceNumber like :referenceNumber) and "
         + "        (o.status in :status or :status is null)"
    )
    Page<OrderEntity> findAll(
        @Param("consumerKey") UUID consumerKey,
        @Param("providerKey") UUID providerKey,
        @Param("referenceNumber")String referenceNumber,
        @Param("status") Set<EnumOrderStatus> status,
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
        @Param("status") Set<EnumOrderStatus> status,
        Pageable pageable,
        boolean includeDetails, boolean includeHelpdeskData
    ) {
        return this.findAll(
            consumerKey,
            providerKey,
            StringUtils.isBlank(referenceNumber) ? null : referenceNumber,
            status != null && status.size() > 0 ? status : null,
            pageable
        ).map(e -> e.toHelpdeskDto(includeDetails));
    }

    default Page<ConsumerOrderDto> findAllObjectsForConsumer(
        UUID consumerKey, String referenceNumber, Set<EnumOrderStatus> status,
        Pageable pageable,
        boolean includeDetails
    ) {
        return this.findAllForConsumer(
            consumerKey,
            StringUtils.isBlank(referenceNumber) ? null : referenceNumber,
            status != null && status.size() > 0 ? status : null,
            pageable
        ).map(e -> e.toConsumerDto(includeDetails));
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
        return this.findOrderEntityByKeyAndConsumerKey(consumerKey, orderKey).map(o -> o.toConsumerDto(true));
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
        order.setStatus(EnumOrderStatus.CREATED);
        order.setStatusUpdatedOn(order.getCreatedOn());
        order.setTaxPercent(command.getQuotation().getQuotation().getTaxPercent());
        order.setTotalPrice(command.getQuotation().getQuotation().getTotalPrice());
        order.setTotalPriceExcludingTax(command.getQuotation().getQuotation().getTotalPriceExcludingTax());
        order.setTotalTax(command.getQuotation().getQuotation().getTax());

        final OrderStatusEntity status = new OrderStatusEntity();
        status.setOrder(order);
        status.setStatus(order.getStatus());
        status.setStatusUpdatedBy(consumer);
        status.setStatusUpdatedOn(order.getCreatedOn());

        order.getStatusHistory().add(status);

        final OrderItemEntity item = new OrderItemEntity();
        item.setAssetId(command.getAsset().getId());
        item.setAssetVersion(command.getAsset().getVersion());
        item.setContractTemplateId(command.getAsset().getContractTemplateId());
        item.setContractTemplateVersion(command.getAsset().getContractTemplateVersion());
        item.setDescription(command.getAsset().getTitle());
        item.setIndex(1);
        item.setOrder(order);
        item.setPricingModel(command.getQuotation());
        item.setProvider(provider);
        item.setSegment(command.getAsset().getTopicCategory().stream().findFirst().orElse(null));
        item.setTotalPrice(command.getQuotation().getQuotation().getTotalPrice());
        item.setTotalPriceExcludingTax(command.getQuotation().getQuotation().getTotalPriceExcludingTax());
        item.setTotalTax(command.getQuotation().getQuotation().getTax());
        switch (command.getAsset().getType()) {
            case VECTOR :
            case RASTER :
                item.setType(EnumOrderItemType.ASSET);
                break;
            case SERVICE :
                item.setType(EnumOrderItemType.SERVICE);
                break;
            default :
                throw new Exception(String.format("Asset type [%s] is not supported", command.getAsset().getType()));
        }

        order.getItems().add(item);

        this.saveAndFlush(order);

        return order.toConsumerDto(true);
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
        Assert.isTrue(order.getStatus() == EnumOrderStatus.CREATED, "Expected order status CREATED");

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
     * @param when
     * @throws Exception
     */
    @Transactional(readOnly = false)
    default void setStatus(UUID orderKey, EnumOrderStatus status, ZonedDateTime when) throws Exception {
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
        history.setStatusUpdatedOn(when);

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

}
