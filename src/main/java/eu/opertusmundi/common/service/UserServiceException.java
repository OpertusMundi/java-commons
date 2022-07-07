package eu.opertusmundi.common.service;

import eu.opertusmundi.common.model.ServiceException;
import eu.opertusmundi.common.model.asset.UserServiceMessageCode;

public class UserServiceException extends ServiceException {

    private static final long serialVersionUID = 1L;

    public UserServiceException(UserServiceMessageCode code) {
        super(code, "An unhandled exception has occurred");
    }

    public UserServiceException(String message) {
        super(UserServiceMessageCode.ERROR, message);
    }

    public UserServiceException(String message, Throwable cause) {
        super(UserServiceMessageCode.ERROR, message, cause);
    }

    public UserServiceException(UserServiceMessageCode code, String message) {
        super(code, message);
    }

    public UserServiceException(UserServiceMessageCode code, String message, Throwable cause) {
        super(code, message, cause);
    }

}