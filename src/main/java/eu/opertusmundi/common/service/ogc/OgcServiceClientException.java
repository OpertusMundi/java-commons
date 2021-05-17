package eu.opertusmundi.common.service.ogc;

import eu.opertusmundi.common.model.ServiceException;

public class OgcServiceClientException extends ServiceException {

	private static final long serialVersionUID = 1L;

	public OgcServiceClientException(OgcServiceMessageCode code) {
		super(code, "Operation has failed");
	}

	public OgcServiceClientException(OgcServiceMessageCode code, String message) {
		super(code, message);
	}

	public OgcServiceClientException(OgcServiceMessageCode code, String message, Throwable cause) {
		super(code, message, cause);
	}

}
