package eu.opertusmundi.common.model.asset.service;

import java.util.Arrays;
import java.util.List;

import eu.opertusmundi.common.model.catalogue.client.EnumSpatialDataServiceType;
import lombok.Getter;

@Getter
public enum EnumUserServiceType {

    WMS("WMS", List.of(EnumSpatialDataServiceType.WMS)),
    WFS("WFS", List.of(EnumSpatialDataServiceType.WFS)),
    ;

    private final String                     value;
    private List<EnumSpatialDataServiceType> allowedOgcServiceTypes;

    EnumUserServiceType(String value, List<EnumSpatialDataServiceType> allowedOgcServiceTypes) {
        this.value                  = value;
        this.allowedOgcServiceTypes = allowedOgcServiceTypes;
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
