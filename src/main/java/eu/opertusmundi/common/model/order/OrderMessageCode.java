package eu.opertusmundi.common.model.order;

import eu.opertusmundi.common.model.MessageCode;

public enum OrderMessageCode implements MessageCode {
    ERROR,
    BPM_SERVICE,
    ORDER_NOT_FOUND,
    ORDER_INVALID_STATUS,
    ;

    @Override
    public String key() {
        return this.getClass().getSimpleName() + '.' + this.name();
    }

}
