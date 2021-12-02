package eu.opertusmundi.common.model.catalogue.client;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.locationtech.jts.geom.Geometry;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;

import eu.opertusmundi.common.model.openapi.schema.CatalogueEndpointTypes;
import eu.opertusmundi.common.model.openapi.schema.GeometryAsJson;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public final class CatalogueItemMetadataCommandDto implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * The publisher key
     *
     * If this is a vendor account (with role `ROLE_VENDOR_PROVIDER`), the
     * publisher key is the unique key of the parent account. If this is a
     * provider account (with role `ROLE_PROVIDER`, this is the unique key of
     * the authenticated user.
     */
    @JsonIgnore
    private UUID publisherKey;

    /**
     * The authenticated user key
     */
    @JsonIgnore
    private UUID ownerKey;

    @JsonIgnore
    private UUID draftKey;

    @Schema(description = "Resource key")
    @NotNull
    private UUID resourceKey;

    @ArraySchema(
        arraySchema = @Schema(
            description = "Controls automated metadata property visibility. The specified properties are hidden. "
                        + "If the property is set to `null`, the metadata is not updated. An empty array will make "
                        + "all metadata properties visible."
        ),
        minItems = 0,
        uniqueItems = true
    )
    private List<String> visibility = new ArrayList<>();

    @Schema(
        description = "Samples as a JSON object. If property is set to `null`, sample data is not updated.",
        implementation = CatalogueEndpointTypes.JsonNodeSamples.class
    )
    private JsonNode samples;

    @ArraySchema(
        schema = @Schema(implementation = GeometryAsJson.class),
        arraySchema = @Schema(
            description = "Areas of interest (bounding boxes) for creating samples for WMS or WFS service resources. "
                        + "If the property is set to `null`, no sampling operation is executed. This property is applicable "
                        + "only to assets of type `SERVICE` with property `spatialDataServiceType` in [`WMS`, `WFS`]. "
                        + "The sampling is performed after the provider accepts a draft and replaces any data already computed "
                        + "by the profiler service or set by the `samples` property."
        ),
        minItems = 0,
        maxItems = 5,
        uniqueItems = true
    )
    @Size(min = 0, max = 5)
    private List<Geometry> sampleAreas;

}
