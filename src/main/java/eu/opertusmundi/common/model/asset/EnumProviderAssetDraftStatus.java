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
     * Automated metadata is completed and HelpDesk review is required
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
     * Draft is published
     */
    PUBLISHED
    ;
}
