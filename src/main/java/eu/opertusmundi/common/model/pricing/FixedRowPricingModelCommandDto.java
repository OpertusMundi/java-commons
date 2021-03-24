package eu.opertusmundi.common.model.pricing;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

public class FixedRowPricingModelCommandDto extends BasePricingModelCommandDto {

    private static final long serialVersionUID = 1L;

    public FixedRowPricingModelCommandDto() {
        super(EnumPricingModel.FIXED_PER_ROWS);
    }

    @Schema(description = "The price prospective clients will pay per 1,000 rows")
    @NotNull
    @DecimalMin("0.01")
    @Digits(fraction = 2, integer = 6)
    @Getter
    @Setter
    private BigDecimal price;

    @Schema(description = "The minimum number of rows a client can purchase per transaction")
    @NotNull
    @Getter
    @Setter
    private Integer minRows;
    
    @ArraySchema(
        arraySchema = @Schema(
            description = "Discount rates based on the number of selected rows. Each element (except for the first one) "
                        + "must have a `count` property with a value greater than the previous one"
        ),
        minItems = 0,
        maxItems = 3,
        uniqueItems = true,
        schema = @Schema(description = "Discount rate", implementation = DiscountRateDto.class)
    )
    @Size(min = 0, max = 3)
    @Valid
    @Getter
    @Setter
    private List<DiscountRateDto> discountRates;

    public void validate() throws QuotationException {
        // No validation is required
    }
    
    public void validate(QuotationParametersDto params) throws QuotationException {
        // No validation is required
    }

    public  EffectivePricingModelDto compute(QuotationParametersDto params) {
        final EffectivePricingModelDto result = EffectivePricingModelDto.from(this, params);

        if (params.getSystemParams() != null && params.getSystemParams().getRows() != null) {
            final QuotationDto quotation = new QuotationDto();
            BigDecimal         discount  = BigDecimal.ZERO;

            quotation.setTaxPercent(params.getTaxPercent().intValue());
            
            if (this.discountRates != null) {
                for (DiscountRateDto r : this.discountRates) {
                    if (params.getSystemParams().getRows() > r.getCount()) {
                        discount = r.getDiscount();
                    }
                }
            }
            quotation.setTotalPriceExcludingTax(this.getPrice()
                .multiply(BigDecimal.valueOf(params.getSystemParams().getRows()))
                .multiply(BigDecimal.valueOf(100).subtract(discount))
                .divide(new BigDecimal(100))
                .setScale(2, RoundingMode.HALF_UP)
            );

            quotation.setTax(quotation.getTotalPriceExcludingTax()
                .multiply(params.getTaxPercent())
                .divide(new BigDecimal(100))
                .setScale(2, RoundingMode.HALF_UP)
            );
            
            result.setQuotation(quotation);
        }

        return result;
    }
    
}
