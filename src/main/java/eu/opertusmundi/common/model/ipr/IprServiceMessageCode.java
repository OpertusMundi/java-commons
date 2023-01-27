package eu.opertusmundi.common.model.ipr;

import eu.opertusmundi.common.model.MessageCode;

public enum IprServiceMessageCode implements MessageCode {
    UNKNOWN,
    SOURCE_NOT_FOUND,
    VARIABLE_NOT_FOUND,
    SERVICE_ERROR,
    ;

    @Override
    public String key() {
        return this.getClass().getSimpleName() + '.' + this.name();
    }

}
