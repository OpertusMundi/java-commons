package eu.opertusmundi.common.model.profiler;

import eu.opertusmundi.common.model.ServiceException;

public class DataProfilerServiceException extends ServiceException {

	private static final long serialVersionUID = 1L;

	public DataProfilerServiceException(DataProfilerServiceMessageCode code) {
		super(code, "[Data Profiler Service] Operation has failed");
	}
	
   public DataProfilerServiceException(DataProfilerServiceMessageCode code, Throwable cause) {
        super(code, "[Data Profiler Service] Operation has failed", cause);
    }

	public DataProfilerServiceException(DataProfilerServiceMessageCode code, String message) {
		super(code, message);
	}

	public DataProfilerServiceException(DataProfilerServiceMessageCode code, String message, Throwable cause) {
		super(code, message, cause);
	}

}
