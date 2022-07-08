package eu.opertusmundi.common.model.asset;

public enum EnumResourceSource {
    /**
     * Source not available
     */
    NONE,
    /**
     * Resource was copied from the parent data source
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
    ;
}
