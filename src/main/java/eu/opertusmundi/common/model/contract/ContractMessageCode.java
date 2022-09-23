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
    DEFAULT_MASTER_CONTRACT_NOT_FOUND,
    DEFAULT_MASTER_CONTRACT_ALREADY_SET,
    DEFAULT_MASTER_CONTRACT_SECTION_INVALID_CONFIG,
    DEFAULT_PROVIDER_CONTRACT_DEACTIVATE,
    WORKFLOW_INSTANCE_EXISTS,
    ;

    @Override
    public String key() {
        return this.getClass().getSimpleName() + '.' + this.name();
    }
}
