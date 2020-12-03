package eu.opertusmundi.common.model.transform;

import lombok.Getter;

public class TransformServiceException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    @Getter
    private final TransformServiceMessageCode code;

    public TransformServiceException(TransformServiceMessageCode code) {
        super("Operation failed");

        this.code = code;
    }

    public TransformServiceException(TransformServiceMessageCode code, String message) {
        super(message);

        this.code = code;
    }

    public TransformServiceException(TransformServiceMessageCode code, String message, Throwable cause) {
        super(message, cause);

        this.code = code;
    }

}
