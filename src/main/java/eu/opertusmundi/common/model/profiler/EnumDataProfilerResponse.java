package eu.opertusmundi.common.model.profiler;

import lombok.Getter;

public enum EnumDataProfilerResponse {
    PROMPT("prompt"),
    DEFERRED("deferred"),
    ;

    @Getter
    private String value;

    private EnumDataProfilerResponse(String value) {
        this.value = value;
    }

    public static EnumDataProfilerResponse fromValue(String value) {
        for (final EnumDataProfilerResponse e : EnumDataProfilerResponse.values()) {
            if (e.getValue().equals(value)) {
                return e;
            }
        }

        throw new IllegalArgumentException(String.format("Value [%s] is not a valid member of enum [EnumDataProfilerResponse]", value));
    }

}
