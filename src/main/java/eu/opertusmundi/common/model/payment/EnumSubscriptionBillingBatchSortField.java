package eu.opertusmundi.common.model.payment;

import lombok.Getter;

public enum EnumSubscriptionBillingBatchSortField {
    CREATED_ON("createdOn"),
    DUE_DATE("dueDate"),
    FROM_DATE("fromDate"),
    STATUS("status"),
    TOTAL_PRICE("totalPrice"),
    UPDATED_ON("updatedOn"),
    ;

    @Getter
    private String value;

    private EnumSubscriptionBillingBatchSortField(String value) {
        this.value = value;
    }

    public static EnumSubscriptionBillingBatchSortField fromValue(String value) {
        for (final EnumSubscriptionBillingBatchSortField e : EnumSubscriptionBillingBatchSortField.values()) {
            if (e.getValue().equals(value)) {
                return e;
            }
        }

        throw new IllegalArgumentException(String.format("Value [%s] is not a valid member of enum [EnumSubscriptionBillingBatchSortField]", value));
    }
}