package eu.opertusmundi.common.service.contract;

import java.util.UUID;

import eu.opertusmundi.common.model.contract.EnumContract;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public  class ContractNamingStrategyContext {

    protected ContractNamingStrategyContext(boolean createIfNotExists) {
        this.createIfNotExists = createIfNotExists;
    }

    protected ContractNamingStrategyContext(boolean createIfNotExists, UUID providerKey, UUID userKey, EnumContract type) {
        super();
        this.createIfNotExists = createIfNotExists;
        this.providerKey       = providerKey;
        this.userKey           = userKey;
        this.type              = type;
    }

    private final boolean createIfNotExists;

    private UUID providerKey;

    private UUID userKey;

    private EnumContract type;

}
