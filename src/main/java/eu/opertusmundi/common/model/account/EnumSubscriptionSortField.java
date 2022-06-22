package eu.opertusmundi.common.model.account;

import lombok.Getter;

public enum EnumSubscriptionSortField {
    ADDED_ON("addedOn"),
    CONSUMER("consumer.email"),
    MODIFIED_ON("updatedOn"),
    ORDER("order.referenceNumber"),
    PROVIDER("provider.email"),
    STATUS("status"),
    ;

    @Getter
    private String value;

    private EnumSubscriptionSortField(String value) {
        this.value = value;
    }

    public static EnumSubscriptionSortField fromValue(String value) {
        for (final EnumSubscriptionSortField e : EnumSubscriptionSortField.values()) {
            if (e.getValue().equals(value)) {
                return e;
            }
        }

        throw new IllegalArgumentException(String.format("Value [%s] is not a valid member of enum [EnumSubscriptionSortField]", value));
    }

}