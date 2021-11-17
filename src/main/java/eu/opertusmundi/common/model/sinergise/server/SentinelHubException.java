package eu.opertusmundi.common.model.sinergise.server;

import eu.opertusmundi.common.model.ServiceException;

public class SentinelHubException extends ServiceException {

    private static final long serialVersionUID = 1L;

    public SentinelHubException() {
        super(SentinelHubExceptionMessageCode.UNKNOWN, "Sentinel Hub operation has failed");
    }

    public SentinelHubException(SentinelHubExceptionMessageCode code, Throwable cause) {
        super(code, "Sentinel Hub operation has failed", cause);
    }

    public SentinelHubException(String message) {
        super(SentinelHubExceptionMessageCode.SERVER, message);
    }

    public SentinelHubException(SentinelHubExceptionMessageCode code, String message) {
        super(code, message);
    }

    public SentinelHubException(SentinelHubExceptionMessageCode code, String message, Throwable cause) {
        super(code, message, cause);
    }

    public static SentinelHubException wrap(Throwable cause) {
        return new SentinelHubException(SentinelHubExceptionMessageCode.UNKNOWN, cause);
    }

}