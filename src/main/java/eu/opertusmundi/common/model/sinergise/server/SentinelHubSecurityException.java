package eu.opertusmundi.common.model.sinergise.server;

public class SentinelHubSecurityException extends SentinelHubException {

    private static final long serialVersionUID = 1L;

    public SentinelHubSecurityException(String message) {
        super(SentinelHubExceptionMessageCode.SERVER, message);
    }

}
