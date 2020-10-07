package eu.opertusmundi.common.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;

public class ValidationMessage extends Message {

    @Getter
    private final String field;

    @Getter
    private final Object value;

    @JsonIgnore
    @Getter
    private final Object[] arguments;

    public ValidationMessage(MessageCode code, String field, String reason, Object value, Object[] arguments) {
        super(code, reason, EnumLevel.ERROR);

        this.field     = field;
        this.value     = value;
        this.arguments = arguments;
    }

}