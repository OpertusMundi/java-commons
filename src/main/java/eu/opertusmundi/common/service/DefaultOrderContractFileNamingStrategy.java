package eu.opertusmundi.common.service;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import eu.opertusmundi.common.model.order.OrderContractFileNamingStrategyContext;

@Service
public class DefaultOrderContractFileNamingStrategy extends AbstractFileNamingStrategy<OrderContractFileNamingStrategyContext> {
   
	@Autowired
    private Path orderDirectory;

    @Override
    public Path getDir(OrderContractFileNamingStrategyContext ctx) throws IOException {
        Assert.notNull(ctx, "Expected a non-null context");

        final Path baseDir = this.orderDirectory.resolve(Paths.get(ctx.getUserId().toString()));
        final Path orderDir = baseDir.resolve(Paths.get(ctx.getOrderKey().toString()));
        final Path itemDir = orderDir.resolve(Paths.get(ctx.getItemIndex().toString()));
        
		for (Path p : new Path[] { baseDir, orderDir, itemDir }) {
			if (ctx.isCreateIfNotExists() && !Files.exists(p)) {
				try {
					Files.createDirectories(p);
					Files.setPosixFilePermissions(p, DEFAULT_DIRECTORY_PERMISSIONS);
				} catch (final FileAlreadyExistsException ex) {
					// Another thread may have created this entry
				}
			}
		}

        final String pathTemplate = "%s-%d-contract.pdf";

        return Paths.get(itemDir.toString(), String.format(pathTemplate, ctx.getOrderKey(), ctx.getItemIndex()));
        
    }

}