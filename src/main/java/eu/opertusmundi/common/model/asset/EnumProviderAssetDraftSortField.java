package eu.opertusmundi.common.model.asset;

import lombok.Getter;

public enum EnumProviderAssetDraftSortField {
    CREATED_ON("createdOn"),
    MODIFIED_ON("modifiedOn"),
    PROVIDER("account.profile.provider.name"),
    STATUS("status"),
    TITLE("title"),
    VERSION("version"),
    ;

    @Getter
    private String value;

    private EnumProviderAssetDraftSortField(String value) {
        this.value = value;
    }

    public static EnumProviderAssetDraftSortField fromValue(String value) {
        for (final EnumProviderAssetDraftSortField e : EnumProviderAssetDraftSortField.values()) {
            if (e.getValue().equals(value)) {
                return e;
            }
        }

        throw new IllegalArgumentException(String.format("Value [%s] is not a valid member of enum [EnumProviderAssetDraftSortField]", value));
    }

}
