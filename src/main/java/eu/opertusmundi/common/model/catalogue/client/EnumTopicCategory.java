package eu.opertusmundi.common.model.catalogue.client;

import java.util.Arrays;

import lombok.Getter;

public enum EnumTopicCategory {

    BIOTA                    ("Biota"), 
    BOUNDARIES               ("Boundaries"), 
    CLIMA                    ("Climatology / Meteorology / Atmosphere"),
    ECONOMY                  ("Economy"), 
    ELEVATION                ("Elevation"), 
    ENVIRONMENT              ("Environment"), 
    FARMING                  ("Farming"), 
    GEO_SCIENTIFIC           ("Geoscientific Information"), 
    HEALTH                   ("Health"), 
    IMAGERY                  ("Imagery / Base Maps / Earth Cover"), 
    INLAND_WATERS            ("Inland Waters"), 
    INTELLIGENCE_MILITARY    ("Intelligence / Military"), 
    LOCATION                 ("Location"), 
    OCEANS                   ("Oceans"), 
    PLANNING_CADASTRE        ("Planning / Cadastre"),
    SOCIETY                  ("Society"),
    STRUCTURE                ("Structure"),
    TRANSPORTATION           ("Transportation"),
    UTILITIES_COMMUNICATION  ("Utilities / Communication"),
   ;

    @Getter
    private final String value;

    private EnumTopicCategory(String value) {
        this.value = value;
    }

    public static EnumTopicCategory fromString(String value) {
        return Arrays.stream(EnumTopicCategory.values())
            .filter(r -> r.value.equalsIgnoreCase(value))
            .findFirst()
            .orElseThrow(
                () -> new IllegalArgumentException(String.format("Value [%s] is not a member of enum EnumTopicCategory", value))
            );
    }

}
