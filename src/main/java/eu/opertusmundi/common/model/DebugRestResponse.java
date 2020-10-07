package eu.opertusmundi.common.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import eu.opertusmundi.common.model.Message;
import io.swagger.v3.oas.annotations.media.Schema;

public class DebugRestResponse extends BaseResponse {

    private final String message;

    private final Exception exception;

    public DebugRestResponse(Message error, String message, Exception exception) {
        super(error);

        this.message   = message;
        this.exception = exception;
    }

    @Schema(
        description = "Exception message if development profile is enabled"
    )
    @JsonInclude(Include.NON_NULL)
    public String getMessage() {
        return this.message;
    }

    @Schema(
        description = "Exception object if development profile is enabled"
    )
    @JsonInclude(Include.NON_NULL)
    public Exception getException() {
        return this.exception;
    }
}
