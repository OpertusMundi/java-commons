package eu.opertusmundi.common.model.asset;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(enumAsRef = true)
public enum EnumAssetAdditionalResource {

    /**
     * Invalid resource type
     */
    UNDEFINED,
    /**
     * File resource
     */
    FILE,
    /**
     * URI resource
     */
    URI,
    ;

    public static EnumAssetAdditionalResource fromString(String value) {
        for (final EnumAssetAdditionalResource item : EnumAssetAdditionalResource.values()) {
            if (item.name().equalsIgnoreCase(value)) {
                return item;
            }
        }
        return null;
    }

}
