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

        this.id = feature.getId();

        this.publisherId = feature.getProperties().getPublisherId();
        this.title       = feature.getProperties().getTitle();
        this.version     = feature.getProperties().getVersion();

        // Initialize with an empty collection. Caller must compute the
        // effective pricing models
        this.pricingModels = new ArrayList<BasePricingModelDto>();
    }

    @Schema(description = "Catalogue item identifier (PID)")
    @Getter
    @Setter
    private String id;

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

    @Schema(description = "A name given to the resource")
    @Getter
    @Setter
    private String title;

    @Schema(description = "Version of the resource")
    @Getter
    @Setter
    private String version;

}
