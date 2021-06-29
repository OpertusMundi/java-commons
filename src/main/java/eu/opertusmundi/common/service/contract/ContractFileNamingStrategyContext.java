package eu.opertusmundi.common.service.contract;

import java.util.UUID;

import eu.opertusmundi.common.model.file.FileNamingStrategyContext;
import io.jsonwebtoken.lang.Assert;
import lombok.Getter;

@Getter
public class ContractFileNamingStrategyContext extends FileNamingStrategyContext {

    protected ContractFileNamingStrategyContext(Integer userId, UUID orderKey, Integer itemIndex, boolean signed) {
        super(true);

        this.userId    = userId;
        this.orderKey  = orderKey;
        this.itemIndex = itemIndex;
        this.signed    = signed;
    }

    private final Integer userId;
    private final UUID    orderKey;
    private final Integer itemIndex;
    private final boolean signed;

    public static ContractFileNamingStrategyContext of(Integer userId, UUID orderKey, boolean signed) {
        return ContractFileNamingStrategyContext.of(userId, orderKey, 1, signed);
    }

    public static ContractFileNamingStrategyContext of(Integer userId, UUID orderKey, Integer itemIndex, boolean signed) {
        Assert.notNull(userId, "Expected a non-null user identifier");
        Assert.notNull(orderKey, "Expected a non-null order key");
        Assert.notNull(itemIndex, "Expected a non-null order item index");
        Assert.isTrue(itemIndex > 0, "Expected an order item index greater than zero");

        return new ContractFileNamingStrategyContext(userId, orderKey, itemIndex, signed);
    }

}
