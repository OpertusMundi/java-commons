package eu.opertusmundi.common.model.asset;

public enum EnumProviderAssetDraftStatus {
    /**
     * Asset is editable
     */
    DRAFT,
    /**
     * Asset is submitted and automated metadata is computed
     */
    SUBMITTED,
    /**
     * Automated metadata has been computed and optionally file assets have been
     * imported to PostGIS for service assets. HelpDesk review is required
     */
    PENDING_HELPDESK_REVIEW,
    /**
     * HelpDesk user rejects the asset
     */
    HELPDESK_REJECTED,
    /**
     * HelpDesk user accepts the asset. Provider review is required. Provider
     * may adjust the visibility of the metadata
     */
    PENDING_PROVIDER_REVIEW,
    /**
     * Provider rejects the asset
     */
    PROVIDER_REJECTED,
    /**
     * Provider accepted the asset and final post-processing actions are executed
     */
    POST_PROCESSING,
    /**
     * Asset is being published to the catalogue
     */
    PUBLISHING,
    /**
     * Draft is published
     */
    PUBLISHED,
    /**
     * Cancelled by the user
     */
    CANCELLED,
    ;
}
