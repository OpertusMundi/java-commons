package eu.opertusmundi.common.model.workflow;

import lombok.Getter;

public enum EnumWorkflow {

    ACCOUNT_REGISTRATION                 ("account-registration"                    , EnumProcessInstanceResource.ACCOUNT),
    CATALOGUE_HARVEST                    ("catalogue-harvest"),
    CONSUMER_COPY_RESOURCE_TO_DRIVE      ("consumer-copy-resource-to-drive"),
    CONSUMER_PURCHASE_ASSET_WITH_PAYIN   ("consumer-purchase-asset-with-payin"      , EnumProcessInstanceResource.ORDER),
    CONSUMER_PURCHASE_ASSET_WITHOUT_PAYIN("consumer-purchase-asset-without-payin"   , EnumProcessInstanceResource.ORDER),
    CONSUMER_REGISTRATION                ("consumer-registration"                   , EnumProcessInstanceResource.CONSUMER),
    PROVIDER_PAYOUT                      ("provider-payout"                         , EnumProcessInstanceResource.PAYOUT),
    PROVIDER_PUBLISH_ASSET               ("provider-publish-asset"                  , EnumProcessInstanceResource.DRAFT),
    PROVIDER_REGISTRATION                ("provider-registration"                   , EnumProcessInstanceResource.PROVIDER),
    PROVIDER_REMOVE_ASSET                ("provider-remove-asset"                   , EnumProcessInstanceResource.ASSET),
    PROVIDER_UPDATE_KYC_LEVEL            ("provider-update-kyc-level"),
    PROVIDER_UPDATE_DEFAULT_CONTRACTS    ("provider-update-default-contracts"),
    PUBLISH_USER_SERVICE                 ("user-publish-service"                    , EnumProcessInstanceResource.USER_SERVICE),
    REMOVE_USER_SERVICE                  ("user-remove-service"                     , EnumProcessInstanceResource.USER_SERVICE),
    SUBSCRIPTION_BILLING                 ("subscription-billing"),
    SUBSCRIPTION_BILLING_CONSUMER_PAYIN  ("subscription-billing-consumer-payin"     , EnumProcessInstanceResource.PAYIN),
    VENDOR_ACCOUNT_REGISTRATION          ("vendor-account-registration"             , EnumProcessInstanceResource.ACCOUNT),

    // System Maintenance workflows
    SYSTEM_MAINTENANCE                   ("system-maintenance"),
    SYSTEM_MAINTENANCE_DELETE_USER       ("system-maintenance-remove-all-user-data" , EnumProcessInstanceResource.ACCOUNT),
    ;

    @Getter
    private final String key;

    @Getter
    private final EnumProcessInstanceResource resourceType;

    EnumWorkflow(String key) {
        this(key, null);
    }

    EnumWorkflow(String key, EnumProcessInstanceResource resourceType) {
        this.key          = key;
        this.resourceType = resourceType;
    }

    public static EnumWorkflow fromKey(String key) {
        for (final EnumWorkflow e : EnumWorkflow.values()) {
            if (e.key.equalsIgnoreCase(key)) {
                return e;
            }
        }
        return null;
    }

}
