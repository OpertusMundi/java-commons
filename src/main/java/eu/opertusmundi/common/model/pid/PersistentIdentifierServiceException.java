package eu.opertusmundi.common.model.pid;

import eu.opertusmundi.common.model.ServiceException;

public class PersistentIdentifierServiceException extends ServiceException {

    private static final long serialVersionUID = 1L;

    public PersistentIdentifierServiceException(PersistentIdentifierServiceMessageCode code) {
        super(code, "[Persistent Identifier Service] Operation has failed");
    }

    public PersistentIdentifierServiceException(PersistentIdentifierServiceMessageCode code, String message) {
        super(code, message);
    }

    public PersistentIdentifierServiceException(PersistentIdentifierServiceMessageCode code, String message, Throwable cause) {
        super(code, message, cause);
    }

}