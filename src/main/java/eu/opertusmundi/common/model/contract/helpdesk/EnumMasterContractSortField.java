package eu.opertusmundi.common.model.contract.helpdesk;

import lombok.Getter;

public enum EnumMasterContractSortField {
    CREATED_ON("createdAt"),
    MODIFIED_ON("modifiedAt"),
    STATUS("status"),
    TITLE("title"),
    VERSION("version"),
    ;

    @Getter
    private String value;

    private EnumMasterContractSortField(String value) {
        this.value = value;
    }

    public static EnumMasterContractSortField fromValue(String value) {
        for (final EnumMasterContractSortField e : EnumMasterContractSortField.values()) {
            if (e.getValue().equals(value)) {
                return e;
            }
        }

        throw new IllegalArgumentException(String.format(
            "Value [%s] is not a valid member of enum [EnumMasterContractSortField]", value
        ));
    }
}
