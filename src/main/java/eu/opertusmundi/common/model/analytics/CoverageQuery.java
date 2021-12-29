package eu.opertusmundi.common.model.analytics;

import java.util.List;

import javax.validation.Valid;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import eu.opertusmundi.common.model.catalogue.client.EnumTopicCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class CoverageQuery {

    @Schema(description = "Temporal dimension constraints")
    @JsonInclude(Include.NON_NULL)
    @Valid
    private TemporalDimension time;

    @Schema(description = "If one or more segments are selected, data will be filtered using the specified segments")
    private List<EnumTopicCategory> segments;

}
