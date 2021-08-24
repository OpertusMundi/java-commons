package eu.opertusmundi.common.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

public class DebugRestResponse extends BaseResponse {

    @Schema(
        description = "Exception message if development profile is enabled"
    )
    @JsonInclude(Include.NON_NULL)
    @Getter
    private final String message;

    @Schema(
        description = "Exception stack trace if development profile is enabled"
    )
    @JsonInclude(Include.NON_NULL)
    @Getter
    private final StackTraceElement[] stackTrace;

    public DebugRestResponse(Message error, String message, Exception exception) {
        super(error);

        this.message    = message;
        this.stackTrace = exception == null ? null : exception.getStackTrace();
    }

}
