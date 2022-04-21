package eu.opertusmundi.common.service.contract;

import java.nio.file.Path;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import eu.opertusmundi.common.model.catalogue.client.EnumContractType;
import eu.opertusmundi.common.model.file.FileSystemException;
import eu.opertusmundi.common.model.file.FileSystemMessageCode;

@Service
public class DefaultContractFileManager implements ContractFileManager {

    private static final Logger logger = LoggerFactory.getLogger(DefaultContractFileManager.class);

    private final ContractFileNamingStrategy contractFileNamingStrategy;

    @Autowired
    public DefaultContractFileManager(ContractFileNamingStrategy contractFileNamingStrategy) {
        this.contractFileNamingStrategy = contractFileNamingStrategy;
    }

    @Override
    public Path resolveMasterContractPath(
        Integer userId, UUID orderKey, Integer itemIndex, boolean signed
    ) throws FileSystemException {
        Assert.notNull(userId, "Expected a non-null user identifier");
        Assert.notNull(orderKey, "Expected a non-null order key");
        Assert.notNull(itemIndex, "Expected a non-null order item index");
        Assert.isTrue(itemIndex > 0, "Expected an order item index greater than zero");

        try {
            final ContractFileNamingStrategyContext ctx = ContractFileNamingStrategyContext.builder()
                .type(EnumContractType.MASTER_CONTRACT)
                .userId(userId)
                .orderKey(orderKey)
                .itemIndex(itemIndex)
                .signed(signed)
                .build();

            final Path absolutePath = this.contractFileNamingStrategy.resolvePath(ctx);

            return absolutePath;
        } catch (final Exception ex) {
            logger.error(String.format("Failed to resolve template contract path [userId=%d, orderKey=%s, itemIndex=%d]", userId, orderKey, itemIndex), ex);
            throw new FileSystemException(FileSystemMessageCode.IO_ERROR, "Failed to resolve contract path", ex);
        }
    }

    @Override
    public Path resolveUploadedContractPath(Integer userId, UUID orderKey, Integer itemIndex, boolean signed) throws FileSystemException {
        Assert.notNull(userId, "Expected a non-null user identifier");
        Assert.notNull(orderKey, "Expected a non-null order key");
        Assert.notNull(itemIndex, "Expected a non-null order item index");
        Assert.isTrue(itemIndex > 0, "Expected an order item index greater than zero");

        try {
            final ContractFileNamingStrategyContext ctx = ContractFileNamingStrategyContext.builder()
                .type(EnumContractType.UPLOADED_CONTRACT)
                .userId(userId)
                .orderKey(orderKey)
                .itemIndex(itemIndex)
                .signed(signed)
                .build();

            final Path absolutePath = this.contractFileNamingStrategy.resolvePath(ctx);

            return absolutePath;
        } catch (final Exception ex) {
            logger.error(String.format("Failed to resolve path uploaded contract [userId=%d, orderKey=%s, itemIndex=%d]", userId, orderKey, itemIndex), ex);
            throw new FileSystemException(FileSystemMessageCode.IO_ERROR, "Failed to resolve contract path", ex);
        }
    }

}
