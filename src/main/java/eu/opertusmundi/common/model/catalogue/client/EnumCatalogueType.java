package eu.opertusmundi.common.model.catalogue.client;

import java.util.Arrays;

import lombok.Getter;

public enum EnumCatalogueType {

    OPERTUSMUNDI("opertusmundi"), 
    CKAN("ckan"), 
    CSW("csw"),
    ;

    @Getter
    private final String value;

    private EnumCatalogueType(String value) {
        this.value = value;
    }

    public static EnumCatalogueType fromString(String value) {
        return Arrays.stream(EnumCatalogueType.values())
            .filter(r -> r.value.equalsIgnoreCase(value))
            .findFirst()
            .orElseThrow(
                () -> new IllegalArgumentException(String.format("Value [%s] is not a member of enum EnumCatalogueType", value))
            );
    }
    
}
