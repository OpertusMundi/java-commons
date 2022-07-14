package eu.opertusmundi.common.model;

import java.util.Arrays;

public enum EnumRole {
    // Marketplace roles

    /**
     * Default role. Required for successful login
     */
    ROLE_USER,
    /**
     * System tester
     */
    ROLE_TESTER,
    /**
     * Role for enabling additional features for development
     */
    ROLE_DEVELOPER,
    /**
     * Provider
     */
    ROLE_PROVIDER,
    /**
     * Provider for open datasets
     */
    ROLE_PROVIDER_OPEN_DATASET,
    /**
     * Consumer
     */
    ROLE_CONSUMER,
    /**
     * Helpdesk account
     */
    ROLE_HELPDESK,

    // Vendor organization roles

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

    // External data provider roles

    /**
     * Sentinel Hub {@link https://www.sentinel-hub.com/}
     */
    ROLE_SENTINEL_HUB,
    ;

    public static EnumRole fromString(String value) {
        return Arrays.stream(EnumRole.values())
            .filter(r -> r.name().equalsIgnoreCase(value))
            .findFirst()
            .orElse(null);
    }

}
