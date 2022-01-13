package eu.opertusmundi.common.model;

import lombok.Getter;

public enum EnumReferenceType {
    ORDER   ("00"),
    PAYIN   ("01"),
    PAYOUT  ("02"),
    ;

    private EnumReferenceType(String prefix) {
        this.prefix = prefix;
    }

    @Getter
    private String prefix;
}
