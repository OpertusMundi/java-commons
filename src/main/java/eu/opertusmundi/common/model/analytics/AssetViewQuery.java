package eu.opertusmundi.common.model.analytics;

import javax.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class AssetViewQuery extends BaseQuery {

    public enum EnumMetric {
        /**
         * Number of views
         */
        COUNT_VIEWS,
        ;
    }

    @Schema(description = "Source of data")
    @NotNull
    private EnumAssetViewSource source;

    @Schema(description = "Aggregate to compute")
    @NotNull
    private EnumMetric metric;

}
