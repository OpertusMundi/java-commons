package eu.opertusmundi.common.model.contract;

import lombok.Getter;

public enum EnumContractFontStyle {
    REGULAR("REGUALR"),
    BOLD("BOLD"),
    ITALIC("ITALIC"),
    UNDERLINE("UNDERLINE")
    ;

    @Getter
    private String value;

    private EnumContractFontStyle(String value) {
        this.value = value;
    }

    public static EnumContractFontStyle fromValue(String value) {
        for (final EnumContractFontStyle e : EnumContractFontStyle.values()) {
            if (e.getValue().equals(value)) {
                return e;
            }
        }

        throw new IllegalArgumentException(String.format("Value [%s] is not a valid member of enum [EnumContractFontStyle]", value));
    }

}
