package eu.opertusmundi.common.model.profiler;

import eu.opertusmundi.common.model.MessageCode;

public enum DataProfilerServiceMessageCode implements MessageCode {
    UNKNOWN,
    FORMAT_NOT_SUPPORTED,
    SOURCE_NOT_FOUND,
    SOURCE_NOT_SUPPORTED,
    VARIABLE_NOT_FOUND,
    INVALID_VARIABLE_VALUE,
    SERVICE_ERROR,
    ;

    @Override
    public String key() {
        return this.getClass().getSimpleName() + '.' + this.name();
    }

}
