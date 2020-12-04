package eu.opertusmundi.common.model.ingest;

import lombok.Builder;
import lombok.Getter;

public class IngestServiceException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    @Getter
    private final IngestServiceMessageCode code;

    @Getter
    private String errorDetails;

    @Getter
    private int retries;

    @Getter
    private long retryTimeout;

    @Builder
    private IngestServiceException(IngestServiceMessageCode code, String message, String errorDetails, int retries, long retryTimeout) {
        super(message);

        this.code         = code;
        this.errorDetails = errorDetails;
        this.retries      = retries;
        this.retryTimeout = retryTimeout;
    }

    public IngestServiceException(IngestServiceMessageCode code) {
        super("Operation failed");

        this.code = code;
    }

    public IngestServiceException(IngestServiceMessageCode code, String message) {
        super(message);

        this.code = code;
    }

    public IngestServiceException(IngestServiceMessageCode code, String message, Throwable cause) {
        super(message, cause);

        this.code = code;
    }

}
