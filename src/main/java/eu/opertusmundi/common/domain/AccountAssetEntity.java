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

import eu.opertusmundi.common.model.account.AccountAssetDto;
import eu.opertusmundi.common.model.account.EnumAssetSource;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "AccountAsset")
@Table(schema = "web", name = "`account_asset`")
public class AccountAssetEntity {

    @Id
    @Column(name = "`id`", updatable = false)
    @SequenceGenerator(sequenceName = "web.account_asset_id_seq", name = "account_asset_id_seq", allocationSize = 1)
    @GeneratedValue(generator = "account_asset_id_seq", strategy = GenerationType.SEQUENCE)
    @Getter
    private Integer id;

    @NotNull
    @ManyToOne(targetEntity = AccountEntity.class, fetch = FetchType.LAZY)
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
    @Column(name = "`asset`")
    @Getter
    @Setter
    private String asset;

    @NotNull
    @Column(name = "`purchased_on`")
    @Getter
    @Setter
    private ZonedDateTime purchasedOn;

    @NotNull
    @Column(name = "`added_on`")
    @Getter
    @Setter
    private ZonedDateTime addedOn;

    @NotNull
    @Column(name = "`update_interval`")
    @Getter
    @Setter
    private Integer updateInterval = 0;

    @NotNull
    @Column(name = "`update_eligibility`")
    @Getter
    @Setter
    private ZonedDateTime updateEligibility;

    @NotNull
    @Column(name = "`source`")
    @Enumerated(EnumType.STRING)
    @Getter
    @Setter
    private EnumAssetSource source;

    public AccountAssetDto toDto() {
        final AccountAssetDto a = new AccountAssetDto();

        a.setAddedOn(addedOn);
        a.setAssetId(asset);
        a.setId(id);
        a.setOrderId(order.getId());
        a.setOrderKey(order.getKey());
        a.setPurchasedOn(purchasedOn);
        a.setSource(source);
        a.setUpdateEligibility(updateEligibility);
        a.setUpdateInterval(updateInterval);

        return a;
    }

}
