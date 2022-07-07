package eu.opertusmundi.common.model.asset.service;

import java.util.Arrays;

import lombok.Getter;

public enum EnumUserServiceType {

    WMS("WMS"),
    WFS("WFS"),
    ;

    @Getter
    private final String value;

    private EnumUserServiceType(String value) {
        this.value = value;
    }

    public static EnumUserServiceType fromString(String value) {
        if (value == null) {
            return null;
        }
        return Arrays.stream(EnumUserServiceType.values())
            .filter(r -> r.value.equalsIgnoreCase(value))
            .findFirst()
            .orElse(null);
    }

}
