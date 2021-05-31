package eu.opertusmundi.common.model.account;

import eu.opertusmundi.common.model.MessageCode;

public enum ConsumerServiceMessageCode  implements MessageCode {
    CATALOGUE_ITEM_NOT_FOUND,
    ;

    @Override
    public String key() {
        return this.getClass().getSimpleName() + '.' + this.name();
    }

}