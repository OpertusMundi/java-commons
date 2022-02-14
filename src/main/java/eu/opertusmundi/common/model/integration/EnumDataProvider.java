package eu.opertusmundi.common.model.integration;

import eu.opertusmundi.common.model.EnumRole;
import lombok.Getter;

public enum EnumDataProvider {
    UNDEFINED   (null,                          "None"), 
    SENTINEL_HUB(EnumRole.ROLE_SENTINEL_HUB,    "Sentinel Hub by SINERGISE"),
    ;

    @Getter
    private final EnumRole requiredRole;

    @Getter
    private final String name;

    EnumDataProvider(String name) {
        this.name         = name;
        this.requiredRole = null;
    }

    EnumDataProvider(EnumRole requiredRole, String name) {
        this.name         = name;
        this.requiredRole = requiredRole;
    }

}
