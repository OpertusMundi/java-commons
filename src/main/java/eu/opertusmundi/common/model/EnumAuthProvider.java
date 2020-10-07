package eu.opertusmundi.common.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(enumAsRef = true)
public enum EnumAuthProvider {
    Forms,
    Google,
    GitHub,
    OpertusMundi,
    ;

    public static EnumAuthProvider fromString(String value) {
        for (final EnumAuthProvider item : EnumAuthProvider.values()) {
            if (item.name().equalsIgnoreCase(value)) {
                return item;
            }
        }
        return null;
    }

}
