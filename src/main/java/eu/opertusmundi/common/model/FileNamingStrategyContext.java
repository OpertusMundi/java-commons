package eu.opertusmundi.common.model;

import lombok.Getter;

@Getter
public abstract class FileNamingStrategyContext {

    protected FileNamingStrategyContext(boolean createIfNotExists) {
        this.createIfNotExists = createIfNotExists;
    }

    private final boolean createIfNotExists;

}
