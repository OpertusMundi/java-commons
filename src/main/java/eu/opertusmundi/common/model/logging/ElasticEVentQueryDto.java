package eu.opertusmundi.common.model.logging;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.opertusmundi.common.model.EnumSortingOrder;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ElasticEVentQueryDto {

    @ArraySchema(
        arraySchema = @Schema(
            description = "Event level",
            example = "ERROR"
        ),
        minItems = 0, uniqueItems = true
    )
    private Set<EnumEventLevel> levels;

    @Schema(description = "Time interval start", example = "2020-06-01")
    private LocalDate fromDate;

    @Schema(description = "Time interval end", example = "2020-06-30")
    private LocalDate toDate;

    @Schema(description = "Remote host IP addresses")
    private List<String> clientAddresses;

    @Schema(description = "User names")
    private List<String> userNames;

    @Schema(description = "Applications")
    private List<String> applications;

    @Schema(description = "Loggers")
    private List<String> loggers;

    @Schema(description = "Pagination page index", defaultValue = "0")
    private Optional<Integer> page;

    @Schema(description = "Pagination page size", defaultValue = "10")
    private Optional<Integer> size;

    @Schema(description = "Sorting field", defaultValue = "TIMESTAMP")
    private EnumEventSortField orderBy;

    @Schema(description = "Sorting direction", defaultValue = "DESC")
    private Optional<EnumSortingOrder> order;

    @JsonIgnore
    public Integer getFrom() {
        if (page == null || size == null) {
            return 0;
        }
        return page.orElse(0) * size.orElse(10) < 0 ? 0 : page.orElse(0) * size.orElse(10);
    }

}
