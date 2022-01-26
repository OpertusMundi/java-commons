package eu.opertusmundi.common.model.pricing;

import java.io.Serializable;

import javax.validation.constraints.NotNull;

import eu.opertusmundi.common.model.payment.EnumRecurringPaymentFrequency;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

public abstract class SubscriptionQuotationParameters extends QuotationParametersDto implements Serializable {

    private static final long serialVersionUID = 1L;

    protected SubscriptionQuotationParameters(EnumPricingModel type) {
        super(type);
    }

    @Schema(description = "Payment frequency for subscriptions", required = true)
    @Getter
    @Setter
    @NotNull
    private EnumRecurringPaymentFrequency frequency;

}
