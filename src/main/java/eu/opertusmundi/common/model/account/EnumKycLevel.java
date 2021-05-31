package eu.opertusmundi.common.model.account;

import com.mangopay.core.enumerations.KycLevel;

import eu.opertusmundi.common.model.payment.PaymentException;
import eu.opertusmundi.common.model.payment.PaymentMessageCode;

public enum EnumKycLevel {
    LIGHT,
    REGULAR,
    ;
    
    public static EnumKycLevel from(KycLevel l) throws PaymentException {
        switch (l) {
            case LIGHT :
                return EnumKycLevel.LIGHT;
            case REGULAR :
                return EnumKycLevel.REGULAR;
            default :
                throw new PaymentException(
                    PaymentMessageCode.ENUM_MEMBER_NOT_SUPPORTED,
                    String.format("KYC level [%s] is not supported", l)
                );
        }
    }
}
