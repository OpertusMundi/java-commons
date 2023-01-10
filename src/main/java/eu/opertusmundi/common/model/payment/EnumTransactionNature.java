package eu.opertusmundi.common.model.payment;

import com.mangopay.core.enumerations.TransactionNature;

public enum EnumTransactionNature {
    /**
     * Not specified.
     */
    NotSpecified,
    /**
     * REGULAR transaction nature.
     */
    REGULAR,
    /**
     * REFUND transaction nature.
     */
    REFUND,
    /**
     * REPUDIATION transaction nature.
     */
    REPUDIATION,
    /**
     * SETTLEMENT transaction nature.
     */
    SETTLEMENT
    ;

    public static EnumTransactionNature from(TransactionNature n) throws PaymentException {
        for (final EnumTransactionNature item : EnumTransactionNature.values()) {
            if (item.name().equalsIgnoreCase(n.name())) {
                return item;
            }
        }
        throw new PaymentException(
            PaymentMessageCode.ENUM_MEMBER_NOT_SUPPORTED,
            String.format("Transaction nature [%s] is not supported", n)
        );
    }

}
