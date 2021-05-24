package eu.opertusmundi.common.model.analytics;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class AssetQuery extends BaseQuery {

    public enum EnumMetric {
        /**
         * Number of views
         */
        COUNT_VIEWS
        ;
    }

    @Schema(description = "Source of data")
    private AssetViewRecord.EnumSource source;

    @Schema(description = "Aggregate to compute")
    private EnumMetric metric;

}
