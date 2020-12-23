package eu.opertusmundi.common.service;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import eu.opertusmundi.common.model.FileSystemMessageCode;
import eu.opertusmundi.common.model.UserFileNamingStrategyContext;
import eu.opertusmundi.common.model.file.FileSystemException;

@Service
public class DefaultUserFileNamingStrategy extends AbstractFileNamingStrategy<UserFileNamingStrategyContext> {

    private final Pattern pattern = Pattern.compile("^(\\/?[a-zA-Z_\\-0-9]+)+(\\.[a-zA-Z0-9]+|\\/?)?$");

    @Value("${opertus-mundi.file-system.max-depth:8}")
    private int maxDepth;

    @Value("${opertus-mundi.file-system.max-length:255}")
    private int maxLength;

    public static final String SCHEME = "user-data";

    @Autowired
    private Path userDataDirectory;

    @Override
    public String getScheme() {
        return SCHEME;
    }

    @Override
    public Path getDir(UserFileNamingStrategyContext ctx) throws IOException {
        Assert.notNull(ctx, "Expected a non-null context");

        final Path baseDir = this.userDataDirectory.resolve(Integer.toString(ctx.getId()));

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
    public URI convertToUri(UserFileNamingStrategyContext ctx, Path relativePath) {
        Assert.notNull(ctx, "Expected a non-null context");
        Assert.notNull(relativePath, "A path is required");
        Assert.isTrue(!relativePath.isAbsolute(), "Expected a relative path");

        URI uri = null;
        try {
            uri = new URI(SCHEME, String.valueOf(ctx.getId()), "/" + relativePath.toString(), null);
        } catch (final URISyntaxException ex) {
            throw new IllegalArgumentException(ex);
        }
        return uri;
    }

    @Override
    public URI convertToUri(Path path) {
        Assert.notNull(path, "A path is required");
        Assert.isTrue(path.isAbsolute(), "Expected an absolute path");
        Assert.isTrue(path.startsWith(this.userDataDirectory), "The path is outside user-data directory");

        final Path userPath       = this.userDataDirectory.relativize(path);
        final int  userPathLength = userPath.getNameCount();

        Assert.isTrue(userPathLength > 1, "The relative path is too short");

        final int userId = Integer.parseInt(userPath.getName(0).toString());

        return this.convertToUri(UserFileNamingStrategyContext.of(userId), userPath.subpath(1, userPathLength));
    }

    @Override
    public Path resolveUri(URI uri) throws IOException {
        Assert.notNull(uri, "A user-data URI is required");
        Assert.isTrue(SCHEME.equals(uri.getScheme()), "The given URI has an unexpected scheme");

        final int    userId = Integer.parseInt(uri.getHost());
        final String path   = uri.getPath();

        Assert.isTrue(path.startsWith("/"), "The URI path was expected as an absolute path");

        return this.resolvePath(UserFileNamingStrategyContext.of(userId), Paths.get(path.substring(1)));
    }

    @Override
    public void validatePath(String path) throws FileSystemException {
        final String[] parts = StringUtils.split(path, "/");

        if (parts.length > this.maxDepth) {
            throw new FileSystemException(
                FileSystemMessageCode.PATH_MAX_DEPTH,
                String.format("Path [%s] depth exceeds the limit [%d]", path, this.maxDepth)
            );
        }
        for (final String p : parts) {
            if (p.length() > this.maxLength) {
                throw new FileSystemException(
                    FileSystemMessageCode.PATH_MAX_LENGTH,
                    String.format("Path segment [%s] length exceeds the limit [%d]", p, this.maxLength)
                );
            }
        }
        final Matcher matcher = this.pattern.matcher(path);
        if (!matcher.matches()) {
            throw new FileSystemException(
                FileSystemMessageCode.INVALID_PATH,
                String.format("Path [%s] is not valid", path)
            );
        }
    }

}