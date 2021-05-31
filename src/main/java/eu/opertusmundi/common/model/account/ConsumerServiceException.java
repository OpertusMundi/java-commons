package eu.opertusmundi.common.model.account;

import eu.opertusmundi.common.model.ServiceException;

public class ConsumerServiceException extends ServiceException {

    private static final long serialVersionUID = 1L;

    public ConsumerServiceException(ConsumerServiceMessageCode code, String message) {
        super(code, message);
    }

    public ConsumerServiceException(ConsumerServiceMessageCode code, String message, Throwable cause) {
        super(code, message, cause);
    }

}