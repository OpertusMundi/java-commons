package eu.opertusmundi.common.model.catalogue;

import eu.opertusmundi.common.model.ServiceException;
import eu.opertusmundi.common.model.catalogue.server.CatalogueMessage;

public class CatalogueServiceException extends ServiceException {

    private static final long serialVersionUID = 1L;

    public CatalogueServiceException() {
        super(CatalogueServiceMessageCode.ERROR, "[Catalogue Service] Operation has failed");
    }

    private CatalogueServiceException(Throwable cause) {
        super(CatalogueServiceMessageCode.ERROR, "[Catalogue Service] Operation has failed", cause);
    }

    public CatalogueServiceException(CatalogueServiceMessageCode code) {
        super(code, "[Catalogue Service] Operation has failed");
    }

    public CatalogueServiceException(CatalogueServiceMessageCode code, String message) {
        super(code, message);
    }

    public CatalogueServiceException(CatalogueServiceMessageCode code, String message, Throwable cause) {
        super(code, message, cause);
    }

    public static CatalogueServiceException wrap(Throwable cause) {
        return new CatalogueServiceException(cause);
    }

    public static CatalogueServiceException fromService(CatalogueMessage message) {
        return new CatalogueServiceException(CatalogueServiceMessageCode.CATALOGUE_SERVICE, message.getDescription());
    }

}
