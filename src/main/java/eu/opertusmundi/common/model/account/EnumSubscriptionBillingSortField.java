package eu.opertusmundi.common.model.account;

import lombok.Getter;

public enum EnumSubscriptionBillingSortField {
    CREATED_ON("createdOn"),
    DUE_DATE("dueDate"),
    FROM_DATE("fromDate"),
    STATUS("status"),
    TO_DATE("toDate"),
    TOTAL_PRICE("totalPrice"),
    UPDATED_ON("updatedOn"),
    ;

    @Getter
    private String value;

    private EnumSubscriptionBillingSortField(String value) {
        this.value = value;
    }

    public static EnumSubscriptionBillingSortField fromValue(String value) {
        for (final EnumSubscriptionBillingSortField e : EnumSubscriptionBillingSortField.values()) {
            if (e.getValue().equals(value)) {
                return e;
            }
        }

        throw new IllegalArgumentException(String.format("Value [%s] is not a valid member of enum [EnumSubscriptionBillingSortField]", value));
    }

}