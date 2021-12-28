package eu.opertusmundi.common.model.payment;

public enum EnumRecurringPaymentType {
    /**
     * Simple PayIn record
     */
    NONE,
    /**
     * Customer-initiated transaction for initializing or authenticating a
     * recurring payment
     */
    CIT,
    /**
     * Merchant-initiated transaction for an in-progress recurring payment
     * registration
     */
    MIT,
    ;
}
