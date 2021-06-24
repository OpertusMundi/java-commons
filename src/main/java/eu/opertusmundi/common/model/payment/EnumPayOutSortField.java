package eu.opertusmundi.common.model.payment;

import lombok.Getter;

public enum EnumPayOutSortField {
    CREATED_ON("createdOn"),
    EXECUTED_ON("executedOn"),
    MODIFIED_ON("statusUpdatedOn"),
    BANKWIRE_REF("bankwireRef"),
    STATUS("status"),
    FUNDS("debitedFunds"),
    PROVIDER("provider.email"),
    ;

    @Getter
    private String value;

    private EnumPayOutSortField(String value) {
        this.value = value;
    }

    public static EnumPayOutSortField fromValue(String value) {
        for (final EnumPayOutSortField e : EnumPayOutSortField.values()) {
            if (e.getValue().equals(value)) {
                return e;
            }
        }

        throw new IllegalArgumentException(String.format("Value [%s] is not a valid member of enum [EnumPayOutSortField]", value));
    }
}