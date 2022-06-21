package eu.opertusmundi.common.domain;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import eu.opertusmundi.common.model.payment.SubscriptionBillingDto;
import eu.opertusmundi.common.model.payment.consumer.ConsumerSubscriptionBillingDto;
import eu.opertusmundi.common.model.payment.helpdesk.HelpdeskSubscriptionBillingDto;
import eu.opertusmundi.common.model.payment.provider.ProviderSubscriptionBillingDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity(name = "SubscriptionBilling")
@Table(schema = "billing", name = "`subscription_billing`")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SubscriptionBillingEntity {

    @Id
    @Column(name = "`id`", updatable = false)
    @SequenceGenerator(sequenceName = "billing.subscription_billing_id_seq", name = "subscription_billing_id_seq", allocationSize = 1)
    @GeneratedValue(generator = "subscription_billing_id_seq", strategy = GenerationType.SEQUENCE)
    @Getter
    private Integer id;

    @NotNull
    @ManyToOne(targetEntity = AccountSubscriptionEntity.class)
    @JoinColumn(name = "subscription", nullable = false)
    @Getter
    @Setter
    private AccountSubscriptionEntity subscription;

    @NotNull
    @Column(name = "`from_date`")
    @Getter
    @Setter
    private ZonedDateTime fromDate;

    @NotNull
    @Column(name = "`to_date`")
    @Getter
    @Setter
    private ZonedDateTime toDate;

    @NotNull
    @Column(name = "`total_rows`")
    @Getter
    @Setter
    private Integer totalRows;

    @NotNull
    @Column(name = "`total_calls`")
    @Getter
    @Setter
    private Integer totalCalls;

    @NotNull
    @Column(name = "`sku_total_rows`")
    @Getter
    @Setter
    private Integer skuTotalRows;

    @NotNull
    @Column(name = "`sku_total_calls`")
    @Getter
    @Setter
    private Integer skuTotalCalls;

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

    private void updateDto(SubscriptionBillingDto s) {
        s.setFromDate(fromDate);
        s.setId(id);
        s.setSubscriptionId(this.getSubscription().getId());
        s.setService(this.getSubscription().getAsset());
        s.setSkuTotalCalls(skuTotalCalls);
        s.setSkuTotalRows(skuTotalRows);
        s.setToDate(toDate);
        s.setTotalCalls(totalCalls);
        s.setTotalPrice(totalPrice);
        s.setTotalPriceExcludingTax(totalPriceExcludingTax);
        s.setTotalRows(totalRows);
        s.setTotalTax(totalTax);
    }

    public ConsumerSubscriptionBillingDto toConsumerDto(boolean includeProviderDetails) {
        final ConsumerSubscriptionBillingDto s = new ConsumerSubscriptionBillingDto();

        this.updateDto(s);

        s.setSubscription(this.getSubscription().toConsumerDto(includeProviderDetails));

        return s;
    }

    public ProviderSubscriptionBillingDto toProviderDto(boolean includeDetails) {
        final ProviderSubscriptionBillingDto s = new ProviderSubscriptionBillingDto();

        this.updateDto(s);

        s.setSubscription(this.getSubscription().toProviderDto());

        return s;
    }

    public HelpdeskSubscriptionBillingDto toHelpdeskDto(boolean includeDetails) {
        final HelpdeskSubscriptionBillingDto s = new HelpdeskSubscriptionBillingDto();

        this.updateDto(s);

        s.setSubscription(this.getSubscription().toHelpdeskDto());

        return s;
    }

}