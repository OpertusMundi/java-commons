package eu.opertusmundi.common.model.asset;

import eu.opertusmundi.common.model.MessageCode;

public enum AssetMessageCode implements MessageCode {
    ERROR,
    IO_ERROR,
    INVALID_STATE,
    DRAFT_NOT_FOUND,
    PROVIDER_NOT_FOUND,
    FORMAT_NOT_SUPPORTED,
    BPM_SERVICE,
    CATALOGUE_SERVICE,
    ;

    @Override
    public String key() {
        return this.getClass().getSimpleName() + '.' + this.name();
    }

}
