package eu.opertusmundi.common.model.contract;

import eu.opertusmundi.common.model.MessageCode;

public enum ContractMessageCode implements MessageCode {
    ACCOUNT_NOT_FOUND,
    DRAFT_NOT_FOUND,
    HISTORY_NOT_FOUND,
    CONTRACT_NOT_FOUND,
    MASTER_CONTRACT_NOT_FOUND,
    INVALID_STATUS,
    ;

    @Override
    public String key() {
        return this.getClass().getSimpleName() + '.' + this.name();
    }

}
