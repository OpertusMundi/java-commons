package eu.opertusmundi.common.model.pricing;

import java.math.BigDecimal;

public class FreePricingModelCommandDto extends BasePricingModelCommandDto {

    private static final long serialVersionUID = 1L;

    public FreePricingModelCommandDto() {
        super(EnumPricingModel.FREE);
    }

    public void validate() throws QuotationException {
        // No validation is required
    }

    public void validate(QuotationParametersDto params) throws QuotationException {
        // No validation is required
    }

    public EffectivePricingModelDto compute(QuotationParametersDto params) {
        final EffectivePricingModelDto result    = EffectivePricingModelDto.from(this, params);
        final QuotationDto             quotation = new QuotationDto();

        quotation.setTaxPercent(params.getTaxPercent().intValue());
        quotation.setTotalPriceExcludingTax(new BigDecimal(0));
        quotation.setTax(new BigDecimal(0));

        result.setQuotation(quotation);

        return result;
    }

}