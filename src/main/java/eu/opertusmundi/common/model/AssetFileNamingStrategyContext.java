package eu.opertusmundi.common.model;

import java.util.UUID;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AssetFileNamingStrategyContext extends FileNamingStrategyContext {

    protected AssetFileNamingStrategyContext(UUID key, boolean createIfNotExists) {
        super(createIfNotExists);

        this.key = key;
    }

    private UUID key;

    public static AssetFileNamingStrategyContext of(UUID key) {
        return new AssetFileNamingStrategyContext(key, true);
    }

}
