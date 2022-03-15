package eu.opertusmundi.common.model.rating.client;

import java.math.BigDecimal;
import java.util.UUID;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "Asset rating command")
public class ClientAssetRatingCommandDto {

    @JsonIgnore
    @Getter
    @Setter
    private UUID account;

    @JsonIgnore
    @Getter
    @Setter
    private String asset;

    @Schema(
        description = "Rating value. Value may be a decimal with only a single fraction digit", minimum = "0.0", maximum = "5.0",
        example = "4.5"
    )
    @NotNull
    @DecimalMin(value = "0.0")
    @DecimalMax(value = "5.0")
    @Digits(integer = 1, fraction = 1)
    @Getter
    @Setter
    protected BigDecimal value;

    @Schema(description = "User comment")
    @Getter
    @Setter
    protected String comment;

}