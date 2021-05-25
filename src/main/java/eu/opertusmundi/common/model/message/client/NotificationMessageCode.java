package eu.opertusmundi.common.model.message.client;

import eu.opertusmundi.common.model.MessageCode;

public enum NotificationMessageCode implements MessageCode {
    TYPE_NOT_SUPPORTED,
    TEMPLATE_NOT_FOUND,
    VARIABLE_MISSING,
    ;

    @Override
    public String key() {
        return this.getClass().getSimpleName() + '.' + this.name();
    }

}
