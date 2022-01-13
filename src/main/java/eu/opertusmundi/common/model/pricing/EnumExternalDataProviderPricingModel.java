package eu.opertusmundi.common.model.pricing;

import java.util.Arrays;

/**
 * Custom pricing models for external data provider integration
 *
 * The values of this enumeration must be a subset of the values in {@link EnumPricingModel}
 */
public enum EnumExternalDataProviderPricingModel {

    // Sentinel Hub

    /**
     * Sentinel Hub open data collections {@link https://www.sentinel-hub.com/}
     */
    SENTINEL_HUB_SUBSCRIPTION,
    /**
     * Sentinel Hub commercial data {@link https://www.sentinel-hub.com/}
     */
    SENTINEL_HUB_IMAGES,
    ;

    public static EnumExternalDataProviderPricingModel fromString(String value) {
        return Arrays.stream(EnumExternalDataProviderPricingModel.values())
            .filter(r -> r.name().equalsIgnoreCase(value))
            .findFirst()
            .orElse(null);
    }

    public EnumPricingModel toPlatformPricingModel() {
        return Arrays.asList(EnumPricingModel.values()).stream()
            .filter(v -> v.name().equals(this.name()))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException(String.format("Cannot find [EnumPricingModel] value for name [%s]", this.name())));
    }

}
