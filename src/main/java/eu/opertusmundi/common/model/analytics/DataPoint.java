package eu.opertusmundi.common.model.analytics;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class DataPoint<T> {

    @Schema(description = "The time instant the data point refers to. If no temporal constraint is specified in "
                        + "the query, this property is not set")
    @JsonInclude(Include.NON_NULL)
    private TimeInstant time;

    @Schema(description = "The location the data point refers to. If no spatial constraint is specified in the "
                        + "query, this property is not set")
    @JsonInclude(Include.NON_NULL)
    private Location location;

    @Schema(description = "Asset segment")
    @JsonInclude(Include.NON_NULL)
    private String segment;

    @Schema(description = "The publisher key the data point refers to. If no publisher constraint is specified "
                        + "in the query, this property is not set. If a user is not an administrator, this property "
                        + "is never set.")
    @JsonInclude(Include.NON_NULL)
    private UUID publisher;

    @Schema(description = "The asset PID the data point refers to. If no asset constraint is specified in the "
                        + "query, this property is not set")
    @JsonInclude(Include.NON_NULL)
    private String asset;

    @Schema(description = "The value of the data point")
    private T value;

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    @Builder
    @Schema(description = "An object that represents a time instant. Depending on the time unit specified by a "
                        + "query, properties are set up to the specified time granularity e.g. if time unit is `MONTH`, "
                        + "only properties `year` and `month` are present.")
    public static class TimeInstant {

        @Schema(description = "The year")
        private Integer year;

        @Schema(description = "The month of the year")
        @JsonInclude(Include.NON_NULL)
        private Integer month;

        @Schema(description = "The week of the year")
        @JsonInclude(Include.NON_NULL)
        private Integer week;

        @Schema(description = "The day of the month")
        @JsonInclude(Include.NON_NULL)
        private Integer day;
    }

    @Getter
    @Setter
    @Schema(description = "An object that represents a country with its centroid")
    public static class Location {

        @Schema(description = "Country code in ISO 3166-1 alpha-2 format")
        private String code;

        @Schema(description = "Longitude")
        @JsonInclude(Include.NON_NULL)
        private Double lon;

        @Schema(description = "Latitude")
        @JsonInclude(Include.NON_NULL)
        private Double lat;

        public static Location of(String code) {
            final DataPoint.Location l = new DataPoint.Location();
            l.setCode(code);
            return l;
        }
    }

}
