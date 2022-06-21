package eu.opertusmundi.common.model.pricing;

import java.math.BigDecimal;
import java.math.RoundingMode;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import eu.opertusmundi.common.model.payment.ServiceUseStatsDto;
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
    @DecimalMin(value = "0.00", inclusive = false)
    @Digits(integer = 6, fraction = 2)
    @Getter
    @Setter
    protected BigDecimal totalPriceExcludingTax;

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

        final QuotationDto quotation = new QuotationDto();

        quotation.setTaxPercent(systemParams.getTaxPercent().intValue());
        quotation.setTotalPriceExcludingTax(this.totalPriceExcludingTax);
        quotation.setTax(quotation.getTotalPriceExcludingTax()
            .multiply(systemParams.getTaxPercent())
            .divide(new BigDecimal(100))
            .setScale(2, RoundingMode.HALF_UP)
        );

        return EffectivePricingModelDto.from(this, userParams, systemParams, quotation);
    }

    @Override
    public QuotationDto compute(ServiceUseStatsDto stats, SystemQuotationParametersDto systemParams) throws QuotationException {
        throw new QuotationException(QuotationMessageCode.QUOTATION_NOT_SUPPORTED, "Model does not support service statistics parameters");
    }

}
