package eu.opertusmundi.common.service;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.google.common.base.Charsets;

import eu.opertusmundi.common.model.file.DirectoryDto;
import eu.opertusmundi.common.model.file.FilePathCommand;
import eu.opertusmundi.common.model.file.FileSystemException;
import eu.opertusmundi.common.model.file.FileSystemMessageCode;
import eu.opertusmundi.common.model.file.FileUploadCommand;
import eu.opertusmundi.common.model.file.QuotaDto;
import eu.opertusmundi.common.model.file.UserFileNamingStrategyContext;

@Service
public class DefaultUserFileManager implements UserFileManager {

    private static final Logger logger = LoggerFactory.getLogger(DefaultUserFileManager.class);

    private long maxUserSpace;

    private static final Set<PosixFilePermission> DEFAULT_DIRECTORY_PERMISSIONS = PosixFilePermissions.fromString("rwxrwxr-x");

    @Value("${opertus-mundi.file-system.user-max-space:20971520000}")
    private void setMaxUserSpace(String maxUserSpace) {
        this.maxUserSpace = this.parseSize(maxUserSpace);
    }

    @Autowired
    private DefaultUserFileNamingStrategy fileNamingStrategy;

    @Autowired
    private DirectoryTraverse directoryTraverse;

    @Override
    public QuotaDto getQuota(String userName) throws FileSystemException {
        Assert.hasText(userName, "Expected a non-empty user name");

        try {
            final UserFileNamingStrategyContext ctx = UserFileNamingStrategyContext.of(userName, false);

            final Path quotaDir          = this.fileNamingStrategy.resolvePath(ctx, "/.quota");
            final Path spaceFilePath     = quotaDir.resolve("report/space");
            final Path spaceUsedFilePath = quotaDir.resolve("report/space-used");

            if (!Files.exists(spaceFilePath) || !(Files.exists(spaceUsedFilePath))) {
                return null;
            }

            final long total = Long.parseLong(FileUtils.readFileToString(spaceFilePath.toFile(), Charsets.UTF_8));
            final long used  = Long.parseLong(FileUtils.readFileToString(spaceUsedFilePath.toFile(), Charsets.UTF_8));

            return QuotaDto.of(total, used);
        } catch (final Exception ex) {
            logger.error(String.format("Failed to get user quota [userName=%s]", userName), ex);

            throw new FileSystemException(FileSystemMessageCode.IO_ERROR, "An unknown error has occurred", ex);

        }
    }

    @Override
    public DirectoryDto browse(FilePathCommand command) throws FileSystemException {
        Assert.notNull(command, "Expected a non-null command");
        Assert.hasText(command.getUserName(), "Expected a non-null user name");
        Assert.isTrue(!StringUtils.isBlank(command.getPath()), "Expected a non-empty path");

        try {
            final UserFileNamingStrategyContext ctx = UserFileNamingStrategyContext.of(command.getUserName());

            final Path target = command.getPath().equals("/")
                ? this.fileNamingStrategy.getDir(ctx)
                : this.fileNamingStrategy.resolvePath(ctx, command.getPath());

            return this.directoryTraverse.getDirectoryInfo(target, ctx::validateName);
        } catch (final Exception ex) {
            logger.error(String.format("Failed to load files. [userName=%s, path=%s]", command.getUserName(), command.getPath()), ex);

            throw new FileSystemException(FileSystemMessageCode.IO_ERROR, "An unknown error has occurred", ex);
        }
    }

    @Override
    public void createPath(FilePathCommand command) throws FileSystemException {
        Assert.notNull(command, "Expected a non-null command");
        Assert.hasText(command.getUserName(), "Expected a non-null user name");

        try {
            if (StringUtils.isEmpty(command.getPath())) {
                throw new FileSystemException(FileSystemMessageCode.PATH_IS_EMPTY, "A path is required");
            }

            final UserFileNamingStrategyContext ctx = UserFileNamingStrategyContext.of(command.getUserName());
            final Path                          dir = this.fileNamingStrategy.resolvePath(ctx, command.getPath());

            if (Files.exists(dir)) {
                throw new FileSystemException(
                    FileSystemMessageCode.PATH_ALREADY_EXISTS,
                    String.format("Directory [%s] already exists", command.getPath())
                );
            }

            Files.createDirectories(dir);
            Files.setPosixFilePermissions(dir, DEFAULT_DIRECTORY_PERMISSIONS);
        } catch (final FileSystemException ex) {
            throw ex;
        } catch (final Exception ex) {
            logger.error(String.format("Failed to create path. [userName=%s, path=%s]", command.getUserName(), command.getPath()), ex);

            throw new FileSystemException(FileSystemMessageCode.IO_ERROR, "An unknown error has occurred", ex);
        }
    }

