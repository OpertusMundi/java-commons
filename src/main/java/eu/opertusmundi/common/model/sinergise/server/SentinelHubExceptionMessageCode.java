package eu.opertusmundi.common.model.sinergise.server;

import eu.opertusmundi.common.model.MessageCode;

public enum SentinelHubExceptionMessageCode implements MessageCode {
    UNKNOWN,
    CLIENT,
    SERVER,
    ;

    @Override
    public String key() {
        return this.getClass().getSimpleName() + '.' + this.name();
    }

}
