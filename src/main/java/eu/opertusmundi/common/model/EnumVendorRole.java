package eu.opertusmundi.common.model;

import java.util.Arrays;

/**
 * Vendor account specific roles
 *
 * The values of this enumeration must be a subset of the values in {@link EnumRole}
 */
public enum EnumVendorRole {

    /**
     * Organizational role for vendor accounts. Required for successful login
     */
    ROLE_VENDOR_USER,
    /**
     * Organizational role for vendor provider accounts
     */
    ROLE_VENDOR_PROVIDER,
    /**
     * Organizational role for vendor consumer accounts
     */
    ROLE_VENDOR_CONSUMER,
    /**
     * Organizational role for vendor analytics accounts
     */
    ROLE_VENDOR_ANALYTICS,
    ;

    public static EnumVendorRole fromString(String value) {
        return Arrays.stream(EnumVendorRole.values())
            .filter(r -> r.name().equalsIgnoreCase(value))
            .findFirst()
            .orElse(null);
    }

    public EnumRole toPlatformRole() {
        return Arrays.asList(EnumRole.values()).stream()
            .filter(v -> v.name().equals(this.name()))
            .findFirst()
            .orElse(null);
    }

}
