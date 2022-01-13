package eu.opertusmundi.common.model.catalogue.client;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import eu.opertusmundi.common.model.EnumRole;
import eu.opertusmundi.common.model.pricing.EnumPricingModel;
import lombok.Getter;

public enum EnumAssetType {
    // Topio supported asset types

    /**
     * A collection of existing assets
     */
    BUNDLE                          (false, "bundle",   "A collection of existing assets"),
    /**
     * A set of data formats for array-oriented scientific data
     */
    NETCDF                          (true,  "netcdf",   "A set of data formats for array-oriented scientific data"),
    /**
     * Raster dataset
     */
    RASTER                          (true,  "raster",   "Raster dataset"),
    /**
     * OGC/Topio service
     */
    SERVICE                         (false, "service",  "OGC/Topio service"),
    /**
     * Tabular dataset
     */
    TABULAR                         (true,  "tabular",  "Tabular data"),
    /**
     * Vector dataset
     */
    VECTOR                          (true,  "vector",   "Vector dataset"),

    // Extended asset types for Sentinel Hub integration

    /**
     * Sentinel Hub open data collections
     */
    SENTINEL_HUB_OPEN_DATA          (false, true, "sentinel-hub-open-data",       "Sentinel Hub open data collections",   false,
        EnumRole.ROLE_SENTINEL_HUB,
        Arrays.asList(EnumPricingModel.SUBSCRIPTION),
        Arrays.asList(EnumDeliveryMethod.DIGITAL_PROVIDER)
    ),
    /**
     * Sentinel Hub commercial data
     */
    SENTINEL_HUB_COMMERCIAL_DATA    (false, true, "sentinel-hub-commercial-data", "Sentinel Hub commercial data",         false,
        EnumRole.ROLE_SENTINEL_HUB,
        Arrays.asList(EnumPricingModel.CUSTOM),
        Arrays.asList(EnumDeliveryMethod.DIGITAL_PROVIDER)
    ),
    ;

    /**
     * Catalogue service enumeration value
     */
    @Getter
    private final String value;

    /**
     * Asset type description required for registering the type to the PID
     * service
     */
    @Getter
    private final String description;

    /**
     * True if this is a primary data source e.g. Vector data
     */
    @Getter
    private final boolean primary;

    /**
     * True if at least one resource must be specified
     */
    @Getter
    private final boolean resourceRequired;

    /**
     * Optional required role for creating an asset of this type
     */
    @Getter
    private final EnumRole requiredRole;

    /**
     * List of allowed pricing models
     */
    @Getter
    private final List<EnumPricingModel> allowedPricingModels;

    /**
     * List of allowed deliver methods
     */
    @Getter
    private final List<EnumDeliveryMethod> allowedDeliveryMethods;

    /**
     * True if the pricing models are injected by an external data provider
     */
    @Getter
    private final boolean dynamicPricingModels;

    private EnumAssetType(boolean primary, String value, String description) {
        this(primary, false, value, description, true, null, Collections.emptyList(), Collections.emptyList());
    }

    private EnumAssetType(
        boolean primary, boolean dynamicPricingModels, String value, String description, boolean resourceRequired,
        EnumRole requiredRole, List<EnumPricingModel> allowedPricingModels, List<EnumDeliveryMethod> allowedDeliveryMethods
    ) {
        this.allowedPricingModels   = allowedPricingModels;
        this.allowedDeliveryMethods = allowedDeliveryMethods;
        this.description            = description;
        this.dynamicPricingModels   = dynamicPricingModels;
        this.primary                = primary;
        this.requiredRole           = requiredRole;
        this.resourceRequired       = resourceRequired;
        this.value                  = value;
    }

    public static EnumAssetType fromString(String value) {
        return Arrays.stream(EnumAssetType.values())
            .filter(r -> r.value.equalsIgnoreCase(value))
            .findFirst()
            .orElse(null);
    }

}
