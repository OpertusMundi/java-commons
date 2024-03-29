package eu.opertusmundi.common.model.catalogue.server;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.List;

import org.locationtech.jts.geom.Geometry;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import eu.opertusmundi.common.model.asset.EnumResourceType;
import eu.opertusmundi.common.model.asset.ServiceResourceDto;
import eu.opertusmundi.common.model.catalogue.LayerStyle;
import eu.opertusmundi.common.model.catalogue.client.EnumAssetType;
import eu.opertusmundi.common.model.catalogue.client.EnumSpatialDataServiceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class CatalogueResource implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Shared resource properties
     */

    private String id;

    @JsonProperty("parent_id")
    @JsonInclude(Include.NON_NULL)
    private String parentId;

    private EnumResourceType type;

    /**
     * File resource properties
     */

    @JsonInclude(Include.NON_NULL)
    private EnumAssetType category;

    @JsonInclude(Include.NON_EMPTY)
    private String delimiter;

    @JsonInclude(Include.NON_NULL)
    private String encoding;

    @JsonProperty("filename")
    @JsonInclude(Include.NON_NULL)
    private String fileName;

    @JsonInclude(Include.NON_NULL)
    private String format;

    @JsonInclude(Include.NON_EMPTY)
    private String geometry;

    @JsonInclude(Include.NON_EMPTY)
    private String latitude;

    @JsonInclude(Include.NON_EMPTY)
    private String longitude;

    @JsonInclude(Include.NON_NULL)
    private ZonedDateTime modifiedOn;

    @JsonInclude(Include.NON_NULL)
    private Long size;

    /**
     * Service resource properties
     */

    @JsonInclude(Include.NON_NULL)
    private Attributes attributes;

    @JsonInclude(Include.NON_NULL)
    private String attribution;

    @JsonInclude(Include.NON_NULL)
    private Geometry bbox;

    @JsonInclude(Include.NON_EMPTY)
    private List<String> crs;

    @JsonProperty("dimension")
    @JsonInclude(Include.NON_EMPTY)
    private List<Dimension> dimensions;

    @JsonInclude(Include.NON_NULL)
    private String endpoint;

    @JsonProperty("filter_capabilities")
    @JsonInclude(Include.NON_EMPTY)
    private List<String> filterCapabilities;

    @JsonProperty("min_scale")
    @JsonInclude(Include.NON_NULL)
    private Double minScale;

    @JsonProperty("max_scale")
    @JsonInclude(Include.NON_NULL)
    private Double maxScale;

    @JsonProperty("output_formats")
    @JsonInclude(Include.NON_EMPTY)
    private List<String> outputFormats;

    @JsonInclude(Include.NON_NULL)
    private EnumSpatialDataServiceType serviceType;

    @JsonInclude(Include.NON_EMPTY)
    @JsonProperty("style")
    private List<LayerStyle> styles;

    @JsonProperty("tile_sets")
    @JsonInclude(Include.NON_EMPTY)
    private List<TileSet> tileSets;

    @NoArgsConstructor
    @Getter
    @Setter
    public static class Attributes implements Serializable {

        private static final long serialVersionUID = 1L;

        public Attributes(ServiceResourceDto.Attributes a) {
            this.cascaded    = a.getCascaded();
            this.fixedHeight = a.getFixedHeight();
            this.fixedWidth  = a.getFixedWidth();
            this.noSubsets   = a.getNoSubsets();
            this.opaque      = a.getOpaque();
            this.queryable   = a.getQueryable();
        }

        @JsonInclude(Include.NON_NULL)
        private Boolean queryable;

        @JsonInclude(Include.NON_NULL)
        private Boolean cascaded;

        @JsonInclude(Include.NON_NULL)
        private Boolean opaque;

        @JsonProperty("no_subsets")
        @JsonInclude(Include.NON_NULL)
        private Boolean noSubsets;

        @JsonProperty("fixed_width")
        @JsonInclude(Include.NON_NULL)
        private Integer fixedWidth;

        @JsonProperty("fixed_height")
        @JsonInclude(Include.NON_NULL)
        private Integer fixedHeight;
    }

    @NoArgsConstructor
    @Getter
    @Setter
    public static class Dimension implements Serializable {

        private static final long serialVersionUID = 1L;

        public Dimension(ServiceResourceDto.Dimension d) {
            this.name         = d.getName();
            this.unit         = d.getUnit();
            this.defaultValue = d.getDefaultValue();
            this.values       = d.getValues();
        }

        private String name;

        private String unit;

        @JsonProperty("default")
        private String defaultValue;

        private List<String> values;
    }

    @NoArgsConstructor
    @Getter
    @Setter
    public static class TileSet implements Serializable {

        private static final long serialVersionUID = 1L;

        public TileSet(ServiceResourceDto.TileSet t) {
            this.identifier = t.getIdentifier();
            this.maxTileCol = t.getMaxTileCol();
            this.maxTileRow = t.getMaxTileRow();
            this.minTileCol = t.getMinTileCol();
            this.minTileRow = t.getMinTileRow();
            this.tileHeight = t.getTileHeight();
            this.tileWidth  = t.getTileWidth();
        }

        private String identifier;

        @JsonProperty("min_tile_row")
        private Integer minTileRow;

        @JsonProperty("max_tile_row")
        private Integer maxTileRow;

        @JsonProperty("min_tile_col")
        private Integer minTileCol;

        @JsonProperty("max_tile_col")
        private Integer maxTileCol;

        @JsonProperty("tile_height")
        private Integer tileHeight;

        @JsonProperty("tile_width")
        private Integer tileWidth;

    }

}
