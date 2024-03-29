package eu.opertusmundi.common.service;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;

import org.springframework.util.Assert;

import eu.opertusmundi.common.model.file.FileNamingStrategyContext;
import eu.opertusmundi.common.model.file.FileSystemException;

public abstract class AbstractFileNamingStrategy<C extends FileNamingStrategyContext> implements UserFileNamingStrategy<C> {

    protected static final Set<PosixFilePermission> DEFAULT_DIRECTORY_PERMISSIONS = PosixFilePermissions.fromString("rwxrwxr-x");

    @Override
    public final Path resolvePath(C ctx, String relativePath) throws IOException, FileSystemException {
        Assert.notNull(ctx, "Expected a non-null context");
        Assert.hasText(relativePath, "Expected a non-empty path");

        return this.resolvePath(ctx, Paths.get(relativePath));
    }

    @Override
    public final Path resolvePath(C ctx, Path relativePath) throws IOException, FileSystemException {
        Assert.notNull(ctx, "Expected a non-null context");
        Assert.notNull(relativePath, "Expected a non-null path");

        this.validatePath(ctx, relativePath.toString());

        final Path baseDir = this.getDir(ctx);

        return Paths.get(baseDir.toString(), relativePath.toString());
    }

    protected void validatePath(C ctx, String path) throws FileSystemException {

    }

}