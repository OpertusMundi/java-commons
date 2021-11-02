package eu.opertusmundi.common.model.account;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(enumAsRef = true)
public enum EnumActivationTokenType {

    ACCOUNT,
    CONSUMER,
    PROVIDER,
    VENDOR_ACCOUNT,
    ;

    public static EnumActivationTokenType fromString(String value) {
        for (final EnumActivationTokenType item : EnumActivationTokenType.values()) {
            if (item.name().equalsIgnoreCase(value)) {
                return item;
            }
        }
        return null;
    }

}
