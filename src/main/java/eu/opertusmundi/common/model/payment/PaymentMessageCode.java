package eu.opertusmundi.common.model.payment;

import eu.opertusmundi.common.model.MessageCode;

public enum PaymentMessageCode implements MessageCode {
    ERROR,
    ;

    @Override
    public String key() {
        return this.getClass().getSimpleName() + '.' + this.name();
    }

}
