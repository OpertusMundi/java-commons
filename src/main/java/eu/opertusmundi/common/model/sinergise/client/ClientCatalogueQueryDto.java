package eu.opertusmundi.common.model.sinergise.client;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

import org.locationtech.jts.geom.Geometry;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.JsonNode;

import eu.opertusmundi.common.model.openapi.schema.GeometryAsJson;
import eu.opertusmundi.common.model.sinergise.FieldsDto;
import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * Sentinel Hub catalogue query
 *
 * @see https://docs.sentinel-hub.com/api/latest/reference/#operation/postSearchSTAC
 */
@Getter
@Setter
@Schema(
    description = "Sentinel Hub catalogue API query",
    externalDocs = @ExternalDocumentation(url = "https://docs.sentinel-hub.com/api/latest/reference/#operation/postSearchSTAC")
)
public class ClientCatalogueQueryDto {

    @Schema(description = "Only features that have a geometry that intersects the bounding box are selected. "
                        + "The bounding box is provided as four or six numbers, depending on whether the coordinate "
                        + "reference system includes a vertical axis (elevation or depth)")
    @Size(min = 4, max = 6)
    private BigDecimal[] bbox;

    @Schema(description = "Interval start date-time. Date and time expressions adhere to RFC 3339.")
    private ZonedDateTime fromDateTime;

    @Schema(description = "Interval end date-time. Date and time expressions adhere to RFC 3339.")
    private ZonedDateTime toDateTime;

    @Schema(implementation = GeometryAsJson.class, description = "Spatial search")
    private Geometry intersects;

    @Schema(description = "Collection identifier to include in the search for items. Only Items in the provided collection will be searched.")
    @NotEmpty
    private String collection;

    @Schema(description = "Array of Item ids to return. All other filter parameters that further restrict the number of search results are ignored.")
    private String[] ids;

    @Schema(description = "The maximum number of results to return (page size). Defaults to 10", defaultValue = "10")
    @Min(1)
    @Max(100)
    private int limit = 10;

    @Schema(description = "The token to retrieve the next set of results")
    @JsonInclude(Include.NON_NULL)
    private Integer next;

    @Schema(description = "Return distinct values of specified property")
    @JsonInclude(Include.NON_EMPTY)
    private String distinct;

    @Schema(description = "The include and exclude members specify an array of property names that are either included or excluded from the result, "
                        + "respectively. If both include and exclude are specified, include takes precedence. Values should include the full JSON "
                        + "path of the property.")
    @JsonInclude(Include.NON_NULL)
    private FieldsDto fields;

    @Schema(implementation = Object.class, description = "Define which properties to query and the operations to apply")
    @JsonInclude(Include.NON_EMPTY)
    private JsonNode query;

    @JsonIgnore
    public String getDateTime() {
        String result = null;

        if ((fromDateTime == null) && (toDateTime == null)) {
            return result;
        }
        result = fromDateTime == null ? "../" : fromDateTime.toOffsetDateTime().toString() + "/";
        result = toDateTime == null ? result + ".." : result + toDateTime.toOffsetDateTime().toString();

        return result;
    }
}
