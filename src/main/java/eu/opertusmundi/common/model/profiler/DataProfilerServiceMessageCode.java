package eu.opertusmundi.common.model.profiler;

import eu.opertusmundi.common.model.MessageCode;

public enum DataProfilerServiceMessageCode implements MessageCode {
    UNKNOWN,
    SOURCE_NOT_FOUND,
    SOURCE_NOT_SUPPORTED,
    SERVICE_ERROR,
    ;

    @Override
    public String key() {
        return this.getClass().getSimpleName() + '.' + this.name();
    }

}
