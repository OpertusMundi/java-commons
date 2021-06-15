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
import eu.opertusmundi.common.model.order.EnumOrderItemType;
import eu.opertusmundi.common.model.order.EnumOrderStatus;
import eu.opertusmundi.common.model.order.OrderCommand;
import eu.opertusmundi.common.model.order.OrderDto;
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

    @Query("SELECT p FROM PayIn p WHERE p.payIn = :payIn")
    Optional<PayInEntity> findPayInById(@Param("payIn") String payIn);

    @Query("SELECT o FROM Order o WHERE o.key = :key")
    Optional<OrderEntity> findOrderEntityByKey(@Param("key") UUID key);

    @Query("SELECT  o "
         + "FROM    Order o "
         + "WHERE   (:referenceNumber is null or o.referenceNumber like :referenceNumber) and"
         + "        (o.status in :status or :status is null)"
    )
    Page<OrderEntity> findAllEntities(
        @Param("referenceNumber")String referenceNumber,
        @Param("status") Set<EnumOrderStatus> status,
        Pageable pageable
    );

    default Page<OrderDto> findAllObjects(
        @Param("referenceNumber")String referenceNumber,
        @Param("status") Set<EnumOrderStatus> status,
        Pageable pageable
    ) {
        return this.findAllEntities(
            StringUtils.isBlank(referenceNumber) ? null : referenceNumber,
            status != null && status.size() > 0 ? status : null,
            pageable
        ).map(e -> e.toDto(false, true));
    }

    default Optional<OrderDto> findOrderObjectByKey(UUID key) {
        return this.findOrderEntityByKey(key).map(o -> o.toDto(true, true));
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
        item.setDescription(command.getAsset().getTitle());
        item.setIndex(1);
        item.setItem(command.getAsset().getId());
        item.setOrder(order);
        item.setPricingModel(command.getQuotation());
        item.setProvider(provider);
        item.setSegment(command.getAsset().getTopicCategory().stream().findFirst().orElse(null));
        item.setTotalPrice(command.getQuotation().getQuotation().getTotalPrice());
        item.setTotalPriceExcludingTax(command.getQuotation().getQuotation().getTotalPriceExcludingTax());
        item.setTotalTax(command.getQuotation().getQuotation().getTax());
        item.setVersion(command.getAsset().getVersion());
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

        return order.toDto();
    }

    @Transactional(readOnly = false)
    default void setPayIn(UUID orderKey, String payInProviderId) throws Exception {
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

        order.getStatusHistory().add(status);
    }

    @Transactional(readOnly = false)
    default void setStatus(UUID orderKey, EnumOrderStatus status, ZonedDateTime when) throws Exception {
        final OrderEntity order = this.findOrderEntityByKey(orderKey).orElse(null);

        // Update order
        order.setStatus(status);

        // Update order status history
        final OrderStatusEntity history = new OrderStatusEntity();
        history.setOrder(order);
        history.setStatus(status);
        history.setStatusUpdatedOn(when);

        order.getStatusHistory().add(history);
    }
}
