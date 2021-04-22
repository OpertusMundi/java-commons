package eu.opertusmundi.common.model.spatial;

import org.locationtech.jts.geom.Geometry;

import com.fasterxml.jackson.annotation.JsonProperty;

import eu.opertusmundi.common.model.openapi.schema.MultiPolygonGeometryAsGeoJson;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

public class NutsRegionFeatureDto implements Feature {

    public NutsRegionFeatureDto() {
        this.type = "Feature";
    }

    @Schema(description = "Feature identifier (equals to NUTS code)")
    @JsonProperty
    public String getId() {
        return this.properties.getCode();
    }

    @Schema(description = "Object type. Always equal to `Feature`")
    @Getter
    private String type;

    @Schema(implementation = MultiPolygonGeometryAsGeoJson.class, description = "Geometry as GeoJSON")
    @Getter
    @Setter
    private Geometry geometry;

    @Schema(description = "Feature properties")
    @Getter
    @Setter
    private NutsRegionPropertiesDto properties = new NutsRegionPropertiesDto();

}
