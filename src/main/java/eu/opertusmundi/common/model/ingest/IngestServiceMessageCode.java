package eu.opertusmundi.common.model.ingest;

import eu.opertusmundi.common.model.MessageCode;

public enum IngestServiceMessageCode implements MessageCode {
    UNKNOWN,
    SOURCE_NOT_FOUND,
    SERVICE_ERROR,
    ;

    @Override
    public String key() {
        return this.getClass().getSimpleName() + '.' + this.name();
    }

}
