package eu.opertusmundi.common.model.account;

import lombok.Getter;

public enum EnumServiceBillingRecordSortField {
    CREATED_ON("createdOn"),
    DUE_DATE("dueDate"),
    FROM_DATE("fromDate"),
    STATUS("status"),
    TITLE("title"),
    TO_DATE("toDate"),
    TOTAL_PRICE("totalPrice"),
    UPDATED_ON("updatedOn"),
    ;

    @Getter
    private String value;

    EnumServiceBillingRecordSortField(String value) {
        this.value = value;
    }

    public static EnumServiceBillingRecordSortField fromValue(String value) {
        for (final EnumServiceBillingRecordSortField e : EnumServiceBillingRecordSortField.values()) {
            if (e.getValue().equals(value)) {
                return e;
            }
        }

        throw new IllegalArgumentException(String.format("Value [%s] is not a valid member of enum [EnumServiceBillingSortField]", value));
    }

}