package eu.opertusmundi.common.model.asset;

import eu.opertusmundi.common.model.ServiceException;

public class AssetRepositoryException extends ServiceException {

    private static final long serialVersionUID = 1L;

    public AssetRepositoryException(AssetMessageCode code) {
        super(code, "An I/O error has occurred");
    }

    public AssetRepositoryException(String message) {
        super(AssetMessageCode.IO_ERROR, message);
    }

    public AssetRepositoryException(AssetMessageCode code, String message) {
        super(code, message);
    }

    public AssetRepositoryException(AssetMessageCode code, String message, Throwable cause) {
        super(code, message, cause);
    }

}