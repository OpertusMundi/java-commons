package eu.opertusmundi.common.model.analytics;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SpatialDimension {

    @Schema(description = "True if grouping based on country codes must be performed")
    private boolean enabled;

    @Schema(description =
        "Country codes in ISO 3166-1 alpha-2 format. If one or more codes are "
      + "specified, data will be filtered using the specified codes"
    )
    private List<String> codes;

}