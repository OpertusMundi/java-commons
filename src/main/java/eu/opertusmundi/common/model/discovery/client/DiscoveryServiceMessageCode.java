package eu.opertusmundi.common.model.discovery.client;

import eu.opertusmundi.common.model.MessageCode;

public enum DiscoveryServiceMessageCode implements MessageCode {
    ERROR,
    DISCOVERY_SERVICE,
    ;

    @Override
    public String key() {
        return this.getClass().getSimpleName() + '.' + this.name();
    }

}
