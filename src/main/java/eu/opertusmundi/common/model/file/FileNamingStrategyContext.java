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
     * @param level the level of the path component
     * @param name a path component
     * @return
     */
    public boolean validateName(int level, String name)
    {
        // This is a stub; override in subclasses to provide specific filtering
        return true;
    }
}
