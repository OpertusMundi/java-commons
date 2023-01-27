package eu.opertusmundi.common.model.ipr;

import eu.opertusmundi.common.model.ServiceException;

public class IprServiceException extends ServiceException {

	private static final long serialVersionUID = 1L;

    public IprServiceException(IprServiceMessageCode code, Throwable cause) {
        super(code, "[IPR Service] Operation has failed", cause);
    }

	public IprServiceException(IprServiceMessageCode code, String message) {
		super(code, message);
	}

	public IprServiceException(IprServiceMessageCode code, String message, Throwable cause) {
		super(code, message, cause);
	}

}
