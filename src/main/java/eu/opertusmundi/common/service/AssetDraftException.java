package eu.opertusmundi.common.service;

import eu.opertusmundi.common.model.asset.AssetMessageCode;
import lombok.Getter;

public class AssetDraftException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    @Getter
    private final AssetMessageCode code;

    public AssetDraftException(AssetMessageCode code) {
        super("An unhandled exception has occurred");

        this.code = code;
    }

    public AssetDraftException(AssetMessageCode code, String message) {
        super(message);

        this.code = code;
    }

    public AssetDraftException(AssetMessageCode code, String message, Throwable cause) {
        super(message, cause);

        this.code = code;
    }

}