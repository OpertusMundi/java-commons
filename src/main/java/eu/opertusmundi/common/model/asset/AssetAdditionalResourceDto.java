package eu.opertusmundi.common.model.asset;

import java.io.Serializable;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import eu.opertusmundi.common.model.catalogue.server.CatalogueAdditionalResource;
import eu.opertusmundi.common.model.pricing.EnumPricingModel;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type"
)
@JsonSubTypes({
    @Type(name = "FILE", value = AssetFileAdditionalResourceDto.class),
    @Type(name = "URI", value = AssetUriAdditionalResourceDto.class),
})
public abstract class AssetAdditionalResourceDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(
        description = "Discriminator field used for deserializing the model to the appropriate data type",
        example = "FILE"
    )
    @JsonDeserialize(using = EnumPricingModel.Deserializer.class)
    @Getter
    @Setter
    protected EnumAssetAdditionalResource type;

    protected AssetAdditionalResourceDto() {
        this.type = EnumAssetAdditionalResource.UNDEFINED;
    }

    protected AssetAdditionalResourceDto(EnumAssetAdditionalResource type) {
        this.type = type;
    }
    
    public static AssetAdditionalResourceDto fromCatalogueResource(CatalogueAdditionalResource r) {
        final EnumAssetAdditionalResource type = EnumAssetAdditionalResource.fromString(r.getType());

        // Load only URI resources from the catalogue
        switch (type) {
            case URI:
                return new AssetUriAdditionalResourceDto(r.getValue(), r.getName());
            default :
                return new AssetFileAdditionalResourceDto(
                    UUID.fromString(r.getId()), r.getValue(), null, r.getName(), null
                );
        }
    }
    
    public abstract CatalogueAdditionalResource toCatalogueResource();
    
}