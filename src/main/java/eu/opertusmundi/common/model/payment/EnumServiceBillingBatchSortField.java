package eu.opertusmundi.common.model.payment;

import lombok.Getter;

public enum EnumServiceBillingBatchSortField {
    CREATED_ON("createdOn"),
    DUE_DATE("dueDate"),
    FROM_DATE("fromDate"),
    STATUS("status"),
    TOTAL_PRICE("totalPrice"),
    UPDATED_ON("updatedOn"),
    ;

    @Getter
    private String value;

    private EnumServiceBillingBatchSortField(String value) {
        this.value = value;
    }

    public static EnumServiceBillingBatchSortField fromValue(String value) {
        for (final EnumServiceBillingBatchSortField e : EnumServiceBillingBatchSortField.values()) {
            if (e.getValue().equals(value)) {
                return e;
            }
        }

        throw new IllegalArgumentException(String.format("Value [%s] is not a valid member of enum [EnumSubscriptionBillingBatchSortField]", value));
    }
}