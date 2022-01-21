package eu.opertusmundi.common.model.pricing;

import java.math.BigDecimal;

public class FreePricingModelCommandDto extends BasePricingModelCommandDto {

    private static final long serialVersionUID = 1L;

    public FreePricingModelCommandDto() {
        super(EnumPricingModel.FREE);
    }

    @Override
    protected void checkUserParametersType(QuotationParametersDto params) throws QuotationException {
        // Pricing model does not support user parameters. No action is required
    }

    @Override
    public void validate() throws QuotationException {
        // No validation is required
    }

    @Override
    public void validate(QuotationParametersDto params, boolean ignoreMissing) throws QuotationException {
        this.checkUserParametersType(params);

        // No validation is required
    }

    @Override
    public EffectivePricingModelDto compute(QuotationParametersDto userParams, SystemQuotationParametersDto systemParams) {
        this.checkUserParametersType(userParams);

        final QuotationDto             quotation = new QuotationDto();

        quotation.setTaxPercent(systemParams.getTaxPercent().intValue());
        quotation.setTotalPriceExcludingTax(new BigDecimal(0));
        quotation.setTax(new BigDecimal(0));

        return EffectivePricingModelDto.from(this, userParams, systemParams, quotation);
    }

}
