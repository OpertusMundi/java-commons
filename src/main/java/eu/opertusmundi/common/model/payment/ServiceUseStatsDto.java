package eu.opertusmundi.common.model.payment;

import java.util.UUID;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import org.springframework.util.Assert;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Schema(description = "An object that contains information about a service usage over a specific period")
@AllArgsConstructor
@Builder
@Getter
public class ServiceUseStatsDto {

    @Schema(description = "Subscriber account unique key")
    @NotNull
    private final UUID userKey;

    @Schema(description = "Asset unique PID")
    @NotEmpty
    private final UUID subscriptionKey;

    @Schema(description = "Number of service calls")
    @Builder.Default
    private int calls = 0;

    @Schema(description = "Number of rows returned by service calls")
    @Builder.Default
    private int rows = 0;

    public void decreaseRows(long rows) {
        this.rows -= rows;

        Assert.isTrue(rows >= 0, "Negative rows number");
    }

    public void decreaseCalls(long calls) {
        this.calls -= calls;

        Assert.isTrue(calls >= 0, "Negative calls number");
    }
}
