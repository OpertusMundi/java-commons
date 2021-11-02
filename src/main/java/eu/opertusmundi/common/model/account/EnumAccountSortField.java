package eu.opertusmundi.common.model.account;

import lombok.Getter;

public enum EnumAccountSortField {
    EMAIL("email"),
    FIRST_NAME("firstName"),
    LAST_NAME("lastName"),
    ;

    @Getter
    private String value;

    private EnumAccountSortField(String value) {
        this.value = value;
    }

    public static EnumAccountSortField fromValue(String value) {
        for (final EnumAccountSortField e : EnumAccountSortField.values()) {
            if (e.getValue().equals(value)) {
                return e;
            }
        }

        throw new IllegalArgumentException(String.format(
            "Value [%s] is not a valid member of enum [EnumAccountSortField]", value
        ));
    }

}
