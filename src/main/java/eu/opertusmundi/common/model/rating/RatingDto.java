package eu.opertusmundi.common.model.rating;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

public class RatingDto {

    @Schema(
        description = "Rating value. Value may be a decimal with only a single fraction digit", minimum = "0.0", maximum = "5.0",
        example = "4.5"
    )
    @Getter
    @Setter
    protected BigDecimal value;

    @Schema(description = "User comment")
    @Getter
    @Setter
    protected String comment;

    @Schema(description = "Rating date")
    @Getter
    @Setter
    protected ZonedDateTime createdAt;

}
