package eu.opertusmundi.common.model.transform;

import eu.opertusmundi.common.model.MessageCode;

public enum TransformServiceMessageCode implements MessageCode {
    UNKNOWN,
    SOURCE_NOT_FOUND,
    TARGET_EXISTS,
    RESOURCE_NOT_FOUND,
    SERVICE_ERROR,
    ;

    @Override
    public String key() {
        return this.getClass().getSimpleName() + '.' + this.name();
    }

}
