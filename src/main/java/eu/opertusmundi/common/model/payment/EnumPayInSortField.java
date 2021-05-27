package eu.opertusmundi.common.model.payment;

import lombok.Getter;

public enum EnumPayInSortField {
    EXECUTED_ON("executedOn"),
    REFERENCE_NUMBER("referenceNumber"),
    TOTAL_PRICE("totalPrice"),
    ;

    @Getter
    private String value;

    private EnumPayInSortField(String value) {
        this.value = value;
    }

    public static EnumPayInSortField fromValue(String value) {
        for (final EnumPayInSortField e : EnumPayInSortField.values()) {
            if (e.getValue().equals(value)) {
                return e;
            }
        }

        throw new IllegalArgumentException(String.format("Value [%s] is not a valid member of enum [EnumPayInSortField]", value));
    }
}