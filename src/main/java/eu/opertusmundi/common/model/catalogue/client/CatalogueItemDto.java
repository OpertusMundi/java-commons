package eu.opertusmundi.common.model.catalogue.client;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import eu.opertusmundi.common.model.catalogue.server.CatalogueFeature;
import eu.opertusmundi.common.model.openapi.schema.PricingModelAsJson;
import eu.opertusmundi.common.model.pricing.BasePricingModelDto;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
public class CatalogueItemDto extends BaseCatalogueItemDto implements Serializable {

    private static final long serialVersionUID = 1L;

    public CatalogueItemDto(CatalogueFeature feature) {
        super(feature);

        this.id = UUID.fromString(feature.getId());

        this.publisherId = feature.getProperties().getPublisherId();
        this.statistics  = feature.getProperties().getStoreStatistics();
        this.title       = feature.getProperties().getTitle();
        this.version     = feature.getProperties().getVersion();

        // Initialize with an empty collection. Caller must compute the
        // effective pricing models
        this.pricingModels = new ArrayList<BasePricingModelDto>();
    }

    @Schema(description = "Catalogue item identifier (UUID)", example = "f5edff99-426b-4b17-a4f8-3d423a6c491b")
    @Getter
    @Setter
    private UUID id;

    @ArraySchema(
        arraySchema = @Schema(
            description = "Supported pricing models"
        ),
        minItems = 1,
        uniqueItems = true,
        schema = @Schema(implementation = PricingModelAsJson.class)
    )
    @Getter
    @Setter
    private List<BasePricingModelDto> pricingModels;

    @Schema(description = "Id of an entity responsible for making the resource available")
    @Getter
    @Setter
    protected UUID publisherId;

    @Schema(description = "Asset statistics")
    @Getter
    @Setter
    private CatalogueItemStoreStatistics statistics;

    @Schema(description = "A name given to the resource")
    private String title;

    @Schema(description = "Version of the resource")
    private String version;

}
