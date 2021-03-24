package eu.opertusmundi.common.domain;

import java.time.ZonedDateTime;

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

import eu.opertusmundi.common.model.dto.AccountSubscriptionDto;
import eu.opertusmundi.common.model.dto.EnumAssetSource;
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
    @JoinColumn(name = "account", nullable = false)
    @Getter
    @Setter
    private AccountEntity account;

    @NotNull
    @ManyToOne(targetEntity = OrderEntity.class)
    @JoinColumn(name = "order", nullable = false)
    @Getter
    @Setter
    private OrderEntity order;

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

    public AccountSubscriptionDto toDto() {
        final AccountSubscriptionDto s = new AccountSubscriptionDto();

        s.setAddedOn(addedOn);
        s.setId(id);
        s.setOrderId(order.getId());
        s.setService(service);
        s.setSource(source);
        s.setUpdatedOn(updatedOn);

        return s;
    }

}