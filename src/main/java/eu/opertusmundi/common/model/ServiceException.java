package eu.opertusmundi.common.model;

import org.apache.commons.lang3.exception.ExceptionUtils;

import lombok.Getter;

public class ServiceException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    @Getter
    protected boolean logEntryRequired = true;

    @Getter
    private final MessageCode code;

    public ServiceException(MessageCode code) {
        super("An error has occurred");

        this.code = code;
    }

    public ServiceException(String message) {
        super(message);

        this.code = BasicMessageCode.InternalServerError;
    }

    public ServiceException(MessageCode code, String message) {
        super(message);

        this.code = code;
    }

    public ServiceException(MessageCode code, String message, Throwable cause) {
        super(message, cause);

        this.code = code;
    }

    public Throwable getRootCause() {
        final Throwable result = ExceptionUtils.getRootCause(this);
        return result == null ? this : result;
    }

}