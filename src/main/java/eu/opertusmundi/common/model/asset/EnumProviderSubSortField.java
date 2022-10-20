package eu.opertusmundi.common.model.asset;

import lombok.Getter;

public enum EnumProviderSubSortField {

    ADDED_ON("addedOn"),
    UPDATED_ON("updatedOn"),
    CONSUMER("consumer.email"),
    ;

    @Getter
    private String value;

    EnumProviderSubSortField(String value) {
        this.value = value;
    }

    public static EnumProviderSubSortField fromValue(String value) {
        for (final EnumProviderSubSortField e : EnumProviderSubSortField.values()) {
            if (e.getValue().equals(value)) {
                return e;
            }
        }

        throw new IllegalArgumentException(String.format("Value [%s] is not a valid member of enum [EnumProviderSubSortField]", value));
    }

}
