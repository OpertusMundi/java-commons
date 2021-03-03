package eu.opertusmundi.common.model.catalogue.client;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.validation.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.opertusmundi.common.model.asset.AssetAdditionalResourceDto;
import eu.opertusmundi.common.model.asset.AssetFileAdditionalResourceDto;
import eu.opertusmundi.common.model.asset.AssetResourceDto;
import eu.opertusmundi.common.model.asset.EnumAssetAdditionalResource;
import eu.opertusmundi.common.model.catalogue.server.CatalogueFeature;
import eu.opertusmundi.common.model.openapi.schema.AssetEndpointTypes;
import eu.opertusmundi.common.model.openapi.schema.PricingModelCommandAsJson;
import eu.opertusmundi.common.model.pricing.BasePricingModelCommandDto;
import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public final class CatalogueItemCommandDto extends BaseCatalogueItemDto implements Serializable {

    private static final long serialVersionUID = 1L;

    public CatalogueItemCommandDto() {
        this.additionalResources = new ArrayList<>();
        this.pricingModels       = new ArrayList<>();
        this.resources           = new ArrayList<>();
    }

    public CatalogueItemCommandDto(CatalogueFeature feature) {
        super(feature);

        this.additionalResources = new ArrayList<>();
        this.pricingModels       = new ArrayList<>();
        this.resources           = new ArrayList<>();
    }

    /**
     * Asset unique key. This value is injected by the controller.
     */
    @JsonIgnore
    private UUID assetKey;
    
    @ArraySchema(
        arraySchema = @Schema(
            description = "Auxiliary files or additional resources to the dataset"
        ),
        minItems = 0,
        uniqueItems = true,
        schema = @Schema(implementation = AssetEndpointTypes.AssetAdditionalResource.class)
    )
    private List<AssetAdditionalResourceDto> additionalResources;
    
    @Schema(
        description = "True if the resource files should be imported into PostGIS database and published using WMS/WFS "
                    + "endpoints. Ingest operation is only supported for formats of category <b>VECTOR</b>",
        required = false,
        defaultValue = "false",
        externalDocs = @ExternalDocumentation(
            description = "See configuration endpoint for asset file types details",
            url   = "#operation/configuration-01" 
        )
    )
    private boolean ingested = false;
   
    @ArraySchema(
        arraySchema = @Schema(
            description = "Supported pricing models"
        ),
        minItems = 1,
        uniqueItems = true,
        schema = @Schema(implementation = PricingModelCommandAsJson.class)
    )
    private List<BasePricingModelCommandDto> pricingModels;

    @Schema(description = "A name given to the resource", required = true)
    @NotEmpty
    private String title;

    @Schema(description = "The nature or genre of the resource", required = true)
    private EnumType type;
    
    @Schema(description = "Version of the resource", required = true)
    @NotEmpty
    private String version;

    @ArraySchema(
        arraySchema = @Schema(
            description = "Provides a list of resources of the dataset"
        ),
        minItems = 0,
        uniqueItems = true,
        schema = @Schema(implementation = AssetResourceDto.class)
    )
    private List<AssetResourceDto> resources;
    
    /**
     * Publisher unique key.
     *
     * This value is ignored during serialization/deserialization. Instead, it
     * is injected by the controller. The value is equal to the unique key of
     * the authenticated user.
     */
    @JsonIgnore
    private UUID publisherKey;
   
    public CatalogueFeature toFeature() {
        return new CatalogueFeature(this);
    }
    
    public void addResource(AssetResourceDto resource) {
        final AssetResourceDto existing = this.resources.stream()
            .filter(r -> r.getId().equals(resource.getId()))
            .findFirst()
            .orElse(null);
        
        if (existing == null) {
            this.resources.add(resource);
        } else {
            existing.patch(resource);
        }
    }
    
    public void addAdditionalResource(AssetFileAdditionalResourceDto resource) {
        final AssetFileAdditionalResourceDto existing = this.additionalResources.stream()
                .filter(r -> r.getType() == EnumAssetAdditionalResource.FILE)
                .map(r -> (AssetFileAdditionalResourceDto) r)
                .filter(r -> r.getId().equals(resource.getId()))
                .findFirst()
                .orElse(null);
            
            if (existing == null) {
                this.additionalResources.add(resource);
            } else {
                existing.patch(resource);
            }
    }

}
