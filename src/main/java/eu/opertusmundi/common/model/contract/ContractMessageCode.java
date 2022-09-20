package eu.opertusmundi.common.model.contract;

import eu.opertusmundi.common.model.MessageCode;

public enum ContractMessageCode implements MessageCode {
    ERROR,
    ACCOUNT_NOT_FOUND,
    DRAFT_NOT_FOUND,
    HISTORY_NOT_FOUND,
    CONTRACT_NOT_FOUND,
    MASTER_CONTRACT_NOT_FOUND,
    MASTER_SECTION_NOT_FOUND,
    INVALID_STATUS,
    DEFAULT_CONTRACT_ALREADY_SET,
    DEFAULT_CONTRACT_SECTION_INVALID_CONFIG,
    DEFAULT_CONTRACT_OPTIONAL_SECTION_OPTION_COUNT,
    ;

    @Override
    public String key() {
        return this.getClass().getSimpleName() + '.' + this.name();
    }

}
