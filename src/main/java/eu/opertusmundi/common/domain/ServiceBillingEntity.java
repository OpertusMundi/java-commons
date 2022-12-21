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

import eu.opertusmundi.common.model.account.EnumPayoffStatus;
import eu.opertusmundi.common.model.payment.EnumBillableServiceType;
import eu.opertusmundi.common.model.payment.ServiceBillingDto;
import eu.opertusmundi.common.model.payment.ServiceUseStatsDto;
import eu.opertusmundi.common.model.payment.consumer.ConsumerServiceBillingDto;
import eu.opertusmundi.common.model.payment.helpdesk.HelpdeskServiceBillingDto;
import eu.opertusmundi.common.model.payment.provider.ProviderServiceBillingDto;
import eu.opertusmundi.common.model.pricing.BasePricingModelCommandDto;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity(name = "ServiceBilling")
@Table(schema = "billing", name = "`service_billing`")
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class ServiceBillingEntity {

    @Id
    @Column(name = "`id`", updatable = false)
    @SequenceGenerator(sequenceName = "billing.service_billing_id_seq", name = "service_billing_id_seq", allocationSize = 1)
    @GeneratedValue(generator = "service_billing_id_seq", strategy = GenerationType.SEQUENCE)
    @Setter(AccessLevel.PRIVATE)
    private Integer id;

    @Builder.Default
    @NotNull
    @NaturalId
    @Column(name = "key", updatable = false, columnDefinition = "uuid")
    @Setter(AccessLevel.PRIVATE)
    private UUID key = UUID.randomUUID();

    @NotNull
    @Column(name = "`type`")
    @Enumerated(EnumType.STRING)
    private EnumBillableServiceType type;

    @NotNull
    @ManyToOne(targetEntity = AccountEntity.class)
    @JoinColumn(name = "`billed_account`", nullable = false)
    private AccountEntity billedAccount;
    
    @ManyToOne(targetEntity = AccountSubscriptionEntity.class)
    @JoinColumn(name = "`subscription`", nullable = false)
    private AccountSubscriptionEntity subscription;

    @ManyToOne(targetEntity = UserServiceEntity.class)
    @JoinColumn(name = "`user_service`", nullable = false)
    private UserServiceEntity userService;

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
    private EnumPayoffStatus status;

    @Column(name = "transfer_provider_id")
    private String transferProviderId;

    @Column(name = "transfer_executed_on")
    private ZonedDateTime transferExecutedOn;

    @Column(name = "transfer_year")
    private Integer transferYear;

    @Column(name = "transfer_month")
    private Integer transferMonth;

    @Column(name = "transfer_week")
    private Integer transferWeek;

    @Column(name = "transfer_day")
    private Integer transferDay;

    @Column(name = "`transfer_credited_funds`", columnDefinition = "numeric", precision = 20, scale = 6)
    private BigDecimal transferCreditedFunds;

    @Column(name = "`transfer_platform_fees`", columnDefinition = "numeric", precision = 20, scale = 6)
    private BigDecimal transferPlatformFees;
    
    private void updateDto(ServiceBillingDto s) {
        s.setCreatedOn(createdOn);
        s.setDueDate(dueDate);
        s.setFromDate(fromDate);
        s.setId(id);
        s.setKey(key);
        s.setPricingModel(pricingModel);
        s.setSkuTotalCalls(skuTotalCalls);
        s.setSkuTotalRows(skuTotalRows);
        s.setStats(stats);
        s.setStatus(status);
        s.setToDate(toDate);
        s.setTotalCalls(totalCalls);
        s.setTotalPrice(totalPrice);
        s.setTotalPriceExcludingTax(totalPriceExcludingTax);
        s.setTotalRows(totalRows);
        s.setTotalTax(totalTax);
        s.setType(type);
        s.setUpdatedOn(updatedOn);

        if (this.getUserService() != null) {
            s.setProviderKey(this.getUserService().getAccount().getKey());
            s.setProviderParentKey(this.getUserService().getAccount().getParentKey());
            s.setServiceKey(this.getUserService().getKey());
            s.setUserServiceId(this.getUserService().getId());
        }
        if (this.getSubscription() != null) {
            s.setConsumerKey(this.getSubscription().getConsumer().getKey());
            s.setProviderKey(this.getSubscription().getProvider().getKey());
            s.setProviderParentKey(this.getSubscription().getProvider().getParentKey());
            s.setServiceKey(this.getSubscription().getKey());
            s.setSubscriptionId(this.getSubscription().getId());
        }
    }

    public ConsumerServiceBillingDto toConsumerDto(boolean includeDetails) {
        final ConsumerServiceBillingDto s = new ConsumerServiceBillingDto();
        this.updateDto(s);

        if (includeDetails) {
            if (this.subscription != null) {
                s.setSubscription(this.subscription.toConsumerDto(includeDetails));
            }
            if (this.userService != null) {
                s.setUserService(this.getUserService().toDto(false));
            }
            if (this.payin != null) {
                s.setPayIn(payin.toConsumerDto(false));
            }
        }
        return s;
    }

    public ProviderServiceBillingDto toProviderDto(boolean includeDetails) {
        final ProviderServiceBillingDto s = new ProviderServiceBillingDto();
        this.updateDto(s);

        if (includeDetails) {
            if (this.subscription != null) {
                s.setSubscription(this.subscription.toProviderDto());
            }
            if (this.userService != null) {
                s.setUserService(this.getUserService().toDto(false));
            }
            if (this.payin != null) {
                s.setPayIn(payin.toProviderDto(false));
            }
        }
        return s;
    }

    public HelpdeskServiceBillingDto toHelpdeskDto() {
        return this.toHelpdeskDto(false);
    }

    public HelpdeskServiceBillingDto toHelpdeskDto(boolean includeDetails) {
        final HelpdeskServiceBillingDto s = new HelpdeskServiceBillingDto();
        this.updateDto(s);

        if (includeDetails) {
            if (this.subscription != null) {
                s.setSubscription(this.subscription.toHelpdeskDto());
            }
            if (this.userService != null) {
                s.setUserService(this.getUserService().toDto(includeDetails));
            }
            if (this.getPayin() != null) {
                s.setPayIn(this.getPayin().toProviderDto(includeDetails));
            }
        }
        return s;
    }

}