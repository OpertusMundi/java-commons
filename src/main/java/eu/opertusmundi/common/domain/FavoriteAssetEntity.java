package eu.opertusmundi.common.domain;

import java.time.ZonedDateTime;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import eu.opertusmundi.common.model.favorite.EnumAssetFavoriteAction;
import eu.opertusmundi.common.model.favorite.EnumFavoriteType;
import eu.opertusmundi.common.model.favorite.FavoriteAssetDto;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "FavoriteAsset")
@Table(schema = "web", name = "`favorite_asset`")
@DiscriminatorValue(value = "ASSET")
@Getter
@Setter
public class FavoriteAssetEntity extends FavoriteEntity {

    public FavoriteAssetEntity() {
        super(EnumFavoriteType.ASSET);
    }

    @NotNull
    @Column(name = "`asset_id`")
    private String assetId;

    @NotNull
    @Column(name = "`asset_version`")
    private String assetVersion;

    @Column(name = "`asset_provider`")
    private Integer assetProvider;

    @NotNull
    @Column(name ="`action`")
    @Enumerated(EnumType.STRING)
    private EnumAssetFavoriteAction action;

    @NotNull
    @Column(name ="`notification_sent`")
    private boolean notificationSent;

    @Column(name = "`notification_sent_at`")
    private ZonedDateTime notificationSentAt;

    @Override
    public FavoriteAssetDto toDto(boolean includeDetails) {
        final FavoriteAssetDto f = new FavoriteAssetDto();

        f.setAccountKey(account.getKey());
        f.setAction(action);
        f.setAssetId(assetId);
        f.setAssetProvider(assetProvider);
        f.setAssetVersion(assetVersion);
        f.setId(id);
        f.setKey(key);
        f.setNotificationSent(notificationSent);
        f.setNotificationSentAt(notificationSentAt);
        f.setTitle(title);
        f.setType(type);

        return f;
    }

}
