package eu.opertusmundi.common.model.asset;

import eu.opertusmundi.common.model.MessageCode;

public enum AssetMessageCode implements MessageCode {
    ERROR,
    IO_ERROR,
    SERIALIZATION_ERROR,
    INVALID_STATE,
    VALIDATION,
    DRAFT_NOT_FOUND,
    PROVIDER_NOT_FOUND,
    VENDOR_ACCOUNT_NOT_FOUND,
    FORMAT_NOT_SUPPORTED,
    METADATA_SERIALIZATION,
    BPM_SERVICE,
    CATALOGUE_SERVICE,
    RESOURCE_NOT_FOUND,
    ADDITIONAL_RESOURCE_NOT_FOUND,
    API_COMMAND_NOT_SUPPORTED,
    API_COMMAND_ASSET_NOT_FOUND,
    API_COMMAND_ASSET_TYPE_NOT_SUPPORTED,
    API_COMMAND_ASSET_ACCESS_DENIED,
    API_COMMAND_RESOURCE_COPY,
    HARVEST_ITEM_NOT_FOUND,
    ASSET_NOT_FOUND,
    DRAFT_FOR_PARENT_EXISTS,
    ;

    @Override
    public String key() {
        return this.getClass().getSimpleName() + '.' + this.name();
    }

}
