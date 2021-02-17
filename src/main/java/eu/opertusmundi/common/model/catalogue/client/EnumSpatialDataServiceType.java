package eu.opertusmundi.common.model.catalogue.client;

import java.util.Arrays;

import lombok.Getter;

public enum EnumSpatialDataServiceType {
   
    TMS("TMS"), 
    WMS("WMS"), 
    WFS("WFS"),
    WCS("WCS"),
    CSW("CSW"),
    DATA_API("Data API"),
    OGC_API("OGC API"),
    ;

    @Getter
    private final String value;

    private EnumSpatialDataServiceType(String value) {
        this.value = value;
    }

    public static EnumSpatialDataServiceType fromString(String value) {
        if (value == null) {
            return null;
        }
        return Arrays.stream(EnumSpatialDataServiceType.values())
            .filter(r -> r.value.equalsIgnoreCase(value))
            .findFirst()
            .orElseThrow(
                () -> new IllegalArgumentException(String.format("Value [%s] is not a member of enum EnumSpatialDataServiceType", value))
            );
    }

}
