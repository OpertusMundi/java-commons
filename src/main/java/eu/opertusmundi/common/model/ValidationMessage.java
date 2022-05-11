package eu.opertusmundi.common.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
public class ValidationMessage extends Message {

    @Getter
    @EqualsAndHashCode.Include
    private final String field;

    @Getter
    @EqualsAndHashCode.Include
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
