package eu.opertusmundi.common.model.asset;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AssetMetadataPropertyDto {

    private EnumAssetSourceType assetType;

    private String name;

    private EnumMetadataPropertyType type;

}
