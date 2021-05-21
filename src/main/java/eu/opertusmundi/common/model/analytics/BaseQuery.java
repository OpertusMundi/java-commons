package eu.opertusmundi.common.model.analytics;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import eu.opertusmundi.common.model.catalogue.client.EnumTopicCategory;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class BaseQuery {

    public enum EnumTemporalUnit {
        YEAR,
        MONTH,
        WEEK,
        DAY,
        ;
    }

    @Getter
    @Setter
    public static class TemporalDimension {

        @Schema(description = "Time interval unit", required = true)
        @NotNull
        private EnumTemporalUnit unit;

        @Schema(description = "Min date in YYYY-MM-DD ISO format")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        private Date min;

        @Schema(description = "Max date in YYYY-MM-DD ISO format")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        private Date max;

    }

    @Getter
    @Setter
    public static class SpatialDimension {

        @Schema(description = "True if grouping based on country codes must be performed")
        private boolean enabled;

        @Schema(description = "Country codes in ISO 3166-1 alpha-2 format. If one or more codes are "
                            + "specified, data will be filtered using the specified codes")
        private List<String> codes;

    }

    @Getter
    @Setter
    public static class SegmentDimension {

        @Schema(description = "True if grouping based on segment codes must be performed")
        private boolean enabled;

        @Schema(description = "If one or more segments are selected, data will be filtered using the "
                            + "specified segments")
        private List<EnumTopicCategory> segments;

    }

    @Schema(description = "Temporal dimension constraints")
    @JsonInclude(Include.NON_NULL)
    private TemporalDimension time;

    @Schema(description = "Spatial dimension constraints")
    @JsonInclude(Include.NON_NULL)
    private SpatialDimension areas;

    @Schema(description = "Segment dimension constraints")
    @JsonInclude(Include.NON_NULL)
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
