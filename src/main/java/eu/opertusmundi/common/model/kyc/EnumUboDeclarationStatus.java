package eu.opertusmundi.common.model.kyc;

import com.mangopay.core.enumerations.UboDeclarationStatus;

public enum EnumUboDeclarationStatus {
    CREATED, 
    VALIDATION_ASKED, 
    INCOMPLETE, 
    VALIDATED, 
    REFUSED,
    ;

    public static EnumUboDeclarationStatus from(UboDeclarationStatus value) {
        return EnumUboDeclarationStatus.valueOf(value.toString());
    }
    
}
