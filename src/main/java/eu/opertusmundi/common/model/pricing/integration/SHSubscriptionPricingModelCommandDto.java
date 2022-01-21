package eu.opertusmundi.common.model.pricing.integration;

import java.math.BigDecimal;
import java.util.UUID;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;

import eu.opertusmundi.common.model.payment.EnumRecurringPaymentFrequency;
import eu.opertusmundi.common.model.pricing.BasePricingModelCommandDto;
import eu.opertusmundi.common.model.pricing.EffectivePricingModelDto;
import eu.opertusmundi.common.model.pricing.EnumContinent;
import eu.opertusmundi.common.model.pricing.EnumPricingModel;
import eu.opertusmundi.common.model.pricing.QuotationException;
import eu.opertusmundi.common.model.pricing.QuotationMessageCode;
import eu.opertusmundi.common.model.pricing.QuotationParametersDto;
import eu.opertusmundi.common.model.pricing.SystemQuotationParametersDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Sentinel Hub subscription pricing model
 */
public class SHSubscriptionPricingModelCommandDto extends BasePricingModelCommandDto {

    private static final long serialVersionUID = 1L;

    public SHSubscriptionPricingModelCommandDto() {
        super(EnumPricingModel.SENTINEL_HUB_SUBSCRIPTION);
    }

    @Builder
    public SHSubscriptionPricingModelCommandDto(
        UUID key,
        String[] domainRestrictions,
        EnumContinent[] coverageRestrictionContinents,
        EnumContinent[] consumerRestrictionContinents,
        String[] coverageRestrictionCountries,
        String[] consumerRestrictionCountries,
        BigDecimal monthlyPriceExcludingTax,
        BigDecimal annualPriceExcludingTax
    ) {
        super(
            key, EnumPricingModel.SENTINEL_HUB_SUBSCRIPTION,
            domainRestrictions,
            coverageRestrictionContinents,
            consumerRestrictionContinents,
            coverageRestrictionCountries,
            consumerRestrictionCountries
        );

        this.annualPriceExcludingTax  = annualPriceExcludingTax;
        this.monthlyPriceExcludingTax = monthlyPriceExcludingTax;
    }

    @Schema(description = "Price excluding tax", required = true)
    @DecimalMin(value = "0.00", inclusive = false)
    @Digits(integer = 6, fraction = 2)
    @Getter
    @Setter
    private BigDecimal monthlyPriceExcludingTax;

    @Schema(description = "Price excluding tax", required = true)
    @DecimalMin(value = "0.00", inclusive = false)
    @Digits(integer = 6, fraction = 2)
    @Getter
    @Setter
    private BigDecimal annualPriceExcludingTax;

    @Override
    protected void checkUserParametersType(QuotationParametersDto params) throws QuotationException {
        // Pricing model and quotation parameters (if not null) must have the same type
        if (params != null && !(params instanceof SHSubscriptionQuotationParametersDto)) {
            throw new QuotationException(QuotationMessageCode.INVALID_PARAMETER_TYPE, String.format(
                "Invalid parameter type [expected=%s, found=%s]", this.getType(), params.getType()
            ));
        }
    }

    @Override
    public void validate() throws QuotationException {
        if (this.monthlyPriceExcludingTax == null && this.annualPriceExcludingTax == null) {
            throw new QuotationException(QuotationMessageCode.PARAMETER_IS_MISSING, "Either monthly or annual price must be set");
        }
    }

    @Override
    public void validate(QuotationParametersDto params, boolean ignoreMissing) throws QuotationException {
        this.checkUserParametersType(params);

        final SHSubscriptionQuotationParametersDto typedParams = (SHSubscriptionQuotationParametersDto) params;
        final EnumRecurringPaymentFrequency        frequency   = typedParams.getFrequency();

        if (frequency == null && ignoreMissing) {
            return;
        }

        if (frequency == null) {
            throw new QuotationException(QuotationMessageCode.SUBSCRIPTION_FREQUENCY_NOT_SET, "Billing frequency is required");
        }
    }

    @Override
    public EffectivePricingModelDto compute(QuotationParametersDto userParams, SystemQuotationParametersDto systemParams) {
        this.checkUserParametersType(userParams);

        // Always returns an empty quotation. When the user adds a new
        // subscription to the cart, the price will be computed using the
        // Sentinel Hub external data provider service
        return EffectivePricingModelDto.from(this, userParams, systemParams);
    }

}
