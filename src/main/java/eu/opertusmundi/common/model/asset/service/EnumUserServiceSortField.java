package eu.opertusmundi.common.model.asset.service;

import lombok.Getter;

public enum EnumUserServiceSortField {
    CREATED_ON("createdOn"),
    STATUS("status"),
    TITLE("title"),
    UPDATED_ON("updatedOn"),
    VERSION("version"),
    ;

    @Getter
    private String value;

    private EnumUserServiceSortField(String value) {
        this.value = value;
    }

    public static EnumUserServiceSortField fromValue(String value) {
        for (final EnumUserServiceSortField e : EnumUserServiceSortField.values()) {
            if (e.getValue().equals(value)) {
                return e;
            }
        }

        throw new IllegalArgumentException(String.format("Value [%s] is not a valid member of enum [EnumUserServiceSortField]", value));
    }

}
