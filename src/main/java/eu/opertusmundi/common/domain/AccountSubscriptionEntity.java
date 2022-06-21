package eu.opertusmundi.common.domain;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

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
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import eu.opertusmundi.common.model.account.AccountSubscriptionDto;
import eu.opertusmundi.common.model.account.EnumAssetSource;
import eu.opertusmundi.common.model.account.EnumSubscriptionStatus;
import eu.opertusmundi.common.model.catalogue.client.EnumTopicCategory;
import eu.opertusmundi.common.model.payment.consumer.ConsumerAccountSubscriptionDto;
import eu.opertusmundi.common.model.payment.helpdesk.HelpdeskAccountSubscriptionDto;
import eu.opertusmundi.common.model.payment.provider.ProviderAccountSubscriptionDto;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "AccountSubscription")
@Table(schema = "web", name = "`account_subscription`")
public class AccountSubscriptionEntity {

    @Id
    @Column(name = "`id`", updatable = false)
    @SequenceGenerator(sequenceName = "web.account_sub_id_seq", name = "account_sub_id_seq", allocationSize = 1)
    @GeneratedValue(generator = "account_sub_id_seq", strategy = GenerationType.SEQUENCE)
    @Getter
    private Integer id;

    @NotNull
    @ManyToOne(targetEntity = AccountEntity.class)
    @JoinColumn(name = "consumer", nullable = false)
    @Getter
    @Setter
    private AccountEntity consumer;

    @NotNull
    @ManyToOne(targetEntity = OrderEntity.class)
    @JoinColumn(name = "`order`", nullable = false)
    @Getter
    @Setter
    private OrderEntity order;

    @NotNull
    @ManyToOne(targetEntity = AccountEntity.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "provider", nullable = false)
    @Getter
    @Setter
    private AccountEntity provider;

    @OneToMany(
        targetEntity = AccountSubscriptionSkuEntity.class,
        mappedBy = "subscription",
        fetch = FetchType.LAZY,
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    @Getter
    private final List<AccountSubscriptionSkuEntity> skus = new ArrayList<>();

    @NotNull
    @Column(name = "`asset`")
    @Getter
    @Setter
    private String asset;

    @NotNull
    @Column(name = "`added_on`")
    @Getter
    @Setter
    private ZonedDateTime addedOn;

    @NotNull
    @Column(name = "`updated_on`")
    @Getter
    @Setter
    private ZonedDateTime updatedOn;

    @Column(name = "`expires_on`")
    @Getter
    @Setter
    private ZonedDateTime expiresOn;

    @Column(name = "`cancelled_on`")
    @Getter
    @Setter
    private ZonedDateTime cancelledOn;

    @NotNull
    @Column(name = "`source`")
    @Enumerated(EnumType.STRING)
    @Getter
    @Setter
    private EnumAssetSource source;

    @Column(name = "`segment`")
    @Enumerated(EnumType.STRING)
    @Getter
    @Setter
    private EnumTopicCategory segment;

    @NotNull
    @Column(name = "`status`")
    @Enumerated(EnumType.STRING)
    @Getter
    @Setter
    private EnumSubscriptionStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payin_recurring_registration")
    @Getter
    @Setter
    private PayInRecurringRegistrationEntity recurringPayIn;

    @NotNull
    @Column(name = "`last_payin_date`")
    @Getter
    @Setter
    private ZonedDateTime lastPayinDate;

    @Column(name = "`next_payin_date`")
    @Getter
    @Setter
    private ZonedDateTime nextPayinDate;

    public void updateDto(AccountSubscriptionDto s) {
        s.setAddedOn(addedOn);
        s.setAssetId(asset);
        s.setId(id);
        s.setKey(order.getKey());
        s.setOrderId(order.getId());
        s.setSegment(segment);
        s.setSource(source);
        s.setStatus(status);
        s.setUpdatedOn(updatedOn);
    }

    public ConsumerAccountSubscriptionDto toConsumerDto(boolean includeProviderDetails) {
        final ConsumerAccountSubscriptionDto s = new ConsumerAccountSubscriptionDto();

        this.updateDto(s);

        s.setProvider(this.provider.getProvider().toProviderDto(includeProviderDetails));
        if (this.recurringPayIn != null) {
            s.setRecurringRegistration(this.recurringPayIn.toConsumerDto(true, false));
        }


        return s;
    }

    public ProviderAccountSubscriptionDto toProviderDto() {
        final ProviderAccountSubscriptionDto s = new ProviderAccountSubscriptionDto();

        this.updateDto(s);

        s.setConsumer(this.consumer.getConsumer().toConsumerDto());
        if (this.recurringPayIn != null) {
            s.setRecurringRegistration(this.recurringPayIn.toProviderDto(true, false));
        }

        return s;
    }

    public HelpdeskAccountSubscriptionDto toHelpdeskDto() {
        final HelpdeskAccountSubscriptionDto s = new HelpdeskAccountSubscriptionDto();

        this.updateDto(s);

        s.setConsumer(this.consumer.getConsumer().toDto());
        s.setProvider(this.provider.getProvider().toDto());
        if (this.recurringPayIn != null) {
            s.setRecurringRegistration(this.recurringPayIn.toHelpdeskDto(true, false));
        }

        return s;
    }

}