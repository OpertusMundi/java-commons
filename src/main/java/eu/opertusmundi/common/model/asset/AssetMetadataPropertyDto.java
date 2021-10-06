package eu.opertusmundi.common.model.asset;

import eu.opertusmundi.common.model.catalogue.client.EnumAssetType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AssetMetadataPropertyDto {

    private EnumAssetType assetType;

    private String name;

    private EnumMetadataPropertyType type;

}
