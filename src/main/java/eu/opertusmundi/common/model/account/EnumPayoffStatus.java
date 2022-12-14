package eu.opertusmundi.common.model.account;

public enum EnumPayoffStatus {
    /**
     * A billing record with 0 total price has been created
     */
    NO_CHARGE,
    /**
     * A billing record has been generated, but it hasn't been paid yet
     */
    DUE,
    /**
     * Payment method has been declined
     */
    FAILED,
    /**
     * Billing record has been paid
     */
    PAID,
    ;
}
