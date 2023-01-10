package eu.opertusmundi.common.model.payment;

import com.mangopay.core.enumerations.InitialTransactionType;
import com.mangopay.core.enumerations.TransactionType;

public enum EnumTransactionType {
    /**
     * Not specified.
     */
    NotSpecified,
    /**
     * PAYIN transaction type.
     */
    PAYIN,
    /**
     * PAYOUT transaction type.
     */
    PAYOUT,
    /**
     * TRANSFER transaction type.
     */
    TRANSFER
    ;

    public static EnumTransactionType from(InitialTransactionType t) throws PaymentException {
        for (final EnumTransactionType item : EnumTransactionType.values()) {
            if (item.name().equalsIgnoreCase(t.name())) {
                return item;
            }
        }
        throw new PaymentException(
            PaymentMessageCode.ENUM_MEMBER_NOT_SUPPORTED,
            String.format("Transaction type [%s] is not supported", t)
        );
    }

    public static EnumTransactionType from(TransactionType t) throws PaymentException {
        for (final EnumTransactionType item : EnumTransactionType.values()) {
            if (item.name().equalsIgnoreCase(t.name())) {
                return item;
            }
        }
        throw new PaymentException(
            PaymentMessageCode.ENUM_MEMBER_NOT_SUPPORTED,
            String.format("Transaction type [%s] is not supported", t)
        );
    }

}
