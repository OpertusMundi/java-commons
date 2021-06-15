package eu.opertusmundi.common.domain;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.NaturalId;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import eu.opertusmundi.common.model.catalogue.client.EnumDeliveryMethod;
import eu.opertusmundi.common.model.order.EnumOrderStatus;
import eu.opertusmundi.common.model.order.OrderDto;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "Order")
@Table(schema = "`order`", name = "`order`", uniqueConstraints = {
    @UniqueConstraint(name = "uq_order_key", columnNames = {"`key`"}),
})
public class OrderEntity {

    @Id
    @Column(name = "`id`", updatable = false)
    @SequenceGenerator(sequenceName = "`order.order_id_seq`", name = "order_id_seq", allocationSize = 1)
    @GeneratedValue(generator = "order_id_seq", strategy = GenerationType.SEQUENCE)
    @Getter
    private Integer id;

    @NotNull
    @NaturalId
    @Column(name = "key", updatable = false, columnDefinition = "uuid")
    @Getter
    private final UUID key = UUID.randomUUID();

    @NotNull
    @ManyToOne(targetEntity = AccountEntity.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "consumer", nullable = false)
    @Getter
    @Setter
    private AccountEntity consumer;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payin", nullable = false)
    @Getter
    @Setter
    private PayInEntity payin;

    @OneToMany(
        mappedBy = "order",
        fetch = FetchType.LAZY,
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    @Getter
    @Setter
    @JsonInclude(Include.NON_NULL)
    private List<OrderItemEntity> items = new ArrayList<>();

    @OneToMany(
        mappedBy = "order",
        fetch = FetchType.LAZY,
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    @Getter
    @Setter
    private List<OrderStatusEntity> statusHistory = new ArrayList<>();

    /**
     * Reference to the cart instance used during the checkout operation
     */
    @NotNull
    @Column(name = "`cart`")
    @Getter
    @Setter
    private Integer cart;

    @NotNull
    @Column(name = "`total_price`", columnDefinition = "numeric", precision = 20, scale = 6)
    @Getter
    @Setter
    private BigDecimal totalPrice;

    @NotNull
    @Column(name = "`total_price_excluding_tax`", columnDefinition = "numeric", precision = 20, scale = 6)
    @Getter
    @Setter
    private BigDecimal totalPriceExcludingTax;

    @NotNull
    @Column(name = "`total_tax`", columnDefinition = "numeric", precision = 20, scale = 6)
    @Getter
    @Setter
    private BigDecimal totalTax;

    @NotNull
    @Column(name = "`tax_percent`")
    @Getter
    @Setter
    private Integer taxPercent;

    @NotNull
    @Column(name = "`currency`")
    @Getter
    @Setter
    private String currency;

    @Column(name = "`country`")
    @Getter
    @Setter
    private String country;

    @NotNull
    @Column(name = "`created_on`")
    @Getter
    @Setter
    private ZonedDateTime createdOn;

    @NotNull
    @Column(name = "`status`")
    @Enumerated(EnumType.STRING)
    @Getter
    @Setter
    private EnumOrderStatus status;

    @NotNull
    @Column(name = "`status_updated_on`")
    @Getter
    @Setter
    private ZonedDateTime statusUpdatedOn;

    @NotNull
    @Column(name = "`delivery_method`")
    @Enumerated(EnumType.STRING)
    @Getter
    @Setter
    private EnumDeliveryMethod deliveryMethod;

    @Column(name = "`reference_number`")
    @Getter
    @Setter
    private String referenceNumber;

    public OrderDto toDto() {
        return this.toDto(true, false);
    }
    
    public OrderDto toDto(boolean includeItems, boolean includeHelpdeskData) {
        final OrderDto o = new OrderDto();

        o.setCartId(cart);
        o.setCreatedOn(createdOn);
        o.setCurrency(currency);
        o.setDeliveryMethod(deliveryMethod);
        o.setId(id);
        if (this.payin != null) {
            o.setPaymentMethod(this.payin.getPaymentMethod());
        }
        o.setKey(key);
        o.setReferenceNumber(referenceNumber);
        o.setStatus(status);
        o.setStatusUpdatedOn(statusUpdatedOn);
        o.setTotalPrice(totalPrice);
        o.setTotalPriceExcludingTax(totalPriceExcludingTax);
        o.setTotalTax(totalTax);

        if (includeItems) {
            items.stream().map(OrderItemEntity::toDto).forEach(o::addItem);
        }
        if (includeHelpdeskData) {
            o.setCustomer(consumer.getProfile().getConsumer().toDto());
            o.setPayIn(payin.toDto(false, true));

            statusHistory.stream().map(OrderStatusEntity::toDto).forEach(o::addStatusHistory);
        }

        return o;
    }
}
