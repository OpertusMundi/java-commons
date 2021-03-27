package eu.opertusmundi.common.model.kyc;

import eu.opertusmundi.common.model.MessageCode;

public enum CustomerVerificationMessageCode implements MessageCode {
    UNKNOWN,
    API_ERROR,
    ACCESS_DENIED,
    ACCOUNT_NOT_FOUND,
    PLATFORM_CUSTOMER_NOT_FOUND,
    PROVIDER_USER_NOT_FOUND,
    CUSTOMER_TYPE_NOT_SUPPORTED,
    UBO_NOT_FOUND,
    INVALID_STATUS,
    DRAFT_UBO_EXISTS,
    SUBMITTED_UBO_EXISTS,
    PAGE_FILE_MISSING,
    ;

    @Override
    public String key() {
        return this.getClass().getSimpleName() + '.' + this.name();
    }

}
