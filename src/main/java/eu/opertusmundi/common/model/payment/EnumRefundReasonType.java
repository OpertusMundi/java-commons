package eu.opertusmundi.common.model.payment;

import com.mangopay.core.enumerations.RefundReasonType;

public enum EnumRefundReasonType {
    /**
     * Not specified.
     */
    NotSpecified,
    /**
     * Incorrect bank account.
     */
    BANKACCOUNT_INCORRECT,
    /**
     * Closed bank account.
     */
    BANKACCOUNT_HAS_BEEN_CLOSED,
    /**
     * Owner-bank account mismatch.
     */
    OWNER_DOT_NOT_MATCH_BANKACCOUNT,
    /**
     * Withdrawal impossible on savings accounts.
     */
    WITHDRAWAL_IMPOSSIBLE_ON_SAVINGS_ACCOUNTS,
    /**
     * Initialized by client.
     */
    INITIALIZED_BY_CLIENT,
    /**
     * Other.
     */
    OTHER
    ;

    public static EnumRefundReasonType from(RefundReasonType t) throws PaymentException {
        for (final EnumRefundReasonType item : EnumRefundReasonType.values()) {
            if (item.name().equalsIgnoreCase(t.name())) {
                return item;
            }
        }
        throw new PaymentException(
            PaymentMessageCode.ENUM_MEMBER_NOT_SUPPORTED,
            String.format("Refund reason type [%s] is not supported", t)
        );
    }

}
