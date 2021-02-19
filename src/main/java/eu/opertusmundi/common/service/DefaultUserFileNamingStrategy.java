package eu.opertusmundi.common.service;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
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

    @Autowired
    private Path userDirectory;

    @Override
    public Path getDir(UserFileNamingStrategyContext ctx) throws IOException {
        Assert.notNull(ctx, "Expected a non-null context");

        final Path baseDir = this.userDirectory.resolve(Integer.toString(ctx.getId()));

        if (ctx.isCreateIfNotExists() && !Files.exists(baseDir)) {
            try {
                Files.createDirectories(baseDir, DEFAULT_DIRECTORY_ATTRIBUTE);
            } catch (final FileAlreadyExistsException ex) {
                // Another thread may have created this entry
            }
        }

        return baseDir;
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