package eu.opertusmundi.common.service.invoice;

import java.io.File;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import eu.opertusmundi.common.model.file.FileSystemException;
import eu.opertusmundi.common.model.file.FileSystemMessageCode;

@Service
public class DefaultInvoiceFileManager implements InvoiceFileManager {

    private static final Logger logger = LoggerFactory.getLogger(DefaultInvoiceFileManager.class);

    @Autowired
    private InvoiceFileNamingStrategy fileNamingStrategy;

    @Override
    public Path resolvePath(
        Integer userId, String payInReferenceNumber
    ) throws FileSystemException {
        Assert.notNull(userId, "Expected a non-null user identifier");
        Assert.notNull(payInReferenceNumber, "Expected a non-null pay in reference number");

        try {
            final InvoiceFileNamingStrategyContext ctx = InvoiceFileNamingStrategyContext.of(userId, payInReferenceNumber);

            final Path absolutePath = this.fileNamingStrategy.resolvePath(ctx);
            final File file         = absolutePath.toFile();

            return file.toPath();
        } catch (final Exception ex) {
            logger.error(String.format("Failed to resolve path [userId=%d, payInReferenceNumber=%s]", userId, payInReferenceNumber), ex);
            throw new FileSystemException(FileSystemMessageCode.IO_ERROR, "Failed to resolve invoice path", ex);
        }
    }

}
