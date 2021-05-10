package eu.opertusmundi.common.util;

import eu.opertusmundi.common.model.MessageCode;

public enum GetCapabilitiesServiceMessageCode implements MessageCode {
    UNKNOWN,
    TYPE_NOT_SUPPORTED,
    RESOURCE_NOT_CREATED,
    ;

    @Override
    public String key() {
        return this.getClass().getSimpleName() + '.' + this.name();
    }

}
