package eu.opertusmundi.common.model.ingest;

import eu.opertusmundi.common.model.ServiceException;
import lombok.Builder;
import lombok.Getter;

public class IngestServiceException extends ServiceException {

    private static final long serialVersionUID = 1L;

    @Getter
    private String errorDetails;

    @Getter
    private int retries;

    @Getter
    private long retryTimeout;

    @Builder
    private IngestServiceException(IngestServiceMessageCode code, String message, String errorDetails, int retries, long retryTimeout) {
        super(code, message);

        this.errorDetails = errorDetails;
        this.retries      = retries;
        this.retryTimeout = retryTimeout;
    }

    public IngestServiceException(IngestServiceMessageCode code) {
        super(code, "Operation failed");
    }

    public IngestServiceException(IngestServiceMessageCode code, String message) {
        super(code, message);
    }

    public IngestServiceException(IngestServiceMessageCode code, String message, Throwable cause) {
        super(code, message, cause);
    }

}
