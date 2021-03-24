package eu.opertusmundi.common.model.order;

import eu.opertusmundi.common.model.MessageCode;

public enum CartMessageCode implements MessageCode {
    ERROR,
    PRICING_MODEL,
    CATALOGUE,
    QUOTATION,
    ;

    @Override
    public String key() {
        return this.getClass().getSimpleName() + '.' + this.name();
    }

}
