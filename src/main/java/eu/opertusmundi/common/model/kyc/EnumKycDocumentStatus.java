package eu.opertusmundi.common.model.kyc;

import com.mangopay.core.enumerations.KycStatus;

public enum EnumKycDocumentStatus {
    CREATED,
    VALIDATION_ASKED,
    VALIDATED,
    REFUSED,
    OUT_OF_DATE,
    ;

    public static EnumKycDocumentStatus from(KycStatus value) {
        return EnumKycDocumentStatus.valueOf(value.toString());
    }

}
