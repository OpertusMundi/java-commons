package eu.opertusmundi.common.model.spatial;

import org.locationtech.jts.geom.Geometry;

import eu.opertusmundi.common.model.openapi.schema.PolygonGeometryAsGeoJson;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NutsRegionDto extends NutsRegionPropertiesDto {

    @Schema(implementation = PolygonGeometryAsGeoJson.class, description = "Geometry as GeoJSON")
    private Geometry geometry;

}
