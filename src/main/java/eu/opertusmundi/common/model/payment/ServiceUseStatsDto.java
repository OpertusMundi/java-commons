package eu.opertusmundi.common.model.payment;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import org.springframework.util.Assert;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Schema(description = "An object that contains information about a service usage over a specific period")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Getter
@ToString
public class ServiceUseStatsDto {

    @Schema(description = "Subscriber account unique key")
    @NotNull
    private EnumBillableServiceType type;

    @Schema(description = "Subscriber account unique key")
    @NotNull
    private UUID userKey;

    @Schema(description = "The subscription or private OGC service unique identifier")
    @NotEmpty
    private UUID serviceKey;

    @Schema(description = "Number of service calls")
    @Builder.Default
    private int calls = 0;

    @Schema(description = "Number of rows returned by service calls")
    @Builder.Default
    private int rows = 0;

    @Schema(description = "Number of service calls per client")
    @JsonInclude(Include.NON_EMPTY)
    private final Map<UUID, Integer> clientCalls = new HashMap<>();

    @Schema(description = "Number of rows returned by service calls per client")
    @JsonInclude(Include.NON_EMPTY)
    private final Map<UUID, Integer> clientRows = new HashMap<>();

    public void decreaseRows(long rows) {
        this.rows -= rows;

        Assert.isTrue(rows >= 0, "Negative rows number");
    }

    public void decreaseCalls(long calls) {
        this.calls -= calls;

        Assert.isTrue(calls >= 0, "Negative calls number");
    }

    public ServiceUseStatsDto shallowCopy() {
        final ServiceUseStatsDto s = new ServiceUseStatsDto();

        s.calls      = this.calls;
        s.rows       = this.rows;
        s.serviceKey = this.serviceKey;
        s.type       = this.type;
        s.userKey    = this.userKey;

        s.clientCalls.putAll(this.clientCalls);
        s.clientRows.putAll(this.clientRows);

        return s;
    }
}
