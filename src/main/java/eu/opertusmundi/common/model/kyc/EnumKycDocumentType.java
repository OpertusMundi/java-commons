package eu.opertusmundi.common.model.kyc;

import com.mangopay.core.enumerations.KycDocumentType;

public enum EnumKycDocumentType {
    IDENTITY_PROOF,
    ADDRESS_PROOF,
    REGISTRATION_PROOF,
    ARTICLES_OF_ASSOCIATION,
    SHAREHOLDER_DECLARATION,
    ;

    public static EnumKycDocumentType from(KycDocumentType t) {
        return EnumKycDocumentType.valueOf(t.toString());
    }

}
