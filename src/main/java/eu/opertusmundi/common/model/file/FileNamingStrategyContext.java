package eu.opertusmundi.common.model.file;

import lombok.Getter;

@Getter
public abstract class FileNamingStrategyContext {

    protected FileNamingStrategyContext(boolean createIfNotExists) {
        this.createIfNotExists = createIfNotExists;
    }

    private final boolean createIfNotExists;
    
    /**
     * Validate a name component of a path.
     * 
     * @param name a path component
     * @return
     */
    public boolean validateName(String name)
    {
        // This is a stub; override in subclasses to provide specific filtering
        return true;
    }
}
