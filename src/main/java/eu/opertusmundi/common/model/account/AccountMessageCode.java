package eu.opertusmundi.common.model.account;

import eu.opertusmundi.common.model.MessageCode;

public enum AccountMessageCode implements MessageCode
{
    ACCOUNT_NOT_FOUND,
    ACCOUNT_CLIENT_ERROR,
    ACCOUNT_CLIENT_NOT_UNIQUE_ALIAS,
    ACCOUNT_CLIENT_NOT_FOUND,
    INVALID_ACCOUNT_STATUS,
    IO_ERROR,
    IDP_OPERATION_ERROR,
    MAIL_OPERATION_ERROR,
    FEIGN_CLIENT_ERROR,
    ;

    @Override
    public String key()
    {
        return this.getClass().getSimpleName() + '.' + this.name();
    }

}
