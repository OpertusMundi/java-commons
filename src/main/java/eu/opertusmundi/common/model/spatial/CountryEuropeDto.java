package eu.opertusmundi.common.model.spatial;

import org.locationtech.jts.geom.Geometry;

import eu.opertusmundi.common.model.openapi.schema.MultiPolygonGeometryAsGeoJson;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CountryEuropeDto {

    @Schema(description = "The country ISO 3166-1 alpha-2 code")
    private String code;

    private String name;

    @Schema(description = "The bounding box of the country", implementation = MultiPolygonGeometryAsGeoJson.class)
    private Geometry geometry;

}
