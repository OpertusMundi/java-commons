package eu.opertusmundi.common.service;

import eu.opertusmundi.common.model.ServiceException;
import eu.opertusmundi.common.model.asset.AssetMessageCode;

public class AssetDraftException extends ServiceException {

    private static final long serialVersionUID = 1L;

    public AssetDraftException(AssetMessageCode code) {
        super(code, "An unhandled exception has occurred");
    }

    public AssetDraftException(AssetMessageCode code, String message) {
        super(code, message);
    }

    public AssetDraftException(AssetMessageCode code, String message, Throwable cause) {
        super(code, message, cause);
    }

}