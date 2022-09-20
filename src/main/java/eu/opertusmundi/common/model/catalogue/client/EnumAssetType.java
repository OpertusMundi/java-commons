package eu.opertusmundi.common.model.catalogue.client;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import eu.opertusmundi.common.model.EnumRole;
import eu.opertusmundi.common.model.order.EnumOrderItemType;
import eu.opertusmundi.common.model.pricing.EnumPricingModel;
import lombok.Getter;

public enum EnumAssetType {
    // Topio supported asset types

    /**
     * A collection of existing assets
     */
    BUNDLE                          (false, "bundle",   EnumOrderItemType.ASSET,        "A collection of existing assets"),
    /**
     * A set of data formats for array-oriented scientific data
     */
    NETCDF                          (true,  "netcdf",   EnumOrderItemType.ASSET,        "A set of data formats for array-oriented scientific data"),
    /**
     * Raster dataset
     */
    RASTER                          (true,  "raster",   EnumOrderItemType.ASSET,        "Raster dataset"),
    /**
     * OGC/Topio service
     */
    SERVICE (
        false,
        false,
        "service",
        EnumOrderItemType.SUBSCRIPTION,
        "OGC/Topio service",
        false,
        null,
        Arrays.asList(EnumPricingModel.FREE, EnumPricingModel.PER_CALL, EnumPricingModel.PER_ROW),
        Arrays.asList(EnumDeliveryMethod.DIGITAL_PLATFORM),
        true,
        false
    ),
    /**
     * Tabular dataset
     */
    TABULAR                         (true,  "tabular",  EnumOrderItemType.ASSET,        "Tabular data"),
    /**
     * Vector dataset
     */
    VECTOR                          (true,  "vector",   EnumOrderItemType.ASSET,        "Vector dataset"),

    // Extended asset types for Sentinel Hub integration

    /**
     * Sentinel Hub open data collections
     */
    SENTINEL_HUB_OPEN_DATA(
        false,
        true,
        "sentinel-hub-open-data",
        EnumOrderItemType.SUBSCRIPTION,
        "Sentinel Hub open data collections",
        false,
        EnumRole.ROLE_SENTINEL_HUB,
        Arrays.asList(EnumPricingModel.SENTINEL_HUB_SUBSCRIPTION),
        Arrays.asList(EnumDeliveryMethod.DIGITAL_PROVIDER),
        true,
        false
    ),
    /**
     * Sentinel Hub commercial data
     */
    SENTINEL_HUB_COMMERCIAL_DATA(
        false,
        true,
        "sentinel-hub-commercial-data",
        EnumOrderItemType.ASSET,
        "Sentinel Hub commercial data",
        false,
        EnumRole.ROLE_SENTINEL_HUB,
        Arrays.asList(EnumPricingModel.SENTINEL_HUB_IMAGES),
        Arrays.asList(EnumDeliveryMethod.DIGITAL_PROVIDER),
        false,
        false
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
     * The order item type of the asset type
     */
    @Getter
    private final EnumOrderItemType orderItemType;

    /**
     * `True` if an asset/subscription should be registered to the user account on purchase
     */
    @Getter
    private final boolean registeredOnPurchase;

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
     * True if the owner of this asset is allowed to download its resources
     */
    @Getter
    private final boolean resourceDownloadAllowed;

    /**
     * True if the pricing models are injected by an external data provider
     */
    @Getter
    private final boolean dynamicPricingModels;

    EnumAssetType(
        boolean primary,
        String value,
        EnumOrderItemType orderItemType,
        String description
    ) {
        this(primary, false, value, orderItemType, description, true, null, Collections.emptyList(), Collections.emptyList(), true, true);
    }

    EnumAssetType(
        boolean primary,
        boolean dynamicPricingModels,
        String value,
        EnumOrderItemType orderItemType,
        String description,
        boolean resourceRequired,
        EnumRole requiredRole,
        List<EnumPricingModel> allowedPricingModels,
        List<EnumDeliveryMethod> allowedDeliveryMethods,
        boolean registeredOnPurchase,
        boolean resourceDownloadAllowed
    ) {
        this.allowedPricingModels    = allowedPricingModels;
        this.allowedDeliveryMethods  = allowedDeliveryMethods;
        this.description             = description;
        this.dynamicPricingModels    = dynamicPricingModels;
        this.orderItemType           = orderItemType;
        this.primary                 = primary;
        this.registeredOnPurchase    = registeredOnPurchase;
        this.requiredRole            = requiredRole;
        this.resourceRequired        = resourceRequired;
        this.value                   = value;
        this.resourceDownloadAllowed = resourceDownloadAllowed;
    }

    public static EnumAssetType fromString(String value) {
        return Arrays.stream(EnumAssetType.values())
            .filter(r -> r.value.equalsIgnoreCase(value))
            .findFirst()
            .orElse(null);
    }

}
