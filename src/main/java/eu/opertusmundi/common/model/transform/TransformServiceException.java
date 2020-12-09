package eu.opertusmundi.common.model.transform;

import eu.opertusmundi.common.model.ServiceException;

public class TransformServiceException extends ServiceException {

    private static final long serialVersionUID = 1L;

    public TransformServiceException(TransformServiceMessageCode code) {
        super(code, "Operation failed");
    }

    public TransformServiceException(TransformServiceMessageCode code, String message) {
        super(code, message);
    }

    public TransformServiceException(TransformServiceMessageCode code, String message, Throwable cause) {
        super(code, message, cause);
    }

}
