package eu.opertusmundi.common.model;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.swagger.v3.oas.annotations.media.Schema;

public class RestResponse<Result> extends BaseResponse {

    private Result result;

    protected RestResponse() {
        super();
    }

    protected RestResponse(Result result) {
        super();
        this.result  = result;
    }

    protected RestResponse(Message message) {
        super(message);
    }

    protected RestResponse(Result result, List<Message> messages) {
        super(messages);
        this.result = result;
    }

    @Schema(
        description = "Response result"
    )
    @JsonInclude(Include.NON_NULL)
    public Result getResult() {
        return this.result;
    }

    protected void setResult(Result result) {
        this.result = result;
    }

    public static <R> RestResponse<R> success() {
        return new RestResponse<>();
    }

    public static <R> RestResponse<R> failure() {
        return RestResponse.<R>error(new Message(BasicMessageCode.InternalServerError, "An error has occurred"));
    }

    public static <R> RestResponse<R> failure(MessageCode code, String description, Message.EnumLevel level) {
        return RestResponse.<R>failure(new Message(code, description, level));
    }

    public static <R> RestResponse<R> failure(MessageCode code, String description) {
        return failure(code, description, Message.EnumLevel.ERROR);
    }

    public static <R> RestResponse<R> failure(Message e) {
        return new RestResponse<R>(null, Collections.singletonList(e));
    }

    public static <R> RestResponse<R> failure(List<Message> errors) {
        return new RestResponse<R>(null, errors);
    }

    public static <R> RestResponse<R> failure(ServiceException ex) {
        if (!ex.getMessages().isEmpty()) {
            return failure(ex.getMessages());
        }

        return failure(ex.getCode(), ex.getMessage(), Message.EnumLevel.ERROR);
    }

    public static <R> RestResponse<R> accessDenied() {
        return RestResponse.<R>failure(new Message(BasicMessageCode.Unauthorized, "Access Denied", Message.EnumLevel.ERROR));
    }

    public static <R> RestResponse<R> notFound() {
        return RestResponse.<R>error(new Message(BasicMessageCode.NotFound, "No data found"));
    }

    public static <R> RestResponse<R> result(R r) {
        return new RestResponse<>(r);
    }

    public static <R> RestResponse<R> error(MessageCode code, String description, Message.EnumLevel level) {
        return RestResponse.<R>error(new Message(code, description, level));
    }

    public static <R> RestResponse<R> error(MessageCode code, String description) {
        return error(code, description, Message.EnumLevel.ERROR);
    }

    public static <R> RestResponse<R> error(Message e) {
        return new RestResponse<R>(null, Collections.singletonList(e));
    }

    public static <R> RestResponse<R> error(List<Message> errors) {
        return new RestResponse<R>(null, errors);
    }

    public static <R> RestResponse<R> invalid(List<FieldError> fieldErrors) {
        return invalid(fieldErrors, null);
    }

    public static <R> RestResponse<R> invalid(List<FieldError> fieldErrors, List<ObjectError> globalErrors) {
        final List<Message> messages = fieldErrors.stream()
            .map(e -> {
                 return new ValidationMessage(
                    e.getField(),
                    e.getCode(),
                    e.getRejectedValue() instanceof byte[] ? null : e.getRejectedValue(),
                    e.getArguments()
                );
            }).collect(Collectors.toList());

        if (globalErrors != null) {
            globalErrors.stream()
                .map(e -> {
                    return new ValidationMessage(null, e.getCode(), e.getDefaultMessage(), e.getArguments());
                })
                .forEach(messages::add);
        }

        return RestResponse.<R>error(messages);
    }
}
