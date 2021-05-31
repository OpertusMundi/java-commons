package eu.opertusmundi.common.service;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import eu.opertusmundi.common.model.asset.DraftFileNamingStrategyContext;

@Service
public class DefaultDraftFileNamingStrategy extends AbstractFileNamingStrategy<DraftFileNamingStrategyContext> {
   
    @Autowired
    private Path draftDirectory;

    @Override
    public Path getDir(DraftFileNamingStrategyContext ctx) throws IOException {
        Assert.notNull(ctx, "Expected a non-null context");

        final Path publisherDir = this.draftDirectory.resolve(Paths.get(ctx.getPublisherKey().toString()));
        final Path draftDir     = publisherDir.resolve(Paths.get(ctx.getDraftKey().toString()));

        for (Path p : new Path[]{publisherDir, draftDir}) {
            if (ctx.isCreateIfNotExists() && !Files.exists(p)) {
                try {
                    Files.createDirectories(p);
                    Files.setPosixFilePermissions(p, DEFAULT_DIRECTORY_PERMISSIONS);
                } catch (final FileAlreadyExistsException ex) {
                    // Another thread may have created this entry
                }
            }
        }

        return draftDir;
    }

}