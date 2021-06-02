package eu.opertusmundi.common.model.analytics;

import javax.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class SalesQuery extends BaseQuery {

    public enum EnumMetric {
        COUNT_TRANSACTIONS,
        SUM_SALES
        ;
    }

    @Schema(description = "Aggregate to compute")
    @NotNull
    private EnumMetric metric;

}
