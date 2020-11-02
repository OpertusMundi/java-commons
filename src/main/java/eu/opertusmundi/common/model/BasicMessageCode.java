package eu.opertusmundi.common.model;

import java.util.Arrays;

import org.springframework.http.HttpStatus;

public enum BasicMessageCode implements MessageCode {
    // Error codes for 5XX HTTP status codes
    InternalServerError(HttpStatus.INTERNAL_SERVER_ERROR),

    // Error codes for 4XX HTTP status codes
    BadRequest(HttpStatus.BAD_REQUEST),
    Unauthorized(HttpStatus.UNAUTHORIZED),
    Forbidden(HttpStatus.FORBIDDEN),
    NotFound(HttpStatus.NOT_FOUND),

    // Validation error codes
    Validation(null),
    ValidationNotUnique(null),

    // Account
    AccountNotFound(null),
    EmailNotFound(null),

    // Activation Token
    TokenNotFound(null),
    TokenIsExpired(null),

    // Mail
    SendFailed(null),

    // Logging
    LogFailed(null),

    // Generic errors
    IOError(null),
    ;

    private final HttpStatus httpStatus;

    private BasicMessageCode(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
    }

    public static BasicMessageCode fromStatusCode(int value) {
        return Arrays.stream(BasicMessageCode.values())
            .filter(r -> r.httpStatus != null && value == r.httpStatus.value())
            .findFirst()
            .orElse(BasicMessageCode.InternalServerError);
    }

    public static BasicMessageCode fromStatusCode(HttpStatus value) {
        return Arrays.stream(BasicMessageCode.values())
            .filter(r -> r.httpStatus != null && value.equals(r.httpStatus))
            .findFirst()
            .orElse(BasicMessageCode.InternalServerError);
    }

    @Override
    public String key() {
        return this.getClass().getSimpleName() + '.' + this.name();
    }

}
