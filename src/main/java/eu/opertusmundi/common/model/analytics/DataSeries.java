package eu.opertusmundi.common.model.analytics;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DataSeries<T> {

    @Schema(description = "Request time unit")
    @JsonInclude(Include.NON_NULL)
    private BaseQuery.EnumTemporalUnit timeUnit;

    @ArraySchema(
        arraySchema = @Schema(
            description = "Data series points"
        ),
        minItems = 0,
        uniqueItems = true
    )
    private List<DataPoint<T>> points = new ArrayList<>();

    public static <T> DataSeries<T> empty() {
        return DataSeries.empty(null);
    }

    public static <T> DataSeries<T> empty(BaseQuery.EnumTemporalUnit timeUnit) {
        final DataSeries<T> result = new DataSeries<T>();
        result.setTimeUnit(timeUnit);
        return result;
    }

}
