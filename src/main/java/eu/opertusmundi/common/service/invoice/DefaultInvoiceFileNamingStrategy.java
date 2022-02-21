package eu.opertusmundi.common.service.invoice;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Service
public class DefaultInvoiceFileNamingStrategy implements InvoiceFileNamingStrategy {

    protected static final Set<PosixFilePermission> DEFAULT_DIRECTORY_PERMISSIONS = PosixFilePermissions.fromString("rwxrwxr-x");

    @Autowired
    private Path invoiceDirectory;

    @Override
    public Path resolvePath(InvoiceFileNamingStrategyContext ctx) throws IOException {
        Assert.notNull(ctx, "Expected a non-null context");

        final Path baseDir = this.invoiceDirectory.resolve(Paths.get(ctx.getUserId().toString()));
        
        if (ctx.isCreateIfNotExists() && !Files.exists(baseDir)) {
            try {
                Files.createDirectories(baseDir);
                Files.setPosixFilePermissions(baseDir, DEFAULT_DIRECTORY_PERMISSIONS);
            } catch (final FileAlreadyExistsException ex) {
                // Another thread may have created this entry
            }
        }

        final String pathTemplate = "Invoice-%s.pdf";

        return Paths.get(baseDir.toString(), String.format(pathTemplate, ctx.getPayInReferenceNumber()));
    }

}