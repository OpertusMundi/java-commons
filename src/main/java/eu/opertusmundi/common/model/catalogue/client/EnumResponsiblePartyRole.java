package eu.opertusmundi.common.model.catalogue.client;

import java.util.Arrays;

import lombok.Getter;

public enum EnumResponsiblePartyRole {
   
    PUBLISHER("publisher"), 
    OWNER("owner"),
    CUSTODIAN("custodian"),
    USER("user"),
    DISTRIBUTOR("distributor"),
    ORIGINATOR("originator"),
    POINT_OF_CONTACT("point of contact"),
    PROCESSOR("processor"),
    AUTHOR("author"),   
    ;

    @Getter
    private final String value;

    private EnumResponsiblePartyRole(String value) {
        this.value = value;
    }

    public static EnumResponsiblePartyRole fromString(String value) {
        return Arrays.stream(EnumResponsiblePartyRole.values())
            .filter(r -> r.value.equalsIgnoreCase(value))
            .findFirst()
            .orElse(null);
    }

}
