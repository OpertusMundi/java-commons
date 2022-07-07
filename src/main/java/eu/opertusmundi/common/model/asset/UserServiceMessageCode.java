package eu.opertusmundi.common.model.asset;

import eu.opertusmundi.common.model.MessageCode;

public enum UserServiceMessageCode implements MessageCode {
    BPM_SERVICE,
    ERROR,
    INVALID_STATE,
    IO_ERROR,
    LOCK_EXISTS,
    METADATA_SERIALIZATION,
    RESOURCE_COPY,
    RESOURCE_NOT_FOUND,
    SERVICE_NOT_FOUND,
    USER_NOT_FOUND,
    VENDOR_ACCOUNT_NOT_FOUND,
    ;

    @Override
    public String key() {
        return this.getClass().getSimpleName() + '.' + this.name();
    }

}
