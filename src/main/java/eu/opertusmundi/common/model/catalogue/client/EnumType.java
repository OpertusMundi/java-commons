package eu.opertusmundi.common.model.catalogue.client;

import java.util.Arrays;

import lombok.Getter;

public enum EnumType {

    RASTER("raster"), 
    SERVICE("service "),
    VECTOR("vector"),
    ;

    @Getter
    private final String value;

    private EnumType(String value) {
        this.value = value;
    }

    public static EnumType fromString(String value) {
        return Arrays.stream(EnumType.values())
            .filter(r -> r.value.equalsIgnoreCase(value))
            .findFirst()
            .orElseThrow(
                () -> new IllegalArgumentException(String.format("Value [%s] is not a member of enum EnumType", value))
            );
    }

}
