package eu.opertusmundi.common.model.payment;

import eu.opertusmundi.common.model.MessageCode;

public enum PaymentMessageCode implements MessageCode {
    API_ERROR,
    ASSET_NOT_FOUND,
    CART_IS_EMPTY,
    CART_MAX_SIZE,
    ENUM_MEMBER_NOT_SUPPORTED,
    NON_ZERO_AMOUNT,
    ORDER_INVALID_STATUS,
    ORDER_NOT_FOUND,
    PAYIN_ASSET_TYPE_NOT_SUPPORTED,
    PAYIN_ITEM_TYPE_NOT_SUPPORTED,
    PLATFORM_CUSTOMER_NOT_FOUND,
    PRICING_MODEL_NOT_FOUND,
    PROVIDER_USER_NOT_FOUND,
    RESOURCE_NOT_FOUND,
    SERVER_ERROR,
    VALIDATION_ERROR,
    WEB_HOOK_NOT_SUPPORTED,
    ZERO_AMOUNT,
    ;

    @Override
    public String key() {
        return this.getClass().getSimpleName() + '.' + this.name();
    }

}
