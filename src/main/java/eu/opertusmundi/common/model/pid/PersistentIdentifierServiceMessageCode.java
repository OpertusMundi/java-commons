package eu.opertusmundi.common.model.pid;

import eu.opertusmundi.common.model.MessageCode;

public enum PersistentIdentifierServiceMessageCode implements MessageCode {
    UNKNOWN,
    USER_REGISTRATION,
    ASSET_TYPE_QUERY,
    ASSET_TYPE_REGISTRATION,
    ASSET_QUERY,
    ASSET_REGISTRATION,
    ;

    @Override
    public String key() {
        return this.getClass().getSimpleName() + '.' + this.name();
    }

}
