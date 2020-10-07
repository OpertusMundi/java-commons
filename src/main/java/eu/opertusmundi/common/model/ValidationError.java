package eu.opertusmundi.common.model;

import java.util.List;

import eu.opertusmundi.common.model.Message;
import eu.opertusmundi.common.model.MessageCode;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
public class ValidationError extends Message {

    @Schema(
        description = "Validation failure reason"
    )
    private final String reason;

    @ArraySchema(
        arraySchema = @Schema(
            description = "Arguments used for formatting messages"
        ),
        minItems = 0
    )
    private final List<String> arguments;

    public ValidationError(MessageCode code, String description, String reason, List<String> arguments) {
        super(code, description, EnumLevel.WARN);

        this.reason    = reason;
        this.arguments = arguments;
    }

}
