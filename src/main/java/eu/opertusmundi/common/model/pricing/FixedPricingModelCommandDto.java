package eu.opertusmundi.common.model.pricing;

import java.math.BigDecimal;
import java.math.RoundingMode;

import javax.validation.constraints.Digits;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

public class FixedPricingModelCommandDto extends BasePricingModelCommandDto {

    private static final long serialVersionUID = 1L;

    public FixedPricingModelCommandDto() {
        super(EnumPricingModel.FIXED);
    }

    @Schema(description = "Number of years for included updates", required = true)
    @NotNull
    @Min(0)
    @Max(10)
    @Getter
    @Setter
    private Integer yearsOfUpdates;

    @Schema(description = "Price excluding tax", required = true)
    @NotNull
    @Digits(fraction = 2, integer = 6)
    @Getter
    @Setter
    protected BigDecimal totalPriceExcludingTax;
    
    public void validate() throws QuotationException {
        // No validation is required
    }
    
    public void validate(QuotationParametersDto params) throws QuotationException {
        // No validation is required
    }
    
    public  EffectivePricingModelDto compute(QuotationParametersDto params) {
        final EffectivePricingModelDto result    = EffectivePricingModelDto.from(this, params);
        final QuotationDto             quotation = new QuotationDto();
        
        quotation.setTaxPercent(params.getTaxPercent().intValue());
            
        quotation.setTotalPriceExcludingTax(this.totalPriceExcludingTax);

        quotation.setTax(quotation.getTotalPriceExcludingTax()
            .multiply(params.getTaxPercent())
            .divide(new BigDecimal(100))
            .setScale(2, RoundingMode.HALF_UP)
        );
            
        result.setQuotation(quotation);
        
        return result;
    }
    
}