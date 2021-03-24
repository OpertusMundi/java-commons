package eu.opertusmundi.common.model.order;

public enum EnumOrderStatus {
    /**
     * Order created
     */
    CREATED,
    /**
     * PayIn created
     */
    CHARGED,
    /**
     * Order payment has been received, asset delivery/subscription registration is pending
     */
    PENDING,
    /**
     * Order has been cancelled, not payment received
     */
    CANCELLED,
    /**
     * Order has been cancelled and PayIn has been refunded
     */
    REFUNDED,
    /**
     * Order has been completed
     */
    SUCCEDEED,
    ;
}
