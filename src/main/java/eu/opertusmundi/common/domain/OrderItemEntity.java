package eu.opertusmundi.common.domain;

import java.math.BigDecimal;

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
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.TypeDef;
import org.hibernate.validator.constraints.Length;

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;

import eu.opertusmundi.common.model.order.EnumOrderItemType;
import eu.opertusmundi.common.model.order.OrderItemDto;
import eu.opertusmundi.common.model.pricing.EffectivePricingModelDto;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "OrderItem")
@Table(schema = "order", name = "`order_item`")
@TypeDef(
    typeClass      = JsonBinaryType.class,
    defaultForType = EffectivePricingModelDto.class
)
public class OrderItemEntity {

    @Id
    @Column(name = "`id`", updatable = false)
    @SequenceGenerator(sequenceName = "order.order_item_id_seq", name = "order_item_id_seq", allocationSize = 1)
    @GeneratedValue(generator = "order_item_id_seq", strategy = GenerationType.SEQUENCE)
    @Getter
    private Integer id;

    @NotNull
    @ManyToOne(targetEntity = OrderEntity.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "`order`", nullable = false)
    @Getter
    @Setter
    private OrderEntity order;

    @NotNull
    @Column(name = "`index`")
    @Getter
    @Setter
    private Integer index;

    @NotNull
    @Column(name = "`type`")
    @Enumerated(EnumType.STRING)
    @Getter
    @Setter
    private EnumOrderItemType type;

    @NotNull
    @Column(name = "`item`")
    @Getter
    @Setter
    private String item;

    @NotNull
    @Column(name = "`description`")
    @Getter
    @Setter
    private String description;

    @NotNull
    @Column(name = "`pricing_model`", columnDefinition = "jsonb")
    @Getter
    @Setter
    private EffectivePricingModelDto pricingModel;

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
    
    @Length(max = 64)
    @Column(name = "`discount_code`", length = 64)
    @Getter
    @Setter
    private String discountCode;

    public OrderItemDto toDto() {
        final OrderItemDto i = new OrderItemDto();

        i.setDescription(description);
        i.setDiscountCode(discountCode);
        i.setId(id);
        i.setIndex(index);
        i.setItem(item);
        i.setOrderId(order.getId());
        i.setPricingModel(pricingModel);
        i.setTotalPrice(totalPrice);
        i.setTotalPriceExcludingTax(totalPriceExcludingTax);
        i.setTotalTax(totalTax);
        i.setType(type);

        return i;
    }

}
