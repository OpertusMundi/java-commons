package eu.opertusmundi.common.model.pricing;

import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public final class EffectivePricingModelDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "The pricing model", oneOf = {
        FreePricingModelCommandDto.class,
        FixedPricingModelCommandDto.class,
        FixedRowPricingModelCommandDto.class,
        FixedPopulationPricingModelCommandDto.class,
        CallPrePaidPricingModelCommandDto.class,
        CallBlockRatePricingModelCommandDto.class,
        RowPrePaidPricingModelCommandDto.class,
        RowBlockRatePricingModelCommandDto.class,
    })
    private BasePricingModelCommandDto model;

    @Schema(description = "Parameters applied to the pricing model for computing the effective pricing model")
    private QuotationParametersDto parameters;
    
    @Schema(description = "Quotation data. May be null if the pricing model is dynamic i.e. `FIXED_FOR_POPULATION` and parameters are missing")
    private QuotationDto quotation;

    public static EffectivePricingModelDto from(BasePricingModelCommandDto model, QuotationParametersDto params) {
        final EffectivePricingModelDto e = new EffectivePricingModelDto();

        e.setModel(model);
        e.setParameters(params);

        return e;
    }

}