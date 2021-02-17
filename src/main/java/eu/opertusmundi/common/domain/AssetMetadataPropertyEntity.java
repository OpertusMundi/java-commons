package eu.opertusmundi.common.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import eu.opertusmundi.common.model.asset.AssetMetadataPropertyDto;
import eu.opertusmundi.common.model.asset.EnumAssetSourceType;
import eu.opertusmundi.common.model.asset.EnumMetadataPropertyType;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "AssetMetadataProperty")
@Table(schema = "`provider`", name = "`asset_metadata_property`")
public class AssetMetadataPropertyEntity {

    @Id
    @SequenceGenerator(sequenceName = "`provider.asset_metadata_property_id_seq`", name = "asset_metadata_property_id_seq", allocationSize = 1)
    @GeneratedValue(generator = "asset_metadata_property_id_seq", strategy = GenerationType.SEQUENCE)
    @Column(name = "`id`")
    @Getter
    private Integer id;

    @NotNull
    @Column(name = "`asset_type`")
    @Enumerated(EnumType.STRING)
    @Getter
    @Setter
    private EnumAssetSourceType assetType;

    @NotNull
    @Column(name = "`name`")
    @Getter
    @Setter
    private String name;

    @Column(name = "`type`")
    @Enumerated(EnumType.STRING)
    @Getter
    @Setter
    private EnumMetadataPropertyType type;

    public AssetMetadataPropertyDto toDto() {
        final AssetMetadataPropertyDto p = new AssetMetadataPropertyDto();

        p.setAssetType(assetType);
        p.setName(name);
        p.setType(type);

        return p;
    }

}
