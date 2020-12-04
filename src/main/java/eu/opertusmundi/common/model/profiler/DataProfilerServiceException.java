package eu.opertusmundi.common.model.profiler;

import lombok.Builder;
import lombok.Getter;

public class DataProfilerServiceException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    @Getter
    private final DataProfilerServiceMessageCode code;

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
        super(message);

        this.code         = code;
        this.errorDetails = errorDetails;
        this.retries      = retries;
        this.retryTimeout = retryTimeout;
    }

    public DataProfilerServiceException(DataProfilerServiceMessageCode code) {
        super("Operation failed");

        this.code = code;
    }

    public DataProfilerServiceException(DataProfilerServiceMessageCode code, String message) {
        super(message);

        this.code = code;
    }

    public DataProfilerServiceException(DataProfilerServiceMessageCode code, String message, Throwable cause) {
        super(message, cause);

        this.code = code;
    }

}
