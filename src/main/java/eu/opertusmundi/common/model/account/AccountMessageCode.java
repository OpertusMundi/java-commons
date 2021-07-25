package eu.opertusmundi.common.model.account;

import eu.opertusmundi.common.model.MessageCode;

public enum AccountMessageCode implements MessageCode
{
    ACCOUNT_NOT_FOUND,
    INVALID_ACCOUNT_STATUS,
    ;

    @Override
    public String key()
    {
        return this.getClass().getSimpleName() + '.' + this.name();
    }

}
