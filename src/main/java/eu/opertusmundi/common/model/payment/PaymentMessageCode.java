package eu.opertusmundi.common.model.payment;

import eu.opertusmundi.common.model.MessageCode;

public enum PaymentMessageCode implements MessageCode {
    NOT_IMPLEMENTED,
    ACCOUNT_NOT_FOUND,
    SUBSCRIPTION_NOT_FOUND,
    USER_SERVICE_NOT_FOUND,
    API_ERROR,
    ASSET_NOT_FOUND,
    ASSET_PROVIDER_NOT_KYC_VALIDATED,
    CART_IS_EMPTY,
    CART_MAX_SIZE,
    ENUM_MEMBER_NOT_SUPPORTED,
    NON_ZERO_AMOUNT,
    ORDER_INVALID_STATUS,
    ORDER_NOT_FOUND,
    PAYIN_NOT_FOUND,
    PAYIN_TYPE_NOT_SUPPORTED,
    PAYIN_ASSET_TYPE_NOT_SUPPORTED,
    PAYIN_ITEM_TYPE_NOT_SUPPORTED,
    PLATFORM_CUSTOMER_NOT_FOUND,
    PRICING_MODEL_NOT_FOUND,
    PROVIDER_USER_NOT_FOUND,
    RESOURCE_NOT_FOUND,
    REFUND_INVALID_TYPE,
    SERVER_ERROR,
    VALIDATION_ERROR,
    WEB_HOOK_NOT_SUPPORTED,
    ZERO_AMOUNT,
    CARD_NOT_FOUND,
    QUOTATION_ERROR,
    QUOTATION_INTERVAL_YEAR,
    QUOTATION_INTERVAL_MONTH,
    USE_STATS_NOT_READY,
    SUBSCRIPTION_BILLING_RUNNING,
    SUBSCRIPTION_BILLING_SELECTION_EMPTY,
    SERVICE_BILLING_RECORD_NOT_FOUND,
    SERVICE_BILLING_RECORD_INVALID_STATUS,
    SERVICE_BILLING_RECORD_ACCESS_DENIED,
    SERVICE_BILLING_RECORD_PAYOFF_ERROR,
    SERVICE_BILLING_RECORD_INVALID_ITEM_TYPE,
    ;

    @Override
    public String key() {
        return this.getClass().getSimpleName() + '.' + this.name();
    }

}
