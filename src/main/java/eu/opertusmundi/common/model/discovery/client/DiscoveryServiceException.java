package eu.opertusmundi.common.model.discovery.client;

import eu.opertusmundi.common.model.ServiceException;

public class DiscoveryServiceException extends ServiceException {

    private static final long serialVersionUID = 1L;

    private DiscoveryServiceException(Throwable cause) {
        super(DiscoveryServiceMessageCode.ERROR, "[Discovery Service] Operation has failed", cause);
    }

    public DiscoveryServiceException(DiscoveryServiceMessageCode code, String message, Throwable cause) {
        super(code, message, cause);
    }

    public static DiscoveryServiceException wrap(Throwable cause) {
        return new DiscoveryServiceException(cause);
    }

}
