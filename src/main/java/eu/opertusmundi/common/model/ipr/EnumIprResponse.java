package eu.opertusmundi.common.model.ipr;

import lombok.Getter;

public enum EnumIprResponse {
    PROMPT("prompt"),
    DEFERRED("deferred"),
    ;

    @Getter
    private String value;

    private EnumIprResponse(String value) {
        this.value = value;
    }

    public static EnumIprResponse fromValue(String value) {
        for (final EnumIprResponse e : EnumIprResponse.values()) {
            if (e.getValue().equals(value)) {
                return e;
            }
        }

        throw new IllegalArgumentException(String.format("Value [%s] is not a valid member of enum [EnumIprResponse]", value));
    }

}
