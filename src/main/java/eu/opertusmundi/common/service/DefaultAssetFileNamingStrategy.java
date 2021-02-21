package eu.opertusmundi.common.service;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import eu.opertusmundi.common.model.AssetFileNamingStrategyContext;

@Service
public class DefaultAssetFileNamingStrategy extends AbstractFileNamingStrategy<AssetFileNamingStrategyContext> {

    @Autowired
    private Path assetDirectory;

    @Override
    public Path getDir(AssetFileNamingStrategyContext ctx) throws IOException {
        Assert.notNull(ctx, "Expected a non-null context");

        final Path baseDir = this.assetDirectory.resolve(Paths.get(ctx.getPid()));

        if (ctx.isCreateIfNotExists() && !Files.exists(baseDir)) {
            try {
                Files.createDirectories(baseDir);
                Files.setPosixFilePermissions(baseDir, DEFAULT_DIRECTORY_PERMISSIONS);
            } catch (final FileAlreadyExistsException ex) {
                // Another thread may have created this entry
            }
        }

        return baseDir;
    }

}