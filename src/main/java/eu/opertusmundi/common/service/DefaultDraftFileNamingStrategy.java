package eu.opertusmundi.common.service;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import eu.opertusmundi.common.model.DraftFileNamingStrategyContext;

@Service
public class DefaultDraftFileNamingStrategy extends AbstractFileNamingStrategy<DraftFileNamingStrategyContext> {
   
    @Autowired
    private Path draftDirectory;

    @Override
    public Path getDir(DraftFileNamingStrategyContext ctx) throws IOException {
        Assert.notNull(ctx, "Expected a non-null context");

        final Path baseDir = this.draftDirectory.resolve(Paths.get(ctx.getPublisherKey().toString(), ctx.getDraftKey().toString()));

        if (ctx.isCreateIfNotExists() && !Files.exists(baseDir)) {
            try {
                Files.createDirectories(baseDir, DEFAULT_DIRECTORY_ATTRIBUTE);
            } catch (final FileAlreadyExistsException ex) {
                // Another thread may have created this entry
            }
        }

        return baseDir;
    }

}