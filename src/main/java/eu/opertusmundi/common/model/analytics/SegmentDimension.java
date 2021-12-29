package eu.opertusmundi.common.model.analytics;

import java.util.List;

import eu.opertusmundi.common.model.catalogue.client.EnumTopicCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SegmentDimension {

    @Schema(description = "True if grouping based on segment codes must be performed")
    private boolean enabled;

    @Schema(description = "If one or more segments are selected, data will be filtered using the specified segments")
    private List<EnumTopicCategory> segments;

}
