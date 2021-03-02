package eu.opertusmundi.common.model.catalogue.client;

import java.util.Arrays;

import lombok.Getter;

public enum EnumDeliveryMethod {

    DIGITAL_PLATFORM("digital_platform"), 
    DIGITAL_PROVIDER("digital_provider"),
    PHYSICAL_PROVIDER("physical_provider"),
    ;

    @Getter
    private final String value;

    private EnumDeliveryMethod(String value) {
        this.value = value;
    }

    public static EnumDeliveryMethod fromString(String value) {
        return Arrays.stream(EnumDeliveryMethod.values())
            .filter(r -> r.value.equalsIgnoreCase(value))
            .findFirst()
            .orElse(null);
    }
    
}
