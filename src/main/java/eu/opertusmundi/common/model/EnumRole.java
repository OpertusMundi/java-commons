package eu.opertusmundi.common.model;

import java.util.Arrays;

public enum EnumRole {

    ROLE_USER(1, "Default Role"),
    ROLE_ADMIN(2, "Administrator"),
    ROLE_DEVELOPER(3, "Developer"),
    ROLE_PROVIDER(4, "Provider"),
    ROLE_CONSUMER(5, "Consumer"),
    ROLE_HELPDESK(6, "Helpdesk User"),
    ;

    private final int    value;

    private final String description;

    private EnumRole(int value, String description) {
        this.value = value;
        this.description = description;
    }

    public int getValue() {
        return this.value;
    }

    public String getDescription() {
        return this.description;
    }

    public static EnumRole fromString(String value) {
        return Arrays.stream(EnumRole.values())
            .filter(r -> r.name().equalsIgnoreCase(value))
            .findFirst()
            .orElse(null);
    }

}
