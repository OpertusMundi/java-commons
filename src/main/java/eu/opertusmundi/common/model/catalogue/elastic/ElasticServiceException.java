package eu.opertusmundi.common.model.catalogue.elastic;

public class ElasticServiceException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ElasticServiceException(String message) {
        super(message);
    }

    public ElasticServiceException(String message, Throwable cause) {
        super(message, cause);
    }

}