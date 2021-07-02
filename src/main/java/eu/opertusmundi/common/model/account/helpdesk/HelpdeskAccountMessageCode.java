package eu.opertusmundi.common.model.account.helpdesk;

import eu.opertusmundi.common.model.MessageCode;

public enum HelpdeskAccountMessageCode implements MessageCode
{
    UNKNOWN,
    ACCOUNT_NOT_SET,
    ACCOUNT_NOT_FOUND,
    ;

    @Override
    public String key()
    {
        return this.getClass().getSimpleName() + '.' + this.name();
    }

}
