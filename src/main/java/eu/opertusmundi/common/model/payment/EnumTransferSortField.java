package eu.opertusmundi.common.model.payment;

import lombok.Getter;

public enum EnumTransferSortField {
    CREATED_ON("transfer.creationDate"),
    EXECUTED_ON("transfer.executionDate"),
    REFERENCE_NUMBER("payin.referenceNumber"),
    STATUS("transfer.transactionStatus"),
    FUNDS("transfer.creditedFunds"),
    FEES("transfer.fees"),
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