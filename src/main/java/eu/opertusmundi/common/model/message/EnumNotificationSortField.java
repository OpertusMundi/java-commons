package eu.opertusmundi.common.model.message;

import lombok.Getter;

public enum EnumNotificationSortField {
    SEND_AT("sendAt"),
    ;

    @Getter
    private String value;

    private EnumNotificationSortField(String value) {
        this.value = value;
    }

    public static EnumNotificationSortField fromValue(String value) {
        for (final EnumNotificationSortField e : EnumNotificationSortField.values()) {
            if (e.getValue().equals(value)) {
                return e;
            }
        }

        throw new IllegalArgumentException(String.format("Value [%s] is not a valid member of enum [EnumNotificationSortField]", value));
    }

}