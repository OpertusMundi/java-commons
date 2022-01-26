package eu.opertusmundi.common.model.payment;

/**
 *
 * Recurring payment status
 *
 * @see https://docs.mangopay.com/guide/recurring-payments
 */
public enum EnumRecurringPaymentStatus {
    /**
     * The registration has been created but not yet used for a recurring pay-in
     */
    CREATED,
    /**
     * The recurring payment is in progress
     */
    IN_PROGRESS,
    /**
     * An attempted pay-in was unsuccessfully authorized, and the end user must
     * authenticate. If the object has this status, you are obliged to notify
     * the end user and execute a CIT which they authenticate.
     */
    AUTHENTICATION_NEEDED,
    /**
     * The recurrence is ended. The object can no longer be modified or re-used.
     */
    ENDED,
    ;

    public static EnumRecurringPaymentStatus from(String value) {
        for (final EnumRecurringPaymentStatus e : EnumRecurringPaymentStatus.values()) {
            if (e.name().equals(value)) {
                return e;
            }
        }

        throw new IllegalArgumentException(String.format("Value [%s] is not a valid member of enum [EnumRecurringPaymentStatus]", value));
    }
}
