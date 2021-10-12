package eu.opertusmundi.common.model.asset;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import eu.opertusmundi.common.model.catalogue.client.EnumAssetType;
import eu.opertusmundi.common.model.catalogue.server.CatalogueResource;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class BundleAssetResourceDto extends ResourceDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonCreator
    public BundleAssetResourceDto(@JsonProperty("id") String id) {
        super(id, null, EnumResourceType.ASSET);
    }

    public BundleAssetResourceDto(CatalogueResource r) {
        super(r.getId(), null, EnumResourceType.ASSET);
    }

    @Override
    public void patch(ResourceDto r) {
        // No editable fields exist
    }

    @Override
    public CatalogueResource toCatalogueResource() {
        final CatalogueResource r = CatalogueResource.builder()
            .category(EnumAssetType.BUNDLE)
            .id(id)
            .type(EnumResourceType.ASSET)
            .build();

        return r;
    }

}