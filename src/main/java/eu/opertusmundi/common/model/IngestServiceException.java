package eu.opertusmundi.common.model;

import eu.opertusmundi.common.model.ingest.IngestServiceMessageCode;
import lombok.Getter;

public class IngestServiceException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    @Getter
    private final IngestServiceMessageCode code;

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
