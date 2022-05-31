package eu.opertusmundi.common.model;

import java.util.Arrays;

import org.springframework.http.HttpStatus;

public enum BasicMessageCode implements MessageCode {
    // Error codes for 5XX HTTP status codes
    InternalServerError(HttpStatus.INTERNAL_SERVER_ERROR),

    // Error codes for 4XX HTTP status codes
    BadRequest(HttpStatus.BAD_REQUEST),
    PayloadTooLarge(HttpStatus.PAYLOAD_TOO_LARGE),
    Unauthorized(HttpStatus.UNAUTHORIZED),
    Forbidden(HttpStatus.FORBIDDEN),
    NotFound(HttpStatus.NOT_FOUND),

    // Configuration
    SettingNotFound,
    InvalidSettingType,

    // Validation error codes
    Validation,
    ValidationNotUnique,
    ForeignKeyConstraint,
    RecordNotFound,
    ReferenceNotFound,
    ValidationRequired,
    ValidationValueMismatch,

    // Account
    AccountNotFound,
    EmailNotFound,

    // Activation Token
    TokenNotFound,
    TokenIsExpired,
    TokenTypeNotSupported,

    // BPM Engine
    BpmServiceError,

    // Mail
    SendFailed,

    // Logging
    LogFailed,

    // Generic errors
    NotImplemented,
    IOError,

    // Success
    Success(HttpStatus.OK),
    ;

    private final HttpStatus httpStatus;

    public HttpStatus getHttpStatus() {
        return this.httpStatus == null ? HttpStatus.INTERNAL_SERVER_ERROR : this.httpStatus;
    }

    private BasicMessageCode() {
        this.httpStatus = null;
    }

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
