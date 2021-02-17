package eu.opertusmundi.common.model;

import java.util.UUID;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DraftFileNamingStrategyContext extends FileNamingStrategyContext {

    protected DraftFileNamingStrategyContext(UUID publisherKey, UUID draftKey, boolean createIfNotExists) {
        super(createIfNotExists);

        this.publisherKey = publisherKey;
        this.draftKey     = draftKey;
    }

    private UUID publisherKey;

    private UUID draftKey;

    public static DraftFileNamingStrategyContext of(UUID publisherKey, UUID draftKey) {
        return new DraftFileNamingStrategyContext(publisherKey, draftKey, true);
    }

}
