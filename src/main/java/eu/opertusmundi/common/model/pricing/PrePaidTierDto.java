package eu.opertusmundi.common.model.pricing;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.validation.Valid;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import eu.opertusmundi.common.support.BigDecimalSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "Pre-Paid tire")
@Getter
@Setter
@Valid
public class PrePaidTierDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "Number of purchased units e.g. number of rows, thousands of service calls, etc. "
                        + "The unit type is determined by the parent pricing model")
    @NotNull
    @Min(1)
    private Long count;

    @Schema(description = "Discount percent")
    @Digits(integer = 3, fraction = 2)
    @DecimalMin(value = "0.00", inclusive = false)
    @DecimalMax(value = "100.00")
    @NotNull
    @JsonSerialize(using = BigDecimalSerializer.class)
    private BigDecimal discount;

}
