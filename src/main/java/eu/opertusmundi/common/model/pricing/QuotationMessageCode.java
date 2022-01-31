package eu.opertusmundi.common.model.pricing;

import eu.opertusmundi.common.model.MessageCode;

public enum QuotationMessageCode implements MessageCode {
    ERROR,
    ASSET_NOT_FOUND,
    PRICING_MODEL_NOT_FOUND,
    PRICING_MODEL_NOT_IMPLEMENTED,
    INVALID_PARAMETER_TYPE,
    PARAMETER_IS_MISSING,
    PARAMETER_NOT_APPLICABLE,
    PRE_PAID_TIER_NOT_SET,
    PRE_PAID_TIER_NOT_FOUND,
    DISCOUNT_RATE_NOT_FOUND,
    NO_ROWS_SELECTED,
    NO_POPULATION_SELECTED,
    NO_NUTS_SELECTED,
    DISCOUNT_RATE_COUNT_ORDER,
    DISCOUNT_RATE_DISCOUNT_ORDER,
    PREPAID_COUNT_ORDER,
    PREPAID_DISCOUNT_ORDER,
    SUBSCRIPTION_FREQUENCY_NOT_SET,
    SUBSCRIPTION_FREQUENCY_NOT_SUPPORTED,
    SUBSCRIPTION_EXISTS,
    ;

    @Override
    public String key() {
        return this.getClass().getSimpleName() + '.' + this.name();
    }

}
