package eu.opertusmundi.common.model.ingest;

import eu.opertusmundi.common.model.ServiceException;

public class IngestServiceException extends ServiceException {

	private static final long serialVersionUID = 1L;

	public IngestServiceException(IngestServiceMessageCode code) {
		super(code, "[Ingest Service] Operation has failed");
	}

	public IngestServiceException(IngestServiceMessageCode code, String message) {
		super(code, message);
	}

	public IngestServiceException(IngestServiceMessageCode code, String message, Throwable cause) {
		super(code, message, cause);
	}

}
