package eu.opertusmundi.common.model.analytics;

import javax.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class GoogleAnalyticsQuery extends BaseQuery {

    public enum EnumMetric {
        COUNT_USERS,
        COUNT_SESSIONS
        ;
    }

    @Schema(description = "Aggregate to compute")
    @NotNull
    private EnumMetric metric;

}
