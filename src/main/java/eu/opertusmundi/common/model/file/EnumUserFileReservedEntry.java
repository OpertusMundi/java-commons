package eu.opertusmundi.common.model.file;

/**
 * Enumerate reserved names (directories or files) for the top-level of a user directory  
 */
public enum EnumUserFileReservedEntry {
    
    /**
     * A subfolder used as the home of a Jupyter notebook
     */
    NOTEBOOKS_FOLDER(".notebooks"),
    
    /**
     * A subfolder with quota reporting/management information
     */
    QUOTA_FOLDER(".quota"),
    
    ;
    
    /**
     * The name of the path component
     */
    private final String entryName;
    
    private EnumUserFileReservedEntry(String entryName)
    {
        this.entryName = entryName;
    }
    
    public String entryName() {
        return entryName;
    }
}
