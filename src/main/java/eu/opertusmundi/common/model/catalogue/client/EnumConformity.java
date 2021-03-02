package eu.opertusmundi.common.model.catalogue.client;

import java.util.Arrays;

import lombok.Getter;

public enum EnumConformity {

    CONFORMANT("conformant"), 
    NOT_CONFORMANT("not conformant"), 
    NOT_EVALUATED("not evaluated"),
    ;

    @Getter
    private final String value;

    private EnumConformity(String value) {
        this.value = value;
    }

    public static EnumConformity fromString(String value) {
        return Arrays.stream(EnumConformity.values())
            .filter(r -> r.value.equalsIgnoreCase(value))
            .findFirst()
            .orElse(EnumConformity.NOT_EVALUATED);
    }

}
