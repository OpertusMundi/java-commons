package eu.opertusmundi.common.model.pricing.integration;

import com.fasterxml.jackson.databind.JsonNode;

import eu.opertusmundi.common.model.pricing.BasePricingModelCommandDto;
import eu.opertusmundi.common.model.pricing.EffectivePricingModelDto;
import eu.opertusmundi.common.model.pricing.EnumContinent;
import eu.opertusmundi.common.model.pricing.EnumPricingModel;
import eu.opertusmundi.common.model.pricing.QuotationException;
import eu.opertusmundi.common.model.pricing.QuotationMessageCode;
import eu.opertusmundi.common.model.pricing.QuotationParametersDto;
import eu.opertusmundi.common.model.pricing.SystemQuotationParametersDto;
import lombok.Builder;

/**
 * Sentinel Hub subscription pricing model
 */
public class SHImagePricingModelCommandDto extends BasePricingModelCommandDto {

    private static final long serialVersionUID = 1L;

    protected SHImagePricingModelCommandDto() {
        super(EnumPricingModel.SENTINEL_HUB_IMAGES);
    }

    @Builder
    public SHImagePricingModelCommandDto(
        String key,
        String[] domainRestrictions,
        EnumContinent[] coverageRestrictionContinents,
        EnumContinent[] consumerRestrictionContinents,
        String[] coverageRestrictionCountries,
        String[] consumerRestrictionCountries
    ) {
        super(
            key, EnumPricingModel.SENTINEL_HUB_IMAGES,
            domainRestrictions,
            coverageRestrictionContinents,
            consumerRestrictionContinents,
            coverageRestrictionCountries,
            consumerRestrictionCountries
        );
    }

    @Override
    protected void checkUserParametersType(QuotationParametersDto params) throws QuotationException {
        // Pricing model and quotation parameters (if not null) must have the same type
        if (params != null && !(params instanceof SHImageQuotationParametersDto)) {
            throw new QuotationException(QuotationMessageCode.INVALID_PARAMETER_TYPE, String.format(
                "Invalid parameter type [expected=%s, found=%s]", this.getType(), params.getType()
            ));
        }
    }

    @Override
    public void validate() throws QuotationException {
        // No action is required
    }

    @Override
    public void validate(QuotationParametersDto params, boolean ignoreMissing) throws QuotationException {
        this.checkUserParametersType(params);

        final SHImageQuotationParametersDto typedParams = (SHImageQuotationParametersDto) params;
        final JsonNode                      query       = typedParams.getQuery();

        if (query == null && ignoreMissing) {
            return;
        }

        if (query == null) {
            throw new QuotationException(QuotationMessageCode.SUBSCRIPTION_FREQUENCY_NOT_SET, "Query is required");
        }
    }

    @Override
    public EffectivePricingModelDto compute(QuotationParametersDto userParams, SystemQuotationParametersDto systemParams) {
        this.checkUserParametersType(userParams);

        final EffectivePricingModelDto result = EffectivePricingModelDto.from(this, userParams, systemParams);

        return result;
    }

}
