package eu.opertusmundi.common.model.asset;

import eu.opertusmundi.common.model.MessageCode;

public enum AssetMessageCode implements MessageCode {
    ADDITIONAL_RESOURCE_NOT_FOUND,
    API_COMMAND_ASSET_ACCESS_DENIED,
    API_COMMAND_ASSET_NOT_FOUND,
    API_COMMAND_ASSET_TYPE_NOT_SUPPORTED,
    API_COMMAND_NOT_SUPPORTED,
    API_COMMAND_RESOURCE_COPY,
    ASSET_NOT_FOUND,
    BPM_SERVICE,
    CATALOGUE_SERVICE,
    CONTRACT_ANNEX_NOT_FOUND,
    DRAFT_FOR_PARENT_EXISTS,
    DRAFT_NOT_FOUND,
    ERROR,
    FORMAT_NOT_SUPPORTED,
    HARVEST_ITEM_NOT_FOUND,
    INVALID_STATE,
    IO_ERROR,
    LOCK_EXISTS,
    METADATA_SERIALIZATION,
    OPERATION_NOT_SUPPORTED,
    PROVIDER_NOT_FOUND,
    RESOURCE_NOT_FOUND,
    RESOURCE_TYPE_NOT_SUPPORTED,
    SERIALIZATION_ERROR,
    VALIDATION,
    VENDOR_ACCOUNT_NOT_FOUND,
    ;

    @Override
    public String key() {
        return this.getClass().getSimpleName() + '.' + this.name();
    }

}
