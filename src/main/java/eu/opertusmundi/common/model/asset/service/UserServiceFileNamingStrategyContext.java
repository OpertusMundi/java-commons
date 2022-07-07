package eu.opertusmundi.common.model.asset.service;

import java.util.UUID;

import eu.opertusmundi.common.model.file.FileNamingStrategyContext;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserServiceFileNamingStrategyContext extends FileNamingStrategyContext {

    protected UserServiceFileNamingStrategyContext(UUID ownerKey, UUID serviceKey, boolean createIfNotExists) {
        super(createIfNotExists);

        this.ownerKey   = ownerKey;
        this.serviceKey = serviceKey;
    }

    private UUID ownerKey;

    private UUID serviceKey;

    public static UserServiceFileNamingStrategyContext of(UUID ownerKey, UUID serviceKey) {
        return new UserServiceFileNamingStrategyContext(ownerKey, serviceKey, true);
    }

}
