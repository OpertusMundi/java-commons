package eu.opertusmundi.common.service;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

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
    public String getScheme() {
        return SCHEME;
    }

    @Override
    public Path getDir(AssetFileNamingStrategyContext ctx) throws IOException {
        Assert.notNull(ctx, "Expected a non-null context");

        final Path baseDir = this.assetDirectory.resolve(ctx.getKey().toString());

        if (ctx.isCreateIfNotExists() && !Files.exists(baseDir)) {
            try {
                Files.createDirectory(baseDir);
            } catch (final FileAlreadyExistsException ex) {
                // Another thread may have created this entry
            }
        }

        return baseDir;
    }

    @Override
    public URI convertToUri(AssetFileNamingStrategyContext ctx, Path relativePath) {
        Assert.notNull(ctx, "Expected a non-null context");
        Assert.notNull(relativePath, "A path is required");
        Assert.isTrue(!relativePath.isAbsolute(), "Expected a relative path");

        URI uri = null;
        try {
            uri = new URI(SCHEME, String.valueOf(ctx.getKey().toString()), "/" + relativePath.toString(), null);
        } catch (final URISyntaxException ex) {
            throw new IllegalArgumentException(ex);
        }
        return uri;
    }

    @Override
    public URI convertToUri(Path path) {
        Assert.notNull(path, "A path is required");
        Assert.isTrue(path.isAbsolute(), "Expected an absolute path");
        Assert.isTrue(path.startsWith(this.assetDirectory), "The path is outside asset directory");

        final Path userPath       = this.assetDirectory.relativize(path);
        final int  userPathLength = userPath.getNameCount();

        Assert.isTrue(userPathLength > 1, "The relative path is too short");

        final UUID key = UUID.fromString(userPath.getName(0).toString());

        return this.convertToUri(AssetFileNamingStrategyContext.of(key), userPath.subpath(1, userPathLength));
    }

    @Override
    public Path resolveUri(URI uri) throws IOException {
        Assert.notNull(uri, "A asset URI is required");
        Assert.isTrue(SCHEME.equals(uri.getScheme()), "The given URI has an unexpected scheme");

        final UUID   key  = UUID.fromString(uri.getHost());
        final String path = uri.getPath();

        Assert.isTrue(path.startsWith("/"), "The URI path was expected as an absolute path");

        return this.resolvePath(AssetFileNamingStrategyContext.of(key), Paths.get(path.substring(1)));
    }

}