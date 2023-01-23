package eu.opertusmundi.common.model.payment;

import lombok.Getter;

public enum EnumRefundSortField {
    CREATED_ON("creationDate"),
    CREDITED_FUNDS("creditedFunds"),
    DEBITED_FUNDS("debitedFunds"),
    EXECUTED_ON("executionDate"),
    REASON_TYPE("refundReasonType"),
    REFERENCE_NUMBER("referenceNumber"),
    STATUS("status"),
    ;

    @Getter
    private String value;

    private EnumRefundSortField(String value) {
        this.value = value;
    }

    public static EnumRefundSortField fromValue(String value) {
        for (final EnumRefundSortField e : EnumRefundSortField.values()) {
            if (e.getValue().equals(value)) {
                return e;
            }
        }

        throw new IllegalArgumentException(String.format("Value [%s] is not a valid member of enum [EnumRefundSortField]", value));
    }
}