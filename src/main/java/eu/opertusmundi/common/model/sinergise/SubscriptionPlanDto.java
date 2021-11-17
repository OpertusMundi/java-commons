package eu.opertusmundi.common.model.sinergise;

import java.math.BigDecimal;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.ArraySchema;
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
public class SubscriptionPlanDto {

    @Schema(description = "Subscription plan unique identifier")
    private String id;

    @Schema(description = "Title")
    private String title;

    @Schema(description = "Description")
    private String description;

    @Schema(description = "Reference to the title of another subscription plan which this plan extends")
    @JsonProperty("extends")
    private String parent;

    @Schema(description = "Billing options")
    private Billing billing;

    @Schema(description = "Array of features supported by the plan")
    @ArraySchema(
        arraySchema = @Schema(
            description = "Feature description"
        ),
        minItems = 1,
        uniqueItems = true
    )
    private List<String[]> features;

    @Schema(description = "Processing resources limits")
    private ProcessingUnits processingUnits;

    @Schema(description = "Request rate limits")
    private Requests requests;

    @Schema(description = "Subscription plan license")
    private String license;

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    @Builder
    public static class Billing {

        @Schema(description = "Price for annual billing")
        private BigDecimal annually;

        @Schema(description = "Price for monthly billing")
        private BigDecimal monthly;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    @Builder
    public static class ProcessingUnits {

        @Schema(description = "Units per month")
        private Long month;

        @Schema(description = "Units per minute")
        private Long minute;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    @Builder
    public static class Requests {

        @Schema(description = "Requests per month")
        private Long month;

        @Schema(description = "Requests per minute")
        private Long minute;
    }

}
