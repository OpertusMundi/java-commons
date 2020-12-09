package eu.opertusmundi.common.model.profiler;

import eu.opertusmundi.common.model.ServiceException;
import lombok.Builder;
import lombok.Getter;

public class DataProfilerServiceException extends ServiceException {

    private static final long serialVersionUID = 1L;

    @Getter
    private String errorDetails;

    @Getter
    private int retries;

    @Getter
    private long retryTimeout;

    @Builder
    private DataProfilerServiceException(
        DataProfilerServiceMessageCode code, String message, String errorDetails, int retries, long retryTimeout
    ) {
        super(code, message);

        this.errorDetails = errorDetails;
        this.retries      = retries;
        this.retryTimeout = retryTimeout;
    }

    public DataProfilerServiceException(DataProfilerServiceMessageCode code) {
        super(code, "Operation failed");
    }

    public DataProfilerServiceException(DataProfilerServiceMessageCode code, String message) {
        super(code, message);
    }

    public DataProfilerServiceException(DataProfilerServiceMessageCode code, String message, Throwable cause) {
        super(code, message, cause);
    }

}
