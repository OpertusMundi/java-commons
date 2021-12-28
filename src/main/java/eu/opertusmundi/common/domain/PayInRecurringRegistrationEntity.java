package eu.opertusmundi.common.domain;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
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
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.NaturalId;

import eu.opertusmundi.common.model.payment.EnumRecurringPaymentFrequency;
import eu.opertusmundi.common.model.payment.EnumRecurringPaymentStatus;
import eu.opertusmundi.common.model.payment.PayInRecurringRegistrationDto;
import eu.opertusmundi.common.util.StreamUtils;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "PayInRecurringRegistration")
@Table(schema = "billing", name = "`payin_recurring_registration`", uniqueConstraints = {
    @UniqueConstraint(name = "uq_payin_recurring_registration_key", columnNames = {"`key`"}),
})
public class PayInRecurringRegistrationEntity {

    @Id
    @Column(name = "`id`", updatable = false)
    @SequenceGenerator(sequenceName = "billing.payin_recurring_registration_id_seq", name = "payin_recurring_registration_id_seq", allocationSize = 1)
    @GeneratedValue(generator = "payin_recurring_registration_id_seq", strategy = GenerationType.SEQUENCE)
    @Getter
    private Integer id;

    @NotNull
    @NaturalId
    @Column(name = "`key`", updatable = false, columnDefinition = "uuid")
    @Getter
    @Setter
    private UUID key;

    /**
     * Reference to the subscription linked to this registration
     */
    @NotNull
    @ManyToOne(targetEntity = AccountSubscriptionEntity.class, fetch = FetchType.EAGER)
    @JoinColumn(name = "subscription", nullable = false)
    @Getter
    @Setter
    private AccountSubscriptionEntity subscription;

    /**
     * Collection of PayIn records linked to this registration
     */
    @OneToMany(
        mappedBy = "recurringPayment",
        fetch = FetchType.LAZY,
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    @Getter
    @Setter
    private List<CardDirectPayInEntity> payins = new ArrayList<>();

    @OneToMany(
        mappedBy = "registration",
        fetch = FetchType.LAZY,
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    @Getter
    @Setter
    private List<PayInRecurringRegistrationStatusEntity> statusHistory = new ArrayList<>();

    @NotEmpty
    @Column(name = "`provider_card`")
    @Getter
    @Setter
    private String providerCard;

    @NotEmpty
    @Column(name = "`provider_registration`")
    @Getter
    @Setter
    private String providerRegistration;

    @NotNull
    @Column(name = "`first_transaction_debited_funds`", columnDefinition = "numeric", precision = 20, scale = 6)
    @Getter
    @Setter
    private BigDecimal firstTransactionDebitedFunds;

    @Column(name = "`next_transaction_debited_funds`", columnDefinition = "numeric", precision = 20, scale = 6)
    @Getter
    @Setter
    private BigDecimal nextTransactionDebitedFunds;

    @NotNull
    @Column(name = "`currency`")
    @Getter
    @Setter
    private String currency;

    @NotNull
    @Column(name = "`end_date`")
    @Getter
    @Setter
    private ZonedDateTime endDate;

    @NotNull
    @Column(name = "`frequency`")
    @Enumerated(EnumType.STRING)
    @Getter
    @Setter
    private EnumRecurringPaymentFrequency frequency;

    @NotNull
    @Column(name = "`fixed_next_amount`")
    @Getter
    @Setter
    private boolean fixedNextAmount = true;

    @NotNull
    @Column(name = "`fractioned_payment`")
    @Getter
    @Setter
    private boolean fractionedPayment = false;

    @NotNull
    @Column(name = "`migration`")
    @Getter
    @Setter
    private boolean migration = false;

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
    private EnumRecurringPaymentStatus status;

    @NotNull
    @Column(name = "`status_updated_on`")
    @Getter
    @Setter
    private ZonedDateTime statusUpdatedOn;

    private void updateDto(PayInRecurringRegistrationDto r, boolean includeDetails) {
        r.setBillingAddress(billingAddress.toDto());
        r.setCreatedOn(createdOn);
        r.setCurrency(currency);
        r.setEndDate(endDate);
        r.setFirstTransactionDebitedFunds(firstTransactionDebitedFunds);
        r.setFixedNextAmount(fixedNextAmount);
        r.setFractionedPayment(fractionedPayment);
        r.setFrequency(frequency);
        r.setId(id);
        r.setKey(key);
        r.setMigration(migration);
        r.setNextTransactionDebitedFunds(nextTransactionDebitedFunds);
        r.setProviderCard(providerCard);
        r.setProviderRegistration(providerRegistration);
        r.setShippingAddress(shippingAddress.toDto());
        r.setStatus(status);
        r.setStatusUpdatedOn(statusUpdatedOn);

        if (includeDetails) {
            StreamUtils.from(statusHistory).map(PayInRecurringRegistrationStatusEntity::toDto).forEach(r.getStatusHistory()::add);
        }
    }

    public PayInRecurringRegistrationDto toConsumerDto(boolean includeDetails) {
        final PayInRecurringRegistrationDto r = new PayInRecurringRegistrationDto();
        this.updateDto(r, includeDetails);

        r.setSubscription(subscription.toConsumerDto(includeDetails));

        if (includeDetails) {
            StreamUtils.from(payins).map(p -> p.toConsumerDto(false)).forEach(r.getPayins()::add);
        }

        return r;
    }

    public PayInRecurringRegistrationDto toProviderDto(boolean includeDetails) {
        final PayInRecurringRegistrationDto r = new PayInRecurringRegistrationDto();
        this.updateDto(r, includeDetails);

        r.setSubscription(subscription.toProviderDto());

        if (includeDetails) {
            StreamUtils.from(payins).map(p -> p.toProviderDto(false)).forEach(r.getPayins()::add);
        }

        return r;
    }

    public PayInRecurringRegistrationDto toHelpdeskDto(boolean includeDetails) {
        final PayInRecurringRegistrationDto r = new PayInRecurringRegistrationDto();
        this.updateDto(r, includeDetails);

        r.setSubscription(subscription.toHelpdeskDto());

        if (includeDetails) {
            StreamUtils.from(payins).map(p -> p.toHelpdeskDto(false)).forEach(r.getPayins()::add);
        }

        return r;
    }
}
