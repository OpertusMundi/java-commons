package eu.opertusmundi.common.model.payment;

import com.mangopay.core.enumerations.FundsType;

public enum EnumFundsType {
    /**
     * Default type.
     */
    DEFAULT,
    /**
     * Fees type.
     */
    FEES,
    /**
     * Credit type.
     */
    CREDIT,
    ;

    public static EnumFundsType from(FundsType t) throws PaymentException {
        switch (t) {
            case DEFAULT :
                return EnumFundsType.DEFAULT;
            case FEES :
                return EnumFundsType.FEES;
            case CREDIT :
                return EnumFundsType.CREDIT;
            default :
                throw new PaymentException(
                    PaymentMessageCode.ENUM_MEMBER_NOT_SUPPORTED,
                    String.format("FundsType [%s] is not supported", t)
                );
        }
    }
}
