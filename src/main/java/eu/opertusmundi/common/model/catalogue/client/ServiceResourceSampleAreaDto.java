package eu.opertusmundi.common.model.catalogue.client;

import java.io.Serializable;
import java.util.List;

import org.locationtech.jts.geom.Geometry;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
@Getter
@Setter
public class ServiceResourceSampleAreaDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "Resource unique identifier")
    private String id;

    @ArraySchema(
        arraySchema = @Schema(
            description = "Areas of interest (bounding boxes) for data sampling"
        ),
        minItems = 0,
        uniqueItems = true
    )
    private List<Geometry> areas;

}