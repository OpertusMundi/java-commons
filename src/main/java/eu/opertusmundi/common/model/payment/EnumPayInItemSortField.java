package eu.opertusmundi.common.model.payment;

import lombok.Getter;

public enum EnumPayInItemSortField {
    CREATED_ON("payin.createdOn"),
    EXECUTED_ON("payin.executedOn"),
    MODIFIED_ON("payin.statusUpdatedOn"),
    REFERENCE_NUMBER("payin.referenceNumber"),
    STATUS("payin.status"),
    ;

    @Getter
    private String value;

    private EnumPayInItemSortField(String value) {
        this.value = value;
    }

    public static EnumPayInItemSortField fromValue(String value) {
        for (final EnumPayInItemSortField e : EnumPayInItemSortField.values()) {
            if (e.getValue().equals(value)) {
                return e;
            }
        }

        throw new IllegalArgumentException(String.format("Value [%s] is not a valid member of enum [EnumPayInSortField]", value));
    }
}