package eu.opertusmundi.common.model.payment;

import lombok.Getter;

public enum EnumPayInSortField {
    CREATED_ON("createdOn"),
    EXECUTED_ON("executedOn"),
    MODIFIED_ON("statusUpdatedOn"),
    REFERENCE_NUMBER("referenceNumber"),
    STATUS("status"),
    TOTAL_PRICE("totalPrice"),
    USER_NAME("consumer.email"),
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