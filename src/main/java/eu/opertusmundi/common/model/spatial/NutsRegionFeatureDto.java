package eu.opertusmundi.common.model.spatial;

import org.locationtech.jts.geom.Geometry;

import com.fasterxml.jackson.annotation.JsonProperty;

import eu.opertusmundi.common.model.openapi.schema.PolygonGeometryAsGeoJson;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

public class NutsRegionFeatureDto implements Feature {

    public NutsRegionFeatureDto() {
        this.type = "Feature";
    }

    @JsonProperty
    public String getId() {
        return this.properties.getCode();
    }

    @Getter
    private String type;

    @Schema(implementation = PolygonGeometryAsGeoJson.class, description = "Geometry as GeoJSON")
    @Getter
    @Setter
    private Geometry geometry;

    @Getter
    @Setter
    private NutsRegionPropertiesDto properties = new NutsRegionPropertiesDto();

}
