package eu.opertusmundi.common.model.analytics;

import javax.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class AssetTypeEarningsQuery extends BaseQuery {

    public enum EnumMetric {
        COUNT_TRANSACTIONS,
        SUM_SALES
        ;
    }
    
    public enum EnumDimension {
        FILE_ASSET,
        API,
        TOTAL,
        ALL_ASSET_TYPES
        ;
    }

    @Schema(description = "Aggregate to compute")
    @NotNull
    private EnumMetric metric;
    
    @Schema(description = "Dimension to be filtered")
    @NotNull
    private EnumDimension dimension;

}
