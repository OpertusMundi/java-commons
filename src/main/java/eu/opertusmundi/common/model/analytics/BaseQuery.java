package eu.opertusmundi.common.model.analytics;

import java.util.List;
import java.util.UUID;

import javax.validation.Valid;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class BaseQuery {

    @Schema(description = "Temporal dimension constraints")
    @JsonInclude(Include.NON_NULL)
    @Valid
    private TemporalDimension time;

    @Schema(description = "Spatial dimension constraints")
    @JsonInclude(Include.NON_NULL)
    @Valid
    private SpatialDimension areas;

    @Schema(description = "Segment dimension constraints")
    @JsonInclude(Include.NON_NULL)
    @Valid
    private SegmentDimension segments;

    @ArraySchema(
        arraySchema = @Schema(
            description = "One or more publishers for grouping data. This option is available only to platform "
                        + "administrators. For providers, the service automatically sets the value to the provider key"
        ),
        minItems = 0,
        uniqueItems = true
    )
    private List<UUID> publishers;

    @ArraySchema(
        arraySchema = @Schema(description = "One or more asset PIDs for grouping data"),
        minItems = 0,
        uniqueItems = true
    )
    private List<String> assets;

}
