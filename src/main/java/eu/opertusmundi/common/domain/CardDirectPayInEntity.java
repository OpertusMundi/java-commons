package eu.opertusmundi.common.domain;

import java.util.stream.Collectors;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;

import eu.opertusmundi.common.model.payment.EnumPaymentMethod;
import eu.opertusmundi.common.model.payment.EnumRecurringPaymentType;
import eu.opertusmundi.common.model.payment.PayInDto;
import eu.opertusmundi.common.model.payment.consumer.ConsumerCardDirectPayInDto;
import eu.opertusmundi.common.model.payment.consumer.ConsumerPayInDto;
import eu.opertusmundi.common.model.payment.helpdesk.HelpdeskCardDirectPayInDto;
import eu.opertusmundi.common.model.payment.helpdesk.HelpdeskPayInDto;
import eu.opertusmundi.common.model.payment.provider.ProviderCardDirectPayInDto;
import eu.opertusmundi.common.model.payment.provider.ProviderPayInDto;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "CardDirectPayIn")
@Table(schema = "billing", name = "`payin_card_direct`")
@DiscriminatorValue(value = "CARD_DIRECT")
public class CardDirectPayInEntity extends PayInEntity {

    public CardDirectPayInEntity() {
        super(EnumPaymentMethod.CARD_DIRECT);
    }

    @Column(name = "`alias`")
    @Getter
    @Setter
    private String alias;

    @NotEmpty
    @Column(name = "`card`")
    @Getter
    @Setter
    private String card;

    @Length(max = 10)
    @NotEmpty
    @Column(name = "`statement_descriptor`", length = 10)
    @Getter
    @Setter
    private String statementDescriptor;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "firstName",  column = @Column(name = "`billing_first_name`")),
        @AttributeOverride(name = "lastName",   column = @Column(name = "`billing_last_name`")),
        @AttributeOverride(name = "line1",      column = @Column(name = "`billing_address_line_1`")),
        @AttributeOverride(name = "line2",      column = @Column(name = "`billing_address_line_2`")),
        @AttributeOverride(name = "city",       column = @Column(name = "`billing_address_city`")),
        @AttributeOverride(name = "region",     column = @Column(name = "`billing_address_region`")),
        @AttributeOverride(name = "postalCode", column = @Column(name = "`billing_address_postal_code`")),
        @AttributeOverride(name = "country",    column = @Column(name = "`billing_address_country`")),
    })
    @Getter
    @Setter
    private BillingAddressEmbeddable billingAddress;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "firstName",  column = @Column(name = "`shipping_first_name`")),
        @AttributeOverride(name = "lastName",   column = @Column(name = "`shipping_last_name`")),
        @AttributeOverride(name = "line1",      column = @Column(name = "`shipping_address_line_1`")),
        @AttributeOverride(name = "line2",      column = @Column(name = "`shipping_address_line_2`")),
        @AttributeOverride(name = "city",       column = @Column(name = "`shipping_address_city`")),
        @AttributeOverride(name = "region",     column = @Column(name = "`shipping_address_region`")),
        @AttributeOverride(name = "postalCode", column = @Column(name = "`shipping_address_postal_code`")),
        @AttributeOverride(name = "country",    column = @Column(name = "`shipping_address_country`")),
    })
    @Getter
    @Setter
    private ShippingAddressEmbeddable shippingAddress;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "acceptHeader",   column = @Column(name = "`browser_info_accept_header`")),
        @AttributeOverride(name = "colorDepth",     column = @Column(name = "`browser_info_color_depth`")),
        @AttributeOverride(name = "javaEnabled",    column = @Column(name = "`browser_info_java_enabled`")),
        @AttributeOverride(name = "language",       column = @Column(name = "`browser_info_language`")),
        @AttributeOverride(name = "screenHeight",   column = @Column(name = "`browser_info_screen_height`")),
        @AttributeOverride(name = "screenWidth",    column = @Column(name = "`browser_info_screen_width`")),
        @AttributeOverride(name = "timeZoneOffset", column = @Column(name = "`browser_info_time_zone_offset`")),
        @AttributeOverride(name = "userAgent",      column = @Column(name = "`browser_info_user_agent`")),
    })
    @Getter
    @Setter
    private BrowserInfoEmbeddable browserInfo;

    @Column(name = "`requested_3ds_version`")
    @Getter
    @Setter
    private String requested3dsVersion;

    @Column(name = "`applied_3ds_version`")
    @Getter
    @Setter
    private String applied3dsVersion;

    @NotEmpty
    @Column(name = "`ip_address`")
    @Getter
    @Setter
    private String ipAddress;

    /**
     * Reference to a recurring payment registration
     */
    @ManyToOne(targetEntity = PayInRecurringRegistrationEntity.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "recurring_registration", nullable = false)
    @Getter
    @Setter
    private PayInRecurringRegistrationEntity recurringPayment;

    @NotNull
    @Column(name = "`recurring_type`")
    @Enumerated(EnumType.STRING)
    @Getter
    @Setter
    private EnumRecurringPaymentType recurringPaymentType;

    public void updateDto(PayInDto p) {
        p.setConsumerKey(consumer.getKey());
        p.setCreatedOn(createdOn);
        p.setCurrency(currency);
        p.setExecutedOn(executedOn);
        p.setId(id);
        p.setKey(key);
        p.setPayIn(payIn);
        p.setPaymentMethod(paymentMethod);
        p.setProviderKey(items.stream().map(i -> i.getProvider().getKey()).distinct().collect(Collectors.toList()));
        p.setReferenceNumber(referenceNumber);
        p.setStatus(status);
        p.setStatusUpdatedOn(statusUpdatedOn);
        p.setTotalPrice(totalPrice);
        p.setTotalPriceExcludingTax(totalPriceExcludingTax);
        p.setTotalTax(totalTax);
    }

    @Override
    public ConsumerPayInDto toConsumerDto(boolean includeDetails) {
        final ConsumerCardDirectPayInDto p = new ConsumerCardDirectPayInDto();

        this.updateDto(p);

        p.setAlias(alias);
        p.setCard(card);
        if (recurringPayment != null) {
            p.setRecurringPayment(recurringPayment.toConsumerDto(includeDetails));
        }
        p.setRecurringPaymentType(recurringPaymentType);
        p.setStatementDescriptor(statementDescriptor);

        if (this.billingAddress != null) {
            p.setBilling(this.billingAddress.toDto());
        }
        if (this.shippingAddress != null) {
            p.setShipping(this.shippingAddress.toDto());
        }
        if (this.browserInfo != null) {
            p.setBrowserInfo(this.browserInfo.toDto());
        }

        if (includeDetails) {
            this.items.stream().map(i -> i.toConsumerDto(includeDetails)).forEach(p::addItem);
        }

        return p;
    }

    @Override
    public ProviderPayInDto toProviderDto(boolean includeDetails) {
        final ProviderCardDirectPayInDto p = new ProviderCardDirectPayInDto();

        this.updateDto(p);

        p.setAlias(alias);
        p.setCard(card);
        p.setConsumer(consumer.getConsumer().toConsumerDto());
        p.setStatementDescriptor(statementDescriptor);

        if (includeDetails) {
            this.items.stream().map(e -> e.toProviderDto(includeDetails)).forEach(p::addItem);
        }

        return p;
    }

    @Override
    public HelpdeskPayInDto toHelpdeskDto(boolean includeDetails) {
        final HelpdeskCardDirectPayInDto p = new HelpdeskCardDirectPayInDto();

        this.updateDto(p);

        p.setAlias(alias);
        p.setCard(card);
        p.setConsumer(consumer.getProfile().getConsumer().toDto());
        p.setProcessDefinition(processDefinition);
        p.setProcessInstance(processInstance);
        p.setProviderPayIn(payIn);
        p.setProviderResultCode(resultCode);
        p.setProviderResultMessage(resultMessage);
        if (recurringPayment != null) {
            p.setRecurringPayment(recurringPayment.toHelpdeskDto(includeDetails));
        }
        p.setRecurringPaymentType(recurringPaymentType);
        p.setStatementDescriptor(statementDescriptor);

        p.setRequested3dsVersion(requested3dsVersion);
        p.setApplied3dsVersion(applied3dsVersion);

        if (this.billingAddress != null) {
            p.setBilling(this.billingAddress.toDto());
        }
        if (this.shippingAddress != null) {
            p.setShipping(this.shippingAddress.toDto());
        }
        if (this.browserInfo != null) {
            p.setBrowserInfo(this.browserInfo.toDto());
        }

        if (includeDetails) {
            this.items.stream().map(e -> e.toHelpdeskDto(includeDetails)).forEach(p::addItem);
        }

        return p;
    }

}
