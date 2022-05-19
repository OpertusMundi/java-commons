package eu.opertusmundi.common.model.rating;

import java.math.BigDecimal;
import java.math.RoundingMode;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class AssetAverageRatingDto {

    public AssetAverageRatingDto(String pid, Double value) {
        super();

        this.pid   = pid;
        this.value = BigDecimal.valueOf(value).setScale(1, RoundingMode.HALF_EVEN);
    }

    @Schema(description = "Asset unique identifier (PID)", example = "topio.provider.100.VECTOR")
    private String pid;

    @Schema(description = "Rating value. Value may be a decimal with only a single fraction digit", minimum = "0.0", maximum = "5.0", example = "4.5")
    private BigDecimal value;

}
