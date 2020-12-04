package eu.opertusmundi.common.model.profiler;

import lombok.Getter;

public class DataProfilerServiceException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    @Getter
    private final DataProfilerServiceMessageCode code;

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
