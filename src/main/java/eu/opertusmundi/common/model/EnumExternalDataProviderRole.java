package eu.opertusmundi.common.model;

import java.util.Arrays;

import lombok.Getter;

/**
 * Roles for external data provider integration
 *
 * The values of this enumeration must be a subset of the values in {@link EnumRole}
 */
public enum EnumExternalDataProviderRole {

    /**
     * Sentinel Hub {@link https://www.sentinel-hub.com/}
     */
    ROLE_SENTINEL_HUB("Sentinel Hub data provider"),
    ;

    private EnumExternalDataProviderRole(String description) {
        this.description = description;
    }

    @Getter
    private String description;

    public static EnumExternalDataProviderRole fromString(String value) {
        return Arrays.stream(EnumExternalDataProviderRole.values())
            .filter(r -> r.name().equalsIgnoreCase(value))
            .findFirst()
            .orElse(null);
    }

    public EnumRole toPlatformRole() {
        return Arrays.asList(EnumRole.values()).stream()
            .filter(v -> v.name().equals(this.name()))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException(String.format("Cannot find [EnumRole] value for name [%s]", this.name())));
    }

}
