package eu.opertusmundi.common.model.payment;

import com.mangopay.core.enumerations.CardType;

public enum EnumCardType {

    CB_VISA_MASTERCARD,
    ;

    public static EnumCardType fromCardType(CardType t) {
        switch (t) {
            case CB_VISA_MASTERCARD :
                return CB_VISA_MASTERCARD;
            default :
                return null;
        }
    }

}
