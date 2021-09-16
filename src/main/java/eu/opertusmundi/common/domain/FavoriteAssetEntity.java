package eu.opertusmundi.common.domain;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import eu.opertusmundi.common.model.favorite.EnumFavoriteType;
import eu.opertusmundi.common.model.favorite.FavoriteAssetDto;
import eu.opertusmundi.common.model.favorite.FavoriteDto;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "FavoriteAsset")
@Table(schema = "web", name = "`favorite_asset`")
@DiscriminatorValue(value = "ASSET")
public class FavoriteAssetEntity extends FavoriteEntity {

    public FavoriteAssetEntity() {
        super(EnumFavoriteType.ASSET);
    }

    @NotNull
    @Column(name = "`asset_id`")
    @Getter
    @Setter
    protected String assetId;

    @NotNull
    @Column(name = "`asset_version`")
    @Getter
    @Setter
    protected String assetVersion;

    @Override
    public FavoriteDto toDto(boolean includeDetails) {
        final FavoriteAssetDto f = new FavoriteAssetDto();

        f.setAssetId(assetId);
        f.setAssetVersion(assetVersion);
        f.setId(id);
        f.setKey(key);
        f.setTitle(title);
        f.setType(type);

        return f;
    }

}
