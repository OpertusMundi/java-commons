package eu.opertusmundi.common.model.asset;

public enum EnumResourceSource {
    /**
     * Source not available
     */
    NONE,
    /**
     * File resource copied from the parent data source
     */
    PARENT_DATASOURCE,
    /**
     * Resource is copied from the user's file system
     */
    FILE_SYSTEM,
    /**
     * File was uploaded by the user
     */
    UPLOAD,
    /**
     * File was downloaded from an external link
     */
    EXTERNAL_URL,
    ;
}
