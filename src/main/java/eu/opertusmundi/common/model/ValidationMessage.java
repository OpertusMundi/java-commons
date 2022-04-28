package eu.opertusmundi.common.model;

import lombok.Getter;

public class ValidationMessage extends Message {

    @Getter
    private final String field;

    @Getter
    private final Object value;

    @Getter
    private final Object[] arguments;

    public ValidationMessage(String field, String reason, Object value, Object[] arguments) {
        super(BasicMessageCode.Validation, reason, EnumLevel.ERROR);

        this.field     = field;
        this.value     = value;
        this.arguments = null;
    }

}
