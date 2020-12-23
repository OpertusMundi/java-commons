package eu.opertusmundi.common.service;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import eu.opertusmundi.common.model.FileNamingStrategyContext;
import eu.opertusmundi.common.model.file.FileSystemException;

public abstract class AbstractFileNamingStrategy<C extends FileNamingStrategyContext> implements UserFileNamingStrategy<C> {

    @Override
    public Path resolvePath(C ctx, String relativePath) throws IOException, FileSystemException {
        Assert.notNull(ctx, "Expected a non-null context");
        Assert.isTrue(!StringUtils.isEmpty(relativePath), "Expected a non-empty path");

        this.validatePath(relativePath);

        return this.resolvePath(ctx, Paths.get(relativePath));
    }

    @Override
    public Path resolvePath(C ctx, Path relativePath) throws IOException, FileSystemException {
        Assert.notNull(ctx, "Expected a non-null context");
        Assert.notNull(relativePath, "Expected a non-null path");

        this.validatePath(relativePath.toString());

        final Path baseDir = this.getDir(ctx);

        return Paths.get(baseDir.toString(), relativePath.toString());
    }

    public void validatePath(String path) throws FileSystemException {

    }

}