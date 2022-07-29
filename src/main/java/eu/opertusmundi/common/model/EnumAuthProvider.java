package eu.opertusmundi.common.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(enumAsRef = true)
public enum EnumAuthProvider {
    Forms       (false),
    Google      (true),
    GitHub      (true),
    OpertusMundi(false),
    ;

    private final boolean external;

    private EnumAuthProvider(boolean external) {
        this.external = external;
    }

    public boolean isExternal() {
        return this.external;
    }

    public static EnumAuthProvider fromString(String value) {
        for (final EnumAuthProvider item : EnumAuthProvider.values()) {
            if (item.name().equalsIgnoreCase(value)) {
                return item;
            }
        }
        return null;
    }

}
