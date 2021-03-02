package eu.opertusmundi.common.model.catalogue.client;

import java.util.Arrays;

import lombok.Getter;

public enum EnumType {

    RASTER("raster", "Raster dataset"), 
    SERVICE("service", "Service"),
    VECTOR("vector", "Vector dataset"),
    ;

    @Getter
    private final String value;

    @Getter
    private final String description;

    private EnumType(String value, String description) {
        this.value       = value;
        this.description = description;
    }

    public static EnumType fromString(String value) {
        return Arrays.stream(EnumType.values())
            .filter(r -> r.value.equalsIgnoreCase(value))
            .findFirst()
            .orElse(null);
    }

}
