package eu.opertusmundi.common.model.catalogue.client;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import eu.opertusmundi.common.model.catalogue.server.CatalogueFeature;
import eu.opertusmundi.common.model.catalogue.server.CatalogueFeatureProperties;
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

        final CatalogueFeatureProperties props = feature.getProperties();
        
        this.id = feature.getId();

        this.publisherId = props.getPublisherId();
        this.title       = props.getTitle();
        this.type        = EnumType.fromString(props.getType());
        this.version     = props.getVersion();

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
    @JsonInclude(Include.NON_EMPTY)
    private List<BasePricingModelDto> pricingModels;

    @Schema(description = "Id of an entity responsible for making the resource available")
    @Getter
    @Setter
    protected UUID publisherId;

    @Schema(description = "A name given to the resource")
    @Getter
    @Setter
    private String title;
    
    @Schema(description = "The nature or genre of the resource")
    @Getter
    @Setter
    private EnumType type;

    @Schema(description = "Version of the resource")
    @Getter
    @Setter
    private String version;

}
