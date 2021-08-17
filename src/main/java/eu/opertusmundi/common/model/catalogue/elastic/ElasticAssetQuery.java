package eu.opertusmundi.common.model.catalogue.elastic;

import java.util.List;
import java.util.Optional;

import org.locationtech.jts.geom.Coordinate;

import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.opertusmundi.common.model.EnumSortingOrder;
import eu.opertusmundi.common.model.catalogue.client.EnumTopicCategory;
import eu.opertusmundi.common.model.catalogue.client.EnumType;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ElasticAssetQuery {

    @Schema(description = "Text used for full text search")
    private String text;

    @ArraySchema(
        arraySchema = @Schema(
            description = "The nature or genre of the resource",
            example = "RASTER"
        ),
        minItems = 0, uniqueItems = true
    )
    private List<EnumType> type;

    @Schema(description = "The file format, physical medium, or dimensions of the resource", example = "ESRI Shapefile")
    private List<String> format;

    @Schema(description = "Information about the reference system in automated metadata")
    private List<String> crs;

    @Schema(description = "Minimum price (excluding VAT)")
    private Integer minPrice;

    @Schema(description = "Maximum price (excluding VAT)")
    private Integer maxPrice;

    @Schema(description = "The temporal extent of the resource (start date)", example = "2020-06-01")
    private String fromDate;

    @Schema(description = "The temporal extent of the resource (end date)", example = "2020-06-30")
    private String toDate;

    @ArraySchema(
        arraySchema = @Schema(
            description = "A high-level classification scheme to assist in the grouping and topic-based "
                        + "search of available spatial data resources",
            example = "LOCATION"
        ),
        minItems = 0, uniqueItems = true
    )
    private List<EnumTopicCategory> topic;

    @Schema(description = "Minimum scale value")
    private Integer minScale;

    @Schema(description = "Maximum scale value")
    private Integer maxScale;

    @Schema(description = "Automated metadata attributes")
    private List<String> attribute;

    @Schema(description = "Information about resource licensing")
    private List<String> license;

    @Schema(description = "Name of an entity responsible for making the resource available")
    private List<String> publisher;

    @Schema(description = "Language of asset file")
    private List<String> language;

    @Schema(description = "Size of dataset")
    private List<EnumElasticSearchDatasetSize> sizeOfDataset;

    @Schema(description = "Sorting field", defaultValue = "SCORE")
    private EnumElasticSearchSortField orderBy;

    @Schema(description = "Sorting direction", defaultValue = "DESC")
    private EnumSortingOrder order;

    @Schema(description = "Pagination page index", defaultValue = "0")
    private Optional<Integer> page;

    @Schema(description = "Pagination page size", defaultValue = "10")
    private Optional<Integer> size;

    @Schema(description = "Mode of coverage search")
    private EnumSpatialOperation spatialOperation;

    @Schema(description = "Bounding box top left longitude")
    private Double topLeftX;

    @Schema(description = "Bounding box top left latitude")
    private Double topLeftY;

    @Schema(description = "Bounding box bottom right longitude")
    private Double bottomRightX;

    @Schema(description = "Bounding box bottom right latitude")
    private Double bottomRightY;

    public Coordinate topLeftToCoordinate() {
        return topLeftX == null || topLeftY == null ? null : new Coordinate(topLeftX, topLeftY);
    }

    public Coordinate bottomRightToCoordinate() {
        return bottomRightX == null || bottomRightY == null ? null : new Coordinate(bottomRightX, bottomRightY);
    }

    @JsonIgnore
    public Integer getFrom() {
        if (page.isPresent() && size.isPresent()) {
            return page.get() * size.get() < 0 ? 0 : page.get() * size.get();
        }
        return null;
    }

}
