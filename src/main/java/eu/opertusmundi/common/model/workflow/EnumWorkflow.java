package eu.opertusmundi.common.model.workflow;

import lombok.Getter;

public enum EnumWorkflow {

    ACCOUNT_REGISTRATION                 ("account-registration"),
    CATALOGUE_HARVEST                    ("catalogue-harvest"),
    CONSUMER_COPY_RESOURCE_TO_DRIVE      ("consumer-copy-resource-to-drive"),
    CONSUMER_PURCHASE_ASSET_WITH_PAYIN   ("consumer-purchase-asset-with-payin"),
    CONSUMER_PURCHASE_ASSET_WITHOUT_PAYIN("consumer-purchase-asset-without-payin"),
    CONSUMER_REGISTRATION                ("consumer-registration"),
    PROVIDER_PAYOUT                      ("provider-payout"),
    PROVIDER_PUBLISH_ASSET               ("provider-publish-asset"),
    PROVIDER_REGISTRATION                ("provider-registration"),
    PROVIDER_REMOVE_ASSET                ("provider-remove-asset"),
    PUBLISH_USER_SERVICE                 ("user-publish-service"),
    REMOVE_USER_SERVICE                  ("user-remove-service"),
    SUBSCRIPTION_BILLING                 ("workflow-subscription-billing"),
    VENDOR_ACCOUNT_REGISTRATION          ("vendor-account-registration"),

    // System Maintenance workflows
    SYSTEM_MAINTENANCE                   ("system-maintenance"),
    SYSTEM_MAINTENANCE_DELETE_USER       ("system-maintenance-remove-all-user-data"),
    ;

    @Getter
    private final String key;

    private EnumWorkflow(String key) {
        this.key = key;
    }

}
