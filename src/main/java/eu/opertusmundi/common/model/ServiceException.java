package eu.opertusmundi.common.model;

import lombok.Getter;

public class ServiceException extends RuntimeException {

    private static final long serialVersionUID = 1L;

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

}