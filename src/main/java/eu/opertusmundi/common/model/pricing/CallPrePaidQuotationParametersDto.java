package eu.opertusmundi.common.model.pricing;

import java.io.Serializable;

import javax.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

public class CallPrePaidQuotationParametersDto extends QuotationParametersDto implements Serializable {

    private static final long serialVersionUID = 1L;

    protected CallPrePaidQuotationParametersDto() {
        super(EnumPricingModel.PER_CALL_WITH_PREPAID);
    }

    @Schema(description = "Selected prepaid tier index if feature is supported. If a tier is selected and the pricing "
                        + "model does not support prepaid tiers, quotation service will return a validation error")
    @Getter
    @Setter
    @NotNull
    private Integer prePaidTier;

}
