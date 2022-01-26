package eu.opertusmundi.common.model.pricing.integration;

import java.io.Serializable;

import eu.opertusmundi.common.model.pricing.EnumPricingModel;
import eu.opertusmundi.common.model.pricing.SubscriptionQuotationParameters;

public class SHSubscriptionQuotationParametersDto extends SubscriptionQuotationParameters implements Serializable {

    private static final long serialVersionUID = 1L;

    protected SHSubscriptionQuotationParametersDto() {
        super(EnumPricingModel.SENTINEL_HUB_SUBSCRIPTION);
    }

}
