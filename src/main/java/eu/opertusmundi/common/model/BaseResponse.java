package eu.opertusmundi.common.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import eu.opertusmundi.common.model.Message;
import eu.opertusmundi.common.model.Message.EnumLevel;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

public class BaseResponse {

    private final List<Message> messages = new ArrayList<Message>();

    protected BaseResponse() {
    }

    protected BaseResponse(Message message) {
        this.messages.add(message);
    }

    protected BaseResponse(List<Message> messages) {
        this.messages.addAll(messages);
    }

    @Schema(
        description = "True if operation was successful; Otherwise False.",
        example = "true"
    )
    @JsonProperty("success")
    public boolean getSuccess() {
        final Message error = this.messages.stream()
            .filter(m -> m.getLevel().getValue() > EnumLevel.WARN.getValue())
            .findAny()
            .orElse(null);

        return error == null;
    }

    @ArraySchema(
        arraySchema = @Schema(
            description = "Response messages"
        ),
        minItems = 0
    )
    public List<Message> getMessages() {
        return Collections.unmodifiableList(this.messages);
    }

}
