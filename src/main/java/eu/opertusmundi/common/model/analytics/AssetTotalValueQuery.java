package eu.opertusmundi.common.model.analytics;

import javax.validation.Valid;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class AssetTotalValueQuery {

    @Schema(description = "Temporal dimension constraints")
    @JsonInclude(Include.NON_NULL)
    @Valid
    private TemporalDimension time;

}
