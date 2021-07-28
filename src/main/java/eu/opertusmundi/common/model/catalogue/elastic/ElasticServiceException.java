package eu.opertusmundi.common.model.catalogue.elastic;

import eu.opertusmundi.common.model.ServiceException;
import eu.opertusmundi.common.model.catalogue.CatalogueServiceMessageCode;

public class ElasticServiceException extends ServiceException {

    private static final long serialVersionUID = 1L;

    public ElasticServiceException(String message) {
        super(CatalogueServiceMessageCode.ELASTIC_SERVICE, message);
    }

    public ElasticServiceException(String message, Throwable cause) {
        super(CatalogueServiceMessageCode.ELASTIC_SERVICE, message, cause);
    }

}