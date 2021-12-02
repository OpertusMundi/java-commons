package eu.opertusmundi.common.service.contract;

import java.io.File;
import java.nio.file.Path;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import eu.opertusmundi.common.model.file.FileSystemException;
import eu.opertusmundi.common.model.file.FileSystemMessageCode;

@Service
public class DefaultContractFileManager implements ContractFileManager {

    private static final Logger logger = LoggerFactory.getLogger(DefaultContractFileManager.class);

    @Autowired
    private ContractFileNamingStrategy fileNamingStrategy;

    @Override
    public Path resolvePath(
        Integer userId, UUID orderKey, Integer itemIndex, boolean signed
    ) throws FileSystemException {
        Assert.notNull(userId, "Expected a non-null user identifier");
        Assert.notNull(orderKey, "Expected a non-null order key");
        Assert.notNull(itemIndex, "Expected a non-null order item index");
        Assert.isTrue(itemIndex > 0, "Expected an order item index greater than zero");

        try {
            final ContractFileNamingStrategyContext ctx = ContractFileNamingStrategyContext.of(userId, orderKey, itemIndex, signed);

            final Path absolutePath = this.fileNamingStrategy.resolvePath(ctx);
            final File file         = absolutePath.toFile();

            return file.toPath();
        } catch (final Exception ex) {
            logger.error(String.format("Failed to resolve path [userId=%d, orderKey=%s, itemIndex=%d]", userId, orderKey, itemIndex), ex);
            throw new FileSystemException(FileSystemMessageCode.IO_ERROR, "Failed to resolve contract path", ex);
        }
    }

}
