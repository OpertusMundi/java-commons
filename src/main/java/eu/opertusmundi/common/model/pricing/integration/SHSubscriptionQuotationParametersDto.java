package eu.opertusmundi.common.model.pricing.integration;

import java.io.Serializable;

import javax.validation.constraints.NotNull;

import eu.opertusmundi.common.model.payment.EnumRecurringPaymentFrequency;
import eu.opertusmundi.common.model.pricing.EnumPricingModel;
import eu.opertusmundi.common.model.pricing.QuotationParametersDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

public class SHSubscriptionQuotationParametersDto extends QuotationParametersDto implements Serializable {

    private static final long serialVersionUID = 1L;

    protected SHSubscriptionQuotationParametersDto() {
        super(EnumPricingModel.SENTINEL_HUB_SUBSCRIPTION);
    }

    @Schema(description = "Payment frequency for subscriptions", required = true)
    @Getter
    @Setter
    @NotNull
    private EnumRecurringPaymentFrequency frequency;

}
