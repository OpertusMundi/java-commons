package eu.opertusmundi.common.model.payment;

import lombok.Getter;

public enum EnumTransferSortField {
    CREATED_ON("transferCreatedOn"),
    EXECUTED_ON("transferExecutedOn"),
    REFERENCE_NUMBER("payin.referenceNumber"),
    STATUS("transferStatus"),
    FUNDS("transferCreditedFunds"),
    FEES("transferFees"),
    ;

    @Getter
    private String value;

    private EnumTransferSortField(String value) {
        this.value = value;
    }

    public static EnumTransferSortField fromValue(String value) {
        for (final EnumTransferSortField e : EnumTransferSortField.values()) {
            if (e.getValue().equals(value)) {
                return e;
            }
        }

        throw new IllegalArgumentException(String.format("Value [%s] is not a valid member of enum [EnumTransferSortField]", value));
    }
}