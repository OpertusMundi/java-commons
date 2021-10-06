package eu.opertusmundi.common.model.catalogue.client;

import java.util.Arrays;

import lombok.Getter;

public enum EnumAssetType {

    BUNDLE ("bundle" , false,  "A collection of existing assets"),
    NETCDF ("netcdf" , true,   "A set of data formats for array-oriented scientific data"),
    RASTER ("raster" , true,   "Raster dataset"),
    SERVICE("service", false,  "Service"),
    TABULAR("tabular", true,   "Tabular data"),
    VECTOR ("vector" , true,   "Vector dataset"),
    ;

    @Getter
    private final String value;

    @Getter
    private final String description;

    /**
     * True if this is a primary data source e.g. Vector data
     */
    @Getter
    private final boolean primary;

    private EnumAssetType(String value, boolean primary, String description) {
        this.value       = value;
        this.description = description;
        this.primary     = primary;
    }

    public static EnumAssetType fromString(String value) {
        return Arrays.stream(EnumAssetType.values())
            .filter(r -> r.value.equalsIgnoreCase(value))
            .findFirst()
            .orElse(null);
    }

}
