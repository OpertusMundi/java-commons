package eu.opertusmundi.common.model.catalogue.client;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import javax.validation.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.opertusmundi.common.model.catalogue.server.CatalogueFeature;
import eu.opertusmundi.common.model.openapi.schema.PricingModelCommandAsJson;
import eu.opertusmundi.common.model.pricing.BasePricingModelCommandDto;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class CatalogueItemCommandDto extends BaseCatalogueItemDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(
        description = "True if the file should be imported into PostGIS database and published using WMS/WFS endpoints",
        required = false,
        defaultValue = "false"
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

    @Schema(description = "Path to user's remote file system", required = false)
    private String source;

    @Schema(description = "A name given to the resource", required = true)
    @NotEmpty
    private String title;

    @Schema(description = "Version of the resource", required = true)
    @NotEmpty
    private String version;

    /**
     * Asset unique key. This value is injected by the controller.
     */
    @JsonIgnore
    private UUID assetKey;

    /**
     * Current user id
     */
    @JsonIgnore
    private Integer userId;

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

}
