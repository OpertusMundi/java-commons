package eu.opertusmundi.common.model.workflow;

import java.util.Collections;
import java.util.List;

import lombok.Getter;

public enum EnumWorkflow {

    ACCOUNT_REGISTRATION                 ("account-registration"                    , EnumProcessInstanceResource.ACCOUNT),
    CATALOGUE_HARVEST                    ("catalogue-harvest"                       , null, List.of(
        "workflow-catalogue-harvest"
    )),
    CONSUMER_COPY_RESOURCE_TO_DRIVE      ("consumer-copy-resource-to-drive"),
    CONSUMER_PURCHASE_ASSET_WITH_PAYIN   ("consumer-purchase-asset-with-payin"      , EnumProcessInstanceResource.ORDER,  List.of(
        "workflow-process-order-with-payin",
        "workflow-process-payin"
    )),
    CONSUMER_PURCHASE_ASSET_WITHOUT_PAYIN("consumer-purchase-asset-without-payin"   , EnumProcessInstanceResource.ORDER,  List.of(
        "workflow-process-order-without-payin"
    )),
    CONSUMER_REGISTRATION                ("consumer-registration"                   , EnumProcessInstanceResource.CONSUMER),
    CONSUMER_SERVICE_BILLING_PAYOFF      ("consumer-service-billing-payoff"         , EnumProcessInstanceResource.PAYIN,  List.of(
        "subscription-billing-consumer-payin"
    )),
    PROVIDER_PAYOUT                      ("provider-payout"                         , EnumProcessInstanceResource.PAYOUT, List.of(
        "workflow-process-payout"
    )),
    PROVIDER_PUBLISH_ASSET               ("provider-publish-asset"                  , EnumProcessInstanceResource.DRAFT,  List.of(
        "workflow-provider-publish-asset"
    )),
    PROVIDER_REGISTRATION                ("provider-registration"                   , EnumProcessInstanceResource.PROVIDER),
    PROVIDER_REMOVE_ASSET                ("provider-remove-asset"                   , EnumProcessInstanceResource.ASSET,  List.of(
        "workflow-provider-remove-asset"
    )),
    PROVIDER_UPDATE_KYC_LEVEL            ("provider-update-kyc-level"),
    PROVIDER_UPDATE_DEFAULT_CONTRACTS    ("provider-update-default-contracts"       , null, List.of(
        "provider-set-default-contract"
    )),
    PUBLISH_USER_SERVICE                 ("user-publish-service"                    , EnumProcessInstanceResource.USER_SERVICE),
    REMOVE_USER_SERVICE                  ("user-remove-service"                     , EnumProcessInstanceResource.USER_SERVICE),
    SERVICE_BILLING                      ("service-billing"                         , null, List.of(
        "subscription-billing",
        "workflow-subscription-billing"
    )),
    VENDOR_ACCOUNT_REGISTRATION          ("vendor-account-registration"             , EnumProcessInstanceResource.ACCOUNT),

    // System Maintenance workflows
    SYSTEM_MAINTENANCE                   ("system-maintenance"                      , null, List.of(
        "system-database-maintenance",
        "system-maintenance-database",
        "workflow-system-database-maintenance"
    )),
    SYSTEM_MAINTENANCE_DELETE_USER       ("system-maintenance-remove-all-user-data" , EnumProcessInstanceResource.ACCOUNT),
    ;

    @Getter
    private final String key;
    
    @Getter
    private final List<String> keyVersions;

    @Getter
    private final EnumProcessInstanceResource resourceType;

    EnumWorkflow(String key) {
        this(key, null, Collections.emptyList());
    }
    
    EnumWorkflow(String key, EnumProcessInstanceResource resourceType) {
        this(key, resourceType, Collections.emptyList());
    }

    EnumWorkflow(String key, EnumProcessInstanceResource resourceType, List<String> keyVersions) {
        this.key          = key;
        this.resourceType = resourceType;
        this.keyVersions  = keyVersions;
    }

    public static EnumWorkflow fromKey(String key) {
        for (final EnumWorkflow e : EnumWorkflow.values()) {
            if (e.key.equalsIgnoreCase(key)) {
                return e;
            }
            for (final String version : e.keyVersions) {
                if (version.equalsIgnoreCase(key)) {
                    return e;
                }
            }
        }
        return null;
    }

}
