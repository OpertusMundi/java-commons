package eu.opertusmundi.common.service.contract;

import java.util.UUID;

import eu.opertusmundi.common.model.catalogue.client.EnumContractType;
import eu.opertusmundi.common.model.file.FileNamingStrategyContext;
import lombok.Builder;
import lombok.Getter;

@Getter
public class ContractFileNamingStrategyContext extends FileNamingStrategyContext {

    @Builder
    protected ContractFileNamingStrategyContext(
        EnumContractType type, Integer userId, UUID orderKey, Integer itemIndex, UUID annexKey, boolean signed
    ) {
        super(true);

        this.annexKey  = annexKey;
        this.itemIndex = itemIndex;
        this.orderKey  = orderKey;
        this.signed    = signed;
        this.type      = type;
        this.userId    = userId;
    }

    private final UUID             annexKey;
    private final Integer          itemIndex;
    private final UUID             orderKey;
    private final boolean          signed;
    private final EnumContractType type;
    private final Integer          userId;

}
