package eu.opertusmundi.common.model.payment;

import lombok.Getter;

public enum EnumDisputeSortField {
    CONTEST_DEADLINE_ON("contestDeadlineDate"),
    CONTESTED_FUNDS("contestedFunds"),
    CREATED_ON("creationDate"),
    DISPUTED_FUNDS("disputedFunds"),
    REFERENCE_NUMBER("payin.referenceNumber"),
    STATUS("status"),
    ;

    @Getter
    private String value;

    private EnumDisputeSortField(String value) {
        this.value = value;
    }

    public static EnumDisputeSortField fromValue(String value) {
        for (final EnumDisputeSortField e : EnumDisputeSortField.values()) {
            if (e.getValue().equals(value)) {
                return e;
            }
        }

        throw new IllegalArgumentException(String.format("Value [%s] is not a valid member of enum [EnumDisputeSortField]", value));
    }
}