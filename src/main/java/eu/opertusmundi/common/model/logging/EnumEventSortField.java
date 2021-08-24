package eu.opertusmundi.common.model.logging;

import lombok.Getter;

public enum EnumEventSortField {
    CLIENT_ADDRESS("clientAddress"),
    TIMESTAMP("generated"),
    USER_NAME("userName"),
    ;

    @Getter
    private String value;

    private EnumEventSortField(String value) {
        this.value = value;
    }

}
