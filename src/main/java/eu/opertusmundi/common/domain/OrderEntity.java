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
import eu.opertusmundi.common.model.order.ConsumerOrderDto;
import eu.opertusmundi.common.model.order.EnumOrderStatus;
import eu.opertusmundi.common.model.order.HelpdeskOrderDto;
import eu.opertusmundi.common.model.order.OrderDto;
import eu.opertusmundi.common.model.order.ProviderOrderDto;
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

    @Column(name = "`vetting_required`")
    @Getter
    @Setter
    private boolean vettingRequired = false;

    @Column(name = "`provider_rejection_reason`")
    @Getter
    @Setter
    private String providerRejectionReason;

    private void updateDto(OrderDto o) {
        o.setCartId(cart);
        o.setCreatedOn(createdOn);
        o.setCurrency(currency);
        o.setDeliveryMethod(deliveryMethod);
        o.setId(id);
        if (this.getPayin() != null) {
            o.setInvoicePrinted(this.getPayin().getInvoicePrintedOn() != null);
            o.setInvoicePrintedOn(this.getPayin().getInvoicePrintedOn());
        }
        o.setKey(key);
        if (this.payin != null) {
            o.setPaymentMethod(this.payin.getPaymentMethod());
        }
        o.setProviderRejectionReason(providerRejectionReason);
        o.setReferenceNumber(referenceNumber);
        o.setStatus(status);
        o.setStatusUpdatedOn(statusUpdatedOn);
        o.setTotalPrice(totalPrice);
        o.setTotalPriceExcludingTax(totalPriceExcludingTax);
        o.setTotalTax(totalTax);
        o.setVettingRequired(vettingRequired);
    }

    public ConsumerOrderDto toConsumerDto(boolean includeItemDetails, boolean includeProviderDetails, boolean includePayIn) {
        final ConsumerOrderDto o = new ConsumerOrderDto();

        this.updateDto(o);

        if (includeItemDetails) {
            items.stream().map(i -> i.toConsumerDto(includeProviderDetails)).forEach(o::addItem);
        }

        return o;
    }

    public ProviderOrderDto toProviderDto(boolean includeDetails, boolean includePayIn) {
        final ProviderOrderDto o = new ProviderOrderDto();

        this.updateDto(o);

        o.setConsumer(consumer.getConsumer().toConsumerDto());

        if (includeDetails) {
            items.stream().map(OrderItemEntity::toProviderDto).forEach(o::addItem);
        }

        return o;
    }

    public HelpdeskOrderDto toHelpdeskDto(boolean includeDetails, boolean includePayIn) {
        final HelpdeskOrderDto o = new HelpdeskOrderDto();

        this.updateDto(o);

        o.setConsumer(consumer.getProfile().getConsumer().toDto(true));
        if (payin != null) {
            o.setPayIn(payin.toHelpdeskDto(false));
        }


        if (includeDetails) {
            items.stream().map(OrderItemEntity::toHelpdeskDto).forEach(o::addItem);
            statusHistory.stream().map(OrderStatusEntity::toDto).forEach(o::addStatusHistory);
        }

        return o;
    }
}
