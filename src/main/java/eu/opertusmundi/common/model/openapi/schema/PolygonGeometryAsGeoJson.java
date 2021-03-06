package eu.opertusmundi.common.model.openapi.schema;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PolygonGeometryAsGeoJson {

    @Schema(description = "Geometry type")
    EnumGeometryType type;

    @ArraySchema(
        schema = @Schema(description = "Geometry coordinates")
    )
    double[][][] coordinates;

}
