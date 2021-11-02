package eu.opertusmundi.common.model;

import java.util.Arrays;

public enum EnumRole {

    /**
     * Default role. Required for successful login
     */
    ROLE_USER,
    /**
     * Platform administrator
     */
    ROLE_ADMIN,
    /**
     * Role for enabling additional features for development
     */
    ROLE_DEVELOPER,
    /**
     * Provider
     */
    ROLE_PROVIDER,
    /**
     * Consumer
     */
    ROLE_CONSUMER,
    /**
     * Helpdesk account
     */
    ROLE_HELPDESK,
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

    public static EnumRole fromString(String value) {
        return Arrays.stream(EnumRole.values())
            .filter(r -> r.name().equalsIgnoreCase(value))
            .findFirst()
            .orElse(null);
    }

}
