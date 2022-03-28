package eu.opertusmundi.common.model.pricing;

import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public final class EffectivePricingModelDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "The pricing model")
    private BasePricingModelCommandDto model;

    @Schema(description = "User-defined parameters applied to the pricing model for computing the effective pricing model")
    private QuotationParametersDto userParameters;

    @Schema(description = "System-defined parameters applied to the pricing model for computing the effective pricing model")
    private SystemQuotationParametersDto systemParameters;

    @Schema(description = "Quotation data. May be null if the pricing model is dynamic i.e. `FIXED_FOR_POPULATION` and parameters are missing")
    private QuotationDto quotation;

    public static EffectivePricingModelDto from(
        BasePricingModelCommandDto model, QuotationParametersDto userParameters, SystemQuotationParametersDto systemParameters
    ) {
        return EffectivePricingModelDto.from(model, userParameters, systemParameters, null);
    }

    public static EffectivePricingModelDto from(
        BasePricingModelCommandDto model, QuotationParametersDto userParameters, SystemQuotationParametersDto systemParameters, QuotationDto quotation
    ) {
        final EffectivePricingModelDto e = new EffectivePricingModelDto();

        e.setModel(model);
        e.setUserParameters(userParameters);
        e.setSystemParameters(systemParameters);
        e.setQuotation(quotation);

        return e;
    }

}
