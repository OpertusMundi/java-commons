package eu.opertusmundi.common.model.pricing;

import lombok.Getter;

public enum EnumContinent {
    AFRICA("AF", "Africa"),
    NORTH_AMERICA("NA", "North America"),  
    OCEANIA("OC", "Oceania"),
    ANTARCTICA("AN", "Antarctica"),
    ASIA("AS", "Asia"),
    EUROPE("EU", "Europe"),
    SOUTH_AMERICA("SA", "South America"),
    ;

    @Getter
    public String code;

    @Getter
    public String description;

    EnumContinent(String code, String description) {
        this.code        = code;
        this.description = description;
    }
    
}
