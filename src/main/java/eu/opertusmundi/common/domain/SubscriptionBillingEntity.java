package eu.opertusmundi.common.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;

import eu.opertusmundi.common.model.account.EnumSubscriptionBillingStatus;
import eu.opertusmundi.common.model.payment.ServiceUseStatsDto;
import eu.opertusmundi.common.model.payment.SubscriptionBillingDto;
import eu.opertusmundi.common.model.payment.consumer.ConsumerSubscriptionBillingDto;
import eu.opertusmundi.common.model.payment.helpdesk.HelpdeskSubscriptionBillingDto;
import eu.opertusmundi.common.model.payment.provider.ProviderSubscriptionBillingDto;
import eu.opertusmundi.common.model.pricing.BasePricingModelCommandDto;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity(name = "SubscriptionBilling")
@Table(schema = "billing", name = "`subscription_billing`")
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class SubscriptionBillingEntity {

    @Id
    @Column(name = "`id`", updatable = false)
    @SequenceGenerator(sequenceName = "billing.subscription_billing_id_seq", name = "subscription_billing_id_seq", allocationSize = 1)
    @GeneratedValue(generator = "subscription_billing_id_seq", strategy = GenerationType.SEQUENCE)
    @Setter(AccessLevel.PRIVATE)
    private Integer id;

    @Builder.Default
    @NotNull
    @NaturalId
    @Column(name = "key", updatable = false, columnDefinition = "uuid")
    @Setter(AccessLevel.PRIVATE)
    private UUID key = UUID.randomUUID();

    @NotNull
    @ManyToOne(targetEntity = AccountSubscriptionEntity.class)
    @JoinColumn(name = "subscription", nullable = false)
    private AccountSubscriptionEntity subscription;

    @ManyToOne(targetEntity = PayInEntity.class)
    @JoinColumn(name = "payin")
    private PayInEntity payin;

    @NotNull
    @Column(name = "`created_on`")
    private ZonedDateTime createdOn;

    @NotNull
    @Column(name = "`updated_on`")
    private ZonedDateTime updatedOn;

    @NotNull
    @Column(name = "`from_date`")
    private LocalDate fromDate;

    @NotNull
    @Column(name = "`to_date`")
    private LocalDate toDate;

    @NotNull
    @Column(name = "`due_date`")
    private LocalDate dueDate;

    @NotNull
    @Column(name = "`total_rows`")
    private Integer totalRows;

    @NotNull
    @Column(name = "`total_calls`")
    private Integer totalCalls;

    @NotNull
    @Column(name = "`sku_total_rows`")
    private Integer skuTotalRows;

    @NotNull
    @Column(name = "`sku_total_calls`")
    private Integer skuTotalCalls;

    @NotNull
    @Column(name = "`total_price`", columnDefinition = "numeric", precision = 20, scale = 6)
    private BigDecimal totalPrice;

    @NotNull
    @Column(name = "`total_price_excluding_tax`", columnDefinition = "numeric", precision = 20, scale = 6)
    private BigDecimal totalPriceExcludingTax;

    @NotNull
    @Column(name = "`total_tax`", columnDefinition = "numeric", precision = 20, scale = 6)
    private BigDecimal totalTax;

    @NotNull
    @Type(type = "jsonb")
    @Column(name = "`pricing_model`", columnDefinition = "jsonb")
    private BasePricingModelCommandDto pricingModel;

    @NotNull
    @Type(type = "jsonb")
    @Column(name = "`stats`", columnDefinition = "jsonb")
    private ServiceUseStatsDto stats;

    @NotNull
    @Column(name = "`status`")
    @Enumerated(EnumType.STRING)
    private EnumSubscriptionBillingStatus status;

    private void updateDto(SubscriptionBillingDto s) {
        s.setConsumerKey(this.getSubscription().getConsumer().getKey());
        s.setCreatedOn(createdOn);
        s.setDueDate(dueDate);
        s.setFromDate(fromDate);
        s.setId(id);
        s.setKey(key);
        s.setPricingModel(pricingModel);
        s.setProviderKey(subscription.getProvider().getKey());
        s.setSubscriptionId(this.getSubscription().getId());
        s.setSkuTotalCalls(skuTotalCalls);
        s.setSkuTotalRows(skuTotalRows);
        s.setStats(stats);
        s.setStatus(status);
        s.setSubscriptionKey(subscription.getKey());
        s.setToDate(toDate);
        s.setTotalCalls(totalCalls);
        s.setTotalPrice(totalPrice);
        s.setTotalPriceExcludingTax(totalPriceExcludingTax);
        s.setTotalRows(totalRows);
        s.setTotalTax(totalTax);
        s.setUpdatedOn(updatedOn);
    }

    public ConsumerSubscriptionBillingDto toConsumerDto(boolean includeProviderDetails) {
        final ConsumerSubscriptionBillingDto s = new ConsumerSubscriptionBillingDto();
        this.updateDto(s);

        if (includeProviderDetails) {
            s.setSubscription(this.subscription.toConsumerDto(includeProviderDetails));
            if (this.payin != null) {
                s.setPayIn(payin.toConsumerDto(false));
            }
        }
        return s;
    }

    public ProviderSubscriptionBillingDto toProviderDto(boolean includeDetails) {
        final ProviderSubscriptionBillingDto s = new ProviderSubscriptionBillingDto();
        this.updateDto(s);

        if (includeDetails) {
            s.setSubscription(this.subscription.toProviderDto());
            if (this.payin != null) {
                s.setPayIn(payin.toProviderDto(false));
            }
        }
        return s;
    }

    public HelpdeskSubscriptionBillingDto toHelpdeskDto() {
        return this.toHelpdeskDto(false);
    }

    public HelpdeskSubscriptionBillingDto toHelpdeskDto(boolean includeDetails) {
        final HelpdeskSubscriptionBillingDto s = new HelpdeskSubscriptionBillingDto();
        this.updateDto(s);

        if (includeDetails) {
            s.setSubscription(this.subscription.toHelpdeskDto());
            if (this.getPayin() != null) {
                s.setPayIn(this.getPayin().toProviderDto(false));
            }
        }
        return s;
    }

}