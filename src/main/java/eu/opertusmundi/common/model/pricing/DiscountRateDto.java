package eu.opertusmundi.common.model.pricing;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import eu.opertusmundi.common.support.BigDecimalSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Schema(description = "Discount rate")
@Getter
@Setter
public class DiscountRateDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "Threshold at which the discount is applied. The field may refer to different "
                        + "units e.g. number of rows, thousands of service calls, thousands of people, etc. "
                        + "The unit type is determined by the parent pricing model")
    @NotNull
    @Min(1)
    private Long count;

    @Schema(description = "Discount percent as an 2 digit integer")
    @Digits(integer = 3, fraction = 2)
    @DecimalMin("1.00")
    @DecimalMax("100.00")
    @NotNull
    @JsonSerialize(using = BigDecimalSerializer.class)
    private BigDecimal discount;

}
