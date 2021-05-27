package eu.opertusmundi.common.model.payment;

import eu.opertusmundi.common.model.MessageCode;

public enum PaymentMessageCode implements MessageCode {
    API_ERROR,
    SERVER_ERROR,
    CART_IS_EMPTY,
    CART_MAX_SIZE,
    ASSET_NOT_FOUND,
    PRICING_MODEL_NOT_FOUND,
    ORDER_NOT_FOUND,
    PLATFORM_CUSTOMER_NOT_FOUND,
    PROVIDER_USER_NOT_FOUND,
    ENUM_MEMBER_NOT_SUPPORTED,
    PAYIN_ITEM_TYPE_NOT_SUPPORTED,
    ;

    @Override
    public String key() {
        return this.getClass().getSimpleName() + '.' + this.name();
    }

}
