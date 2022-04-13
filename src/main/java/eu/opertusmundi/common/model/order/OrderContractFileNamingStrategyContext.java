package eu.opertusmundi.common.model.order;

import java.util.UUID;

import eu.opertusmundi.common.model.file.FileNamingStrategyContext;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderContractFileNamingStrategyContext extends FileNamingStrategyContext {

    protected OrderContractFileNamingStrategyContext(Integer userId, UUID orderKey, Integer itemIndex, boolean createIfNotExists) {
        super(createIfNotExists);

        this.userId 	= userId;
        this.orderKey	= orderKey;
        this.itemIndex	= itemIndex;
    }

    private Integer userId;

    private UUID orderKey;

    private Integer itemIndex;

    public static OrderContractFileNamingStrategyContext of(Integer userId, UUID orderKey) {
        return OrderContractFileNamingStrategyContext.of(userId, orderKey, 1, true);
    }

    public static OrderContractFileNamingStrategyContext of(Integer userId, UUID orderKey, Integer itemIndex, boolean createIfNotExists) {
        return new OrderContractFileNamingStrategyContext(userId, orderKey, itemIndex, true);
    }

}