    @Override
    public void uploadFile(InputStream input, FileUploadCommand command) throws FileSystemException {
        Assert.notNull(command, "Expected a non-null command");
        Assert.hasText(command.getUserName(), "Expected a non-null user name");
        Assert.isTrue(command.getSize() > 0, "Expected file size to be greater than 0");

        try  {
            final UserFileNamingStrategyContext ctx         = UserFileNamingStrategyContext.of(command.getUserName());
            final Path                          userDir     = this.fileNamingStrategy.getDir(ctx);
            final DirectoryDto                  userDirInfo = this.directoryTraverse.getDirectoryInfo(userDir, ctx::validateName);

            final long size = userDirInfo.getSize();
            if (size + command.getSize() > this.maxUserSpace) {
                throw new FileSystemException(FileSystemMessageCode.NOT_ENOUGH_SPACE, "Insufficient storage space");
            }

            if (StringUtils.isBlank(command.getPath())) {
                command.setPath("/");
            }
            if (StringUtils.isEmpty(command.getFileName())) {
                throw new FileSystemException(FileSystemMessageCode.PATH_IS_EMPTY, "File name is not set");
            }

            final Path relativePath = Paths.get(command.getPath(), command.getFileName());
            final Path absolutePath = this.fileNamingStrategy.resolvePath(ctx, relativePath);
            final File localFile    = absolutePath.toFile();

            if (localFile.isDirectory()) {
                throw new FileSystemException(FileSystemMessageCode.PATH_IS_DIRECTORY, "File is a directory");
            }

            final String localFolder = localFile.getParent();

            if (!StringUtils.isBlank(localFolder)) {
                Files.createDirectories(Paths.get(localFolder));
                Files.setPosixFilePermissions(Paths.get(localFolder), DEFAULT_DIRECTORY_PERMISSIONS);
            }

            if (localFile.exists()) {
                if (command.isOverwrite()) {
                    FileUtils.deleteQuietly(localFile);
                } else {
                    throw new FileSystemException(FileSystemMessageCode.PATH_ALREADY_EXISTS, "File with the same name already exists");
                }
            }

            Files.copy(input, absolutePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (final FileSystemException ex) {
            throw ex;
        } catch (final Exception ex) {
            throw new FileSystemException(FileSystemMessageCode.IO_ERROR, "An unknown error has occurred", ex);
        }
    }

    @Override
    public void deletePath(FilePathCommand command) throws FileSystemException {
        Assert.notNull(command, "Expected a non-null command");
        Assert.hasText(command.getUserName(), "Expected a non-null user name");

        try {
            if (StringUtils.isEmpty(command.getPath())) {
                throw new FileSystemException(FileSystemMessageCode.PATH_IS_EMPTY, "A path is required");
            }

            final UserFileNamingStrategyContext ctx          = UserFileNamingStrategyContext.of(command.getUserName());
            final Path                          absolutePath = this.fileNamingStrategy.resolvePath(ctx, command.getPath());
            final File                          file         = absolutePath.toFile();

            if (!file.exists()) {
                throw new FileSystemException(FileSystemMessageCode.PATH_NOT_FOUND, "Path does not exist");
            }
            if (file.isDirectory() && file.listFiles().length != 0) {
                throw new FileSystemException(FileSystemMessageCode.PATH_NOT_EMPTY, "Path is not empty");
            }

            Files.delete(absolutePath);
        } catch (final FileSystemException ex) {
            throw ex;
        } catch (final Exception ex) {
            throw new FileSystemException(FileSystemMessageCode.IO_ERROR, "An unknown error has occurred", ex);
        }
    }

    @Override
    public Path resolveFilePath(FilePathCommand command) throws FileSystemException {
        Assert.notNull(command, "Expected a non-null command");
        Assert.hasText(command.getUserName(), "Expected a non-null user name");

        try {
            if (StringUtils.isEmpty(command.getPath())) {
                throw new FileSystemException(FileSystemMessageCode.PATH_IS_EMPTY, "A path to the file is required");
            }

            final UserFileNamingStrategyContext ctx          = UserFileNamingStrategyContext.of(command.getUserName());
            final Path                          absolutePath = this.fileNamingStrategy.resolvePath(ctx, command.getPath());
            final File                          file         = absolutePath.toFile();

            if (!file.exists()) {
                throw new FileSystemException(FileSystemMessageCode.PATH_NOT_FOUND, "File does not exist");
            } else if (file.isDirectory()) {
                throw new FileSystemException(FileSystemMessageCode.PATH_IS_DIRECTORY, "Path is not a file");
            }

            return file.toPath();
        } catch (final FileSystemException ex) {
            throw ex;
        } catch (final Exception ex) {
            logger.warn("Failed to resolve path. [userName={}, path={}]", command.getUserName(), command.getPath());

            throw new FileSystemException(FileSystemMessageCode.IO_ERROR, "An unknown error has occurred", ex);
        }
    }

    private long parseSize(String size) {
        Assert.hasText(size, "Size must not be empty");

        size = size.toUpperCase(Locale.ENGLISH);
        if (size.endsWith("KB")) {
            return Long.parseLong(size.substring(0, size.length() - 2)) * 1024;
        }
        if (size.endsWith("MB")) {
            return Long.parseLong(size.substring(0, size.length() - 2)) * 1024 * 1024;
        }
        if (size.endsWith("GB")) {
            return Long.parseLong(size.substring(0, size.length() - 2)) * 1024 * 1024 * 1024;
        }
        return Long.parseLong(size);
    }

}
