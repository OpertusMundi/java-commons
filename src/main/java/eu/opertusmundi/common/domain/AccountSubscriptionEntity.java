package eu.opertusmundi.common.domain;

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
import javax.validation.constraints.NotNull;

import eu.opertusmundi.common.model.account.AccountSubscriptionDto;
import eu.opertusmundi.common.model.account.EnumAssetSource;
import eu.opertusmundi.common.model.catalogue.client.EnumTopicCategory;
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

    @NotNull
    @Column(name = "`service`")
    @Getter
    @Setter
    private String service;

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

    public AccountSubscriptionDto toDto() {
        final AccountSubscriptionDto s = new AccountSubscriptionDto();

        s.setAddedOn(addedOn);
        s.setId(id);
        s.setOrderId(order.getId());
        s.setSegment(segment);
        s.setService(service);
        s.setSource(source);
        s.setUpdatedOn(updatedOn);

        return s;
    }

}