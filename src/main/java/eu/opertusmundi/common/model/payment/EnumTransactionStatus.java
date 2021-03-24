package eu.opertusmundi.common.model.payment;

import com.mangopay.core.enumerations.TransactionStatus;

public enum EnumTransactionStatus {
    /**
     * New payment, no PayIn has been created yet
     */
    CREATED,
    /**
     * PayIn failed
     */
    FAILED,
    /**
     * PayIn succeeded
     */
    SUCCEEDED,
    ;

    public static EnumTransactionStatus from(TransactionStatus s) throws PaymentException {
        switch (s) {
            case CREATED :
                return EnumTransactionStatus.CREATED;
            case FAILED :
                return EnumTransactionStatus.FAILED;
            case SUCCEEDED :
                return EnumTransactionStatus.SUCCEEDED;
            default :
                throw new PaymentException(
                    PaymentMessageCode.ENUM_MEMBER_NOT_SUPPORTED,
                    String.format("Transaction status [%s] is not supported", s)
                );
        }
    }

}
