package eu.opertusmundi.common.domain;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

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
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.TypeDef;
import org.hibernate.validator.constraints.Length;

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;

import eu.opertusmundi.common.model.catalogue.client.EnumContractType;
import eu.opertusmundi.common.model.catalogue.client.EnumTopicCategory;
import eu.opertusmundi.common.model.order.ConsumerOrderItemDto;
import eu.opertusmundi.common.model.order.EnumOrderItemType;
import eu.opertusmundi.common.model.order.HelpdeskOrderItemDto;
import eu.opertusmundi.common.model.order.OrderItemDto;
import eu.opertusmundi.common.model.order.ProviderOrderItemDto;
import eu.opertusmundi.common.model.pricing.EffectivePricingModelDto;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "OrderItem")
@Table(schema = "`order`", name = "`order_item`")
@TypeDef(
    typeClass      = JsonBinaryType.class,
    defaultForType = EffectivePricingModelDto.class
)
public class OrderItemEntity {

    @Id
    @Column(name = "`id`", updatable = false)
    @SequenceGenerator(sequenceName = "`order.order_item_id_seq`", name = "order_item_id_seq", allocationSize = 1)
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
    @ManyToOne(targetEntity = AccountEntity.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "provider", nullable = false)
    @Getter
    @Setter
    private AccountEntity provider;

    @NotNull
    @Column(name = "`type`")
    @Enumerated(EnumType.STRING)
    @Getter
    @Setter
    private EnumOrderItemType type;

    @Column(name = "`segment`")
    @Enumerated(EnumType.STRING)
    @Getter
    @Setter
    private EnumTopicCategory segment;

    @NotNull
    @Column(name = "`asset_pid`")
    @Getter
    @Setter
    private String assetId;

    @NotNull
    @Column(name = "`asset_version`")
    @Getter
    @Setter
    private String assetVersion;

    @NotNull
    @Column(name = "`contract_type`")
    @Enumerated(EnumType.STRING)
    @Getter
    @Setter
    private EnumContractType contractType;
    
    @Column(name = "`contract_template_id`")
    @Getter
    @Setter
    private Integer contractTemplateId;

    @Column(name = "`contract_template_version`")
    @Getter
    @Setter
    private String contractTemplateVersion;

    @Column(name = "`contract_signed_on`")
    @Getter
    @Setter
    private ZonedDateTime contractSignedOn;

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

    @Transient
    public boolean signed() {
        return this.contractSignedOn != null;
    }

    public void updateDto(OrderItemDto i) {
        i.setAssetId(assetId);
        i.setAssetVersion(assetVersion);
        i.setContractSignedOn(contractSignedOn);
        i.setDescription(description);
        i.setDiscountCode(discountCode);
        i.setId(id);
        i.setIndex(index);
        i.setOrderId(order.getId());
        i.setPricingModel(pricingModel);
        i.setTotalPrice(totalPrice);
        i.setTotalPriceExcludingTax(totalPriceExcludingTax);
        i.setTotalTax(totalTax);
        i.setType(type);
        i.setContractType(contractType);
    }

    public ConsumerOrderItemDto toConsumerDto(boolean includeProviderDetails) {
        final ConsumerOrderItemDto i = new ConsumerOrderItemDto();

        this.updateDto(i);

        i.setProvider(this.provider.getProvider().toProviderDto(includeProviderDetails));

        return i;
    }

    public ProviderOrderItemDto toProviderDto() {
        final ProviderOrderItemDto i = new ProviderOrderItemDto();

        this.updateDto(i);

        i.setContractTemplateId(contractTemplateId);
        i.setContractTemplateVersion(contractTemplateVersion);

        return i;
    }

    public HelpdeskOrderItemDto toHelpdeskDto() {
        final HelpdeskOrderItemDto i = new HelpdeskOrderItemDto();

        this.updateDto(i);

        i.setContractTemplateId(contractTemplateId);
        i.setContractTemplateVersion(contractTemplateVersion);
        i.setProvider(this.provider.getProvider().toDto());

        return i;
    }

}
