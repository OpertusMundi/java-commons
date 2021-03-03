package eu.opertusmundi.common.model.asset;

import lombok.Getter;

public enum EnumProviderAssetSortField {
    PROVIDER("provider"),
    TITLE("title"),
    TYPE("type"),
    VERSION("version"),
    ;

    @Getter
    private String value;

    private EnumProviderAssetSortField(String value) {
        this.value = value;
    }

    public static EnumProviderAssetSortField fromValue(String value) {
        for (final EnumProviderAssetSortField e : EnumProviderAssetSortField.values()) {
            if (e.getValue().equals(value)) {
                return e;
            }
        }

        throw new IllegalArgumentException(String.format("Value [%s] is not a valid member of enum [EnumProviderAssetSortField]", value));
    }

}
