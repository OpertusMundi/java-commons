package eu.opertusmundi.common.model.message;

import lombok.Getter;

public enum EnumContactFormSortField {
    CREATED_AT("createdAt"),
    EMAIL("email"),
    STATUS("status"),
    ;

    @Getter
    private String value;

    private EnumContactFormSortField(String value) {
        this.value = value;
    }

    public static EnumContactFormSortField fromValue(String value) {
        for (final EnumContactFormSortField e : EnumContactFormSortField.values()) {
            if (e.getValue().equals(value)) {
                return e;
            }
        }

        throw new IllegalArgumentException(String.format("Value [%s] is not a valid member of enum [EnumContactFormSortField]", value));
    }
}