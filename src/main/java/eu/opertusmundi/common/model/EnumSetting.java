package eu.opertusmundi.common.model;

import lombok.Getter;

/**
 * Enumeration of application settings
 */
public enum EnumSetting {
    MARKETPLACE_BANNER_TEXT         (EnumService.API_GATEWAY,   "announcement.text",                EnumSettingType.TEXT),
    MARKETPLACE_BANNER_ENABLED      (EnumService.API_GATEWAY,   "announcement.enabled",             EnumSettingType.BOOLEAN),
    TOPIO_ACCOUNT_ID                (EnumService.BPM_WORKER,    "topio-account-id",                 EnumSettingType.NUMERIC),
    TOPIO_FEE_PERCENT               (EnumService.BPM_WORKER,    "topio-fee-percent",                EnumSettingType.NUMERIC),
    USER_SERVICE_PRICE_PER_CALL     (EnumService.ADMIN_GATEWAY, "user-service.pricing-model.price", EnumSettingType.JSON),
    ;

    @Getter
    private EnumService service;

    @Getter
    private String key;

    @Getter
    private EnumSettingType type;

    EnumSetting(EnumService service, String key, EnumSettingType type) {
        this.key     = key;
        this.service = service;
        this.type    = type;
    }

}
