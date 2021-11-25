package eu.opertusmundi.common.model.logging;

import lombok.Getter;

public enum EnumEventSortField {
    APPLICATION("program-name"),
    CLIENT_ADDRESS("fromhost"),
    TIMESTAMP("timestamp"),
    ;

    @Getter
    private String value;

    private EnumEventSortField(String value) {
        this.value = value;
    }

}
