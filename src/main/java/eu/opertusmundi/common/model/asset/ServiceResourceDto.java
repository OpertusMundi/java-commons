package eu.opertusmundi.common.model.asset;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.util.Assert;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import eu.opertusmundi.common.model.catalogue.client.EnumSpatialDataServiceType;
import eu.opertusmundi.common.model.catalogue.server.CatalogueResource;
import eu.opertusmundi.common.util.StreamUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class ServiceResourceDto extends ResourceDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonCreator
    @Builder
    public ServiceResourceDto(
        @JsonProperty("id") UUID id,
        @JsonProperty("parentId") UUID parentId,
        @JsonProperty("serviceType") EnumSpatialDataServiceType serviceType,
        @JsonProperty("endpoint") String endpoint,
        @JsonProperty("crs") List<String> crs,
        @JsonProperty("styles") List<String> styles,
        @JsonProperty("bbox") Geometry bbox,
        @JsonProperty("dimensions") List<Dimension> dimensions,
        @JsonProperty("outputFormats") List<String> outputFormats,
        @JsonProperty("filterCapabilities") List<String> filterCapabilities,
        @JsonProperty("attribution") String attribution,
        @JsonProperty("minScale") Integer minScale,
        @JsonProperty("maxScale") Integer maxScale,
        @JsonProperty("tileSets") List<Object> tileSets
    ) {
        super(id, parentId, EnumResourceType.SERVICE);

        this.serviceType        = serviceType;
        this.endpoint           = endpoint;
        this.crs                = crs;
        this.styles             = styles;
        this.bbox               = bbox;
        this.dimensions         = dimensions;
        this.outputFormats      = outputFormats;
        this.filterCapabilities = filterCapabilities;
        this.attribution        = attribution;
        this.minScale           = minScale;
        this.maxScale           = maxScale;
        this.tileSets           = tileSets;
    }

    public ServiceResourceDto(CatalogueResource r) {
        this.attributes         = Optional.of(r.getAttributes())
            .map(ServiceResourceDto.Attributes::new)
            .orElse(null);
        this.attribution        = r.getAttribution();
        this.bbox               = r.getBbox();
        this.crs                = r.getCrs();
        this.dimensions         = StreamUtils.from(r.getDimension())
            .map(ServiceResourceDto.Dimension::new)
            .collect(Collectors.toList());
        this.endpoint           = r.getEndpoint();
        this.filterCapabilities = r.getFilterCapabilities();
        this.id                 = r.getId();
        this.maxScale           = r.getMaxScale();
        this.minScale           = r.getMinScale();
        this.outputFormats      = r.getOutputFormats();
        this.parentId           = r.getParentId();
        this.serviceType        = r.getServiceType();
        this.styles             = r.getStyle();
        this.tileSets           = r.getTileSets();
        this.type               = r.getType();
    }

    @Schema(description = "Service type")
    private EnumSpatialDataServiceType serviceType;

    @Schema(description = "Service endpoint")
    private String endpoint;

    private Attributes attributes;

    private List<String> crs;

    private List<String> styles;

    private Geometry bbox;

    private List<Dimension> dimensions;

    private List<String> outputFormats;

    private List<String> filterCapabilities;

    private String attribution;

    private Integer minScale;

    private Integer maxScale;

    private List<Object> tileSets;

    @Override
    public void patch(ResourceDto r) {
        Assert.isTrue(r.getType() == this.type);

        final ServiceResourceDto resource = (ServiceResourceDto) r;
        // Id and type are immutable
        this.serviceType = resource.serviceType;
        this.endpoint    = resource.endpoint;
    }

    @Override
    public CatalogueResource toCatalogueResource() {
        return CatalogueResource.builder()
            .parentId(parentId)
            .serviceType(serviceType)
            .type(EnumResourceType.SERVICE)
            .attributes(Optional.ofNullable(attributes).map(CatalogueResource.Attributes::new).orElse(null))
            .attribution(attribution)
            .bbox(bbox)
            .crs(crs)
            .dimension(StreamUtils.from(dimensions).map(CatalogueResource.Dimension::new).collect(Collectors.toList()))
            .endpoint(endpoint)
            .filterCapabilities(filterCapabilities)
            .id(id)
            .maxScale(maxScale)
            .minScale(minScale)
            .outputFormats(outputFormats)
            .parentId(parentId)
            .serviceType(serviceType)
            .style(styles)
            .tileSets(tileSets)
            .build();
    }

    @NoArgsConstructor
    @Getter
    @Setter
    public static class Attributes {

        public Attributes(CatalogueResource.Attributes a) {
            this.cascaded    = a.isCascaded();
            this.fixedHeight = a.getFixedHeight();
            this.fixedWidth  = a.getFixedWidth();
            this.noSubsets   = a.isNoSubsets();
            this.opaque      = a.isOpaque();
            this.queryable   = a.isQueryable();
        }

        private boolean queryable;

        private boolean cascaded;

        private boolean opaque;

        @JsonProperty("no_subsets")
        private boolean noSubsets;

        @JsonProperty("fixed_width")
        private Integer fixedWidth;

        @JsonProperty("fixed_height")
        private Integer fixedHeight;
    }

    @NoArgsConstructor
    @Getter
    @Setter
    @Schema(description = "The dimensions of the resource (derived from WMS)")
    public static class Dimension {

        public Dimension(CatalogueResource.Dimension d) {
            this.name         = d.getName();
            this.unit         = d.getUnit();
            this.defaultValue = d.getDefaultValue();
            this.values       = d.getValues();
        }

        private String name;

        private String unit;

        private String defaultValue;

        private List<String> values;

    }

}
