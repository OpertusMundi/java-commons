package eu.opertusmundi.common.model.ingest;

import lombok.Getter;

public enum EnumIngestResponse {
    PROMPT("prompt"),
    DEFERRED("deferred"),
    ;

    @Getter
    private String value;

    private EnumIngestResponse(String value) {
        this.value = value;
    }

    public static EnumIngestResponse fromValue(String value) {
        for (final EnumIngestResponse e : EnumIngestResponse.values()) {
            if (e.getValue().equals(value)) {
                return e;
            }
        }

        throw new IllegalArgumentException(String.format("Value [%s] is not a valid member of enum [EnumIngestResponse]", value));
    }

}
