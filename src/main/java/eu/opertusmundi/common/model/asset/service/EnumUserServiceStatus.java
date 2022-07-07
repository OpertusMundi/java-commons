package eu.opertusmundi.common.model.asset.service;

public enum EnumUserServiceStatus {
    /**
     * Service is being prepared
     */
    PROCESSING,
    /**
     * Publish operation has failed
     */
    FAILED,
    /**
     * Service is published
     */
    PUBLISHED,
    /**
     * Service is deleted
     */
    DELETED,
    ;
}
