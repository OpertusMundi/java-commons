package eu.opertusmundi.common.model.contract.provider;

import lombok.Getter;

public enum EnumProviderContractSortField {
    CREATED_ON("createdAt"),
    MODIFIED_ON("modifiedAt"),
    STATUS("status"),
    TITLE("title"),
    VERSION("version"),
    ;

    @Getter
    private String value;

    private EnumProviderContractSortField(String value) {
        this.value = value;
    }

    public static EnumProviderContractSortField fromValue(String value) {
        for (final EnumProviderContractSortField e : EnumProviderContractSortField.values()) {
            if (e.getValue().equals(value)) {
                return e;
            }
        }

        throw new IllegalArgumentException(String.format(
            "Value [%s] is not a valid member of enum [EnumProviderContractSortField]", value
        ));
    }
}
