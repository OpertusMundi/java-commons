package eu.opertusmundi.common.service;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.logging.log4j.core.util.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import eu.opertusmundi.common.model.AssetFileNamingStrategyContext;

@Service
public class DefaultAssetFileNamingStrategy extends AbstractFileNamingStrategy<AssetFileNamingStrategyContext> {

    public static final String SCHEME = "asset-data";

    @Autowired
    private Path assetDirectory;

    @Override
    public Path getDir(AssetFileNamingStrategyContext ctx) throws IOException {
        Assert.notNull(ctx, "Expected a non-null context");

        final Path baseDir = this.assetDirectory.resolve(Paths.get(ctx.getPublisherKey().toString(), ctx.getDraftKey().toString()));

        if (ctx.isCreateIfNotExists() && !Files.exists(baseDir)) {
            try {
                FileUtils.mkdir(baseDir.toFile(), true);
            } catch (final FileAlreadyExistsException ex) {
                // Another thread may have created this entry
            }
        }

        return baseDir;
    }

}