package eu.opertusmundi.common.service;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import eu.opertusmundi.common.model.asset.service.UserServiceFileNamingStrategyContext;

@Service
public class DefaultUserServiceFileNamingStrategy extends AbstractFileNamingStrategy<UserServiceFileNamingStrategyContext> {

    @Autowired
    private Path userServiceDirectory;

    @Override
    public Path getDir(UserServiceFileNamingStrategyContext ctx) throws IOException {
        Assert.notNull(ctx, "Expected a non-null context");

        final Path ownerDir   = this.userServiceDirectory.resolve(Paths.get(ctx.getOwnerKey().toString()));
        final Path serviceDir = ownerDir.resolve(Paths.get(ctx.getServiceKey().toString()));

        for (final Path p : new Path[]{ownerDir, serviceDir}) {
            if (ctx.isCreateIfNotExists() && !Files.exists(p)) {
                try {
                    Files.createDirectories(p);
                    Files.setPosixFilePermissions(p, DEFAULT_DIRECTORY_PERMISSIONS);
                } catch (final FileAlreadyExistsException ex) {
                    // Another thread may have created this entry
                }
            }
        }

        return serviceDir;
    }

}