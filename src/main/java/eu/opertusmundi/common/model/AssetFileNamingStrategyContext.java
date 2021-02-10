package eu.opertusmundi.common.model;

import java.util.UUID;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AssetFileNamingStrategyContext extends FileNamingStrategyContext {

    protected AssetFileNamingStrategyContext(UUID publisherKey, UUID draftKey, boolean createIfNotExists) {
        super(createIfNotExists);

        this.publisherKey = publisherKey;
        this.draftKey     = draftKey;
    }

    private UUID publisherKey;

    private UUID draftKey;

    public static AssetFileNamingStrategyContext of(UUID publisherKey, UUID draftKey) {
        return new AssetFileNamingStrategyContext(publisherKey, draftKey, true);
    }

}
