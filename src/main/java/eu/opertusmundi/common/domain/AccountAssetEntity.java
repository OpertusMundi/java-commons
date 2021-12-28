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

    /**
     * The owner of the asset
     */
    @NotNull
    @ManyToOne(targetEntity = AccountEntity.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "consumer", nullable = false)
    @Getter
    @Setter
    private AccountEntity consumer;

    /**
     * The order linked to the purchase of the asset. If this record has been
     * created due to an asset update, the order reference is set to the one of
     * the initial purchase
     */
    @NotNull
    @ManyToOne(targetEntity = OrderEntity.class)
    @JoinColumn(name = "`order`", nullable = false)
    @Getter
    @Setter
    private OrderEntity order;

    /**
     * Asset unique identifier (PID)
     */
    @NotNull
    @Column(name = "`asset`")
    @Getter
    @Setter
    private String asset;

    /**
     * Date of purchase. If this record is created due to an asset update, the
     * date is set to the one of the initial purchase
     */
    @NotNull
    @Column(name = "`purchased_on`")
    @Getter
    @Setter
    private ZonedDateTime purchasedOn;

    /**
     * The date the asset has been registered to the user account
     */
    @NotNull
    @Column(name = "`added_on`")
    @Getter
    @Setter
    private ZonedDateTime addedOn;

    /**
     * Interval in years that the user is eligible to receive updates
     *
     * For `UPDATE` records, the value is set to the one of the initial
     * registration.
     *
     * The value of the field must be greater than zero when comparing dates
     * for determining update qualification.
     */
    @NotNull
    @Column(name = "`update_interval`")
    @Getter
    @Setter
    private Integer updateInterval = 0;

    /**
     * The user is eligible to receive updates until this date. For `UPDATE`
     * records, the value is set to the one of the initial registration
     */
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
