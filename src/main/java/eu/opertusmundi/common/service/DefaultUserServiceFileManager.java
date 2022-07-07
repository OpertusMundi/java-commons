package eu.opertusmundi.common.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import eu.opertusmundi.common.model.asset.UserServiceMessageCode;
import eu.opertusmundi.common.model.asset.service.UserServiceCommandDto;
import eu.opertusmundi.common.model.asset.service.UserServiceFileNamingStrategyContext;
import eu.opertusmundi.common.model.file.FileDto;
import eu.opertusmundi.common.model.file.FileSystemException;
import eu.opertusmundi.common.model.file.FileSystemMessageCode;

@Service
public class DefaultUserServiceFileManager implements UserServiceFileManager {

    private static final Logger logger = LoggerFactory.getLogger(DefaultUserServiceFileManager.class);

    private static final Logger fileManagerLogger = LoggerFactory.getLogger("USER_SERVICE_FILE_MANAGER");

    private static final String METADATA_PATH = "/metadata";

    private static final Set<PosixFilePermission> DEFAULT_DIRECTORY_PERMISSIONS = PosixFilePermissions.fromString("rwxrwxr-x");

    @Autowired
    private DefaultUserServiceFileNamingStrategy namingStrategy;

    @Override
    public List<FileDto> getResources(UUID ownerKey, UUID serviceKey) throws FileSystemException, UserServiceException {
        Assert.notNull(ownerKey, "Expected non-null owner key");
        Assert.notNull(serviceKey, "Expected non-null service key");

        try {
            final UserServiceFileNamingStrategyContext ctx    = UserServiceFileNamingStrategyContext.of(ownerKey, serviceKey);
            final Path                                 target = this.namingStrategy.getDir(ctx);

            final List<FileDto> resources = new ArrayList<FileDto>();
            final File          dir       = target.toFile();

            if (dir.exists()) {
                for (final File entry : dir.listFiles()) {
                    // Ignore any folders e.g. the metadata folder
                    if (entry.isFile()) {
                        resources.add(new FileDto(
                            entry.getName(), Paths.get(entry.getName()).toString(), entry.length(), entry.lastModified()
                        ));
                    }
                }
            }

            return resources;
        } catch (final FileSystemException ex) {
            throw ex;
        } catch (final Exception ex) {
            logger.error(String.format("Failed to load resource files. [ownerKey=%s, serviceKey=%s]", ownerKey, serviceKey), ex);

            throw new UserServiceException("An unknown error has occurred", ex);
        }
    }

    @Override
    public void uploadResource(
        UserServiceCommandDto command, InputStream input
    ) throws UserServiceException, FileSystemException {
        Assert.notNull(command, "Expected a non-null command");
        Assert.notNull(command.getOwnerKey(), "Expected a non-null owner key");
        Assert.notNull(command.getServiceKey(), "Expected a non-null service key");
        Assert.hasText(command.getPath(), "Expected a non-empty file path");
        Assert.hasText(command.getFileName(), "Expected a non-empty file name");
        Assert.isTrue(command.getFileSize() > 0, "Expected file size to be greater than 0");

        try {
            final UserServiceFileNamingStrategyContext ctx    = UserServiceFileNamingStrategyContext.of(command.getOwnerKey(), command.getServiceKey());
            final Path                           relativePath = Paths.get(command.getFileName());
            final Path                           absolutePath = this.namingStrategy.resolvePath(ctx, relativePath);
            final File                           localFile    = absolutePath.toFile();

            // Create parent directory
            if (!absolutePath.getParent().toFile().exists()) {
                Files.createDirectories(absolutePath.getParent());
                Files.setPosixFilePermissions(absolutePath.getParent(), DEFAULT_DIRECTORY_PERMISSIONS);
            }

            if (localFile.exists()) {
                FileUtils.deleteQuietly(localFile);
            }

            Files.copy(input, absolutePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (final FileSystemException ex) {
            throw ex;
        } catch (final Exception ex) {
            throw new UserServiceException(UserServiceMessageCode.IO_ERROR, "An unknown error has occurred", ex);
        }
    }

    @Override
    public void deleteResource(UUID ownerKey, UUID serviceKey, String fileName) throws FileSystemException, UserServiceException {
        Assert.notNull(ownerKey, "Expected a non-null owner key");
        Assert.notNull(serviceKey, "Expected a non-null service key");
        Assert.isTrue(!StringUtils.isBlank(fileName), "Expected a non-empty file name");

        try {
            final UserServiceFileNamingStrategyContext ctx    = UserServiceFileNamingStrategyContext.of(ownerKey, serviceKey);
            final Path                           relativePath = Paths.get(fileName);
            final Path                           absolutePath = this.namingStrategy.resolvePath(ctx, relativePath);
            final File                           file         = absolutePath.toFile();

            if (!file.exists()) {
                throw new FileSystemException(FileSystemMessageCode.PATH_NOT_FOUND, "Path does not exist");
            }

            if (!FileUtils.deleteQuietly(absolutePath.toFile())) {
                fileManagerLogger.error(String.format("Resource [%s] was not deleted", relativePath));
            }
        } catch (final FileSystemException ex) {
            throw ex;
        } catch (final Exception ex) {
            throw new UserServiceException(UserServiceMessageCode.IO_ERROR, "An unknown error has occurred", ex);
        }
    }

    @Override
    public Path resolveResourcePath(UUID ownerKey, UUID serviceKey, String fileName) throws FileSystemException, UserServiceException {
        Assert.notNull(ownerKey, "Expected a non-null owner key");
        Assert.notNull(serviceKey, "Expected a non-null service key");
        Assert.hasText(fileName, "Expected a non-empty file name");

        try {
            final UserServiceFileNamingStrategyContext ctx    = UserServiceFileNamingStrategyContext.of(ownerKey, serviceKey);
            final Path                           relativePath = Paths.get(fileName);
            final Path                           absolutePath = this.namingStrategy.resolvePath(ctx, relativePath);
            final File                           file         = absolutePath.toFile();

            if (!file.exists()) {
                throw new FileSystemException(FileSystemMessageCode.PATH_NOT_FOUND, "File does not exist");
            }

            return file.toPath();
        } catch (final FileSystemException ex) {
            throw ex;
        } catch (final Exception ex) {
            logger.warn("Failed to resolve metadata path. [fileName={}, serviceKey={}]", fileName, serviceKey);

            throw new UserServiceException(UserServiceMessageCode.IO_ERROR, "An unknown error has occurred", ex);
        }
    }

    @Override
    public Path resolveMetadataPropertyPath(
        UUID ownerKey, UUID serviceKey, String fileName
    ) throws FileSystemException, UserServiceException {
        Assert.notNull(ownerKey, "Expected a non-null owner key");
        Assert.notNull(serviceKey, "Expected a non-null service key");
        Assert.isTrue(!StringUtils.isBlank(fileName), "Expected a non-empty file name");

        try {
            final UserServiceFileNamingStrategyContext ctx          = UserServiceFileNamingStrategyContext.of(ownerKey, serviceKey);
            final Path                                 relativePath = Paths.get(METADATA_PATH, fileName);
            final Path                                 absolutePath = this.namingStrategy.resolvePath(ctx, relativePath);

            return absolutePath;
        } catch (final FileSystemException ex) {
            throw ex;
        } catch (final Exception ex) {
            logger.warn("Failed to resolve metadata path. [fileName={}, serviceKey={}]", fileName, serviceKey);

            throw new UserServiceException(UserServiceMessageCode.IO_ERROR, "An unknown error has occurred", ex);
        }
    }

    private Path prepareMetadataFile(
        UUID ownerKey, UUID serviceKey, String fileName, String content
    ) throws FileSystemException, IOException {
        Assert.notNull(ownerKey, "Expected a non-null owner key");
        Assert.notNull(serviceKey, "Expected a non-null service key");
        Assert.isTrue(!StringUtils.isBlank(fileName), "Expected a non-empty file name");
        Assert.isTrue(!StringUtils.isBlank(content), "Expected non-empty content");

        final UserServiceFileNamingStrategyContext ctx          = UserServiceFileNamingStrategyContext.of(ownerKey, serviceKey);
        final Path                           relativePath = Paths.get(METADATA_PATH,  fileName);
        final Path                           absolutePath = this.namingStrategy.resolvePath(ctx, relativePath);
        final File                           localFile    = absolutePath.toFile();

        // Create parent directory
        if (!absolutePath.getParent().toFile().exists()) {
            Files.createDirectories(absolutePath.getParent());
            Files.setPosixFilePermissions(absolutePath.getParent(), DEFAULT_DIRECTORY_PERMISSIONS);
        }

        if (localFile.exists()) {
            FileUtils.deleteQuietly(localFile);
        }

        return absolutePath;
    }

    @Override
    public void saveMetadataAsText(UUID ownerKey, UUID serviceKey, String fileName, String content) throws UserServiceException {
        try {
            final Path path = this.prepareMetadataFile(ownerKey, serviceKey, fileName, content);

            FileUtils.writeStringToFile(path.toFile(), content, Charset.forName("UTF-8"));
        } catch (final FileSystemException ex) {
            throw ex;
        } catch (final Exception ex) {
            throw new UserServiceException(UserServiceMessageCode.IO_ERROR, "An unknown error has occurred", ex);
        }
    }

    @Override
    public void saveMetadataPropertyAsImage(
        UUID ownerKey, UUID serviceKey, String fileName, String content
    ) throws FileSystemException, UserServiceException {
        try {
            final Path   path = this.prepareMetadataFile(ownerKey, serviceKey, fileName, content);
            final byte[] data = Base64.getDecoder().decode(content);

            FileUtils.writeByteArrayToFile(path.toFile(), data);
        } catch (final FileSystemException ex) {
            throw ex;
        } catch (final Exception ex) {
            throw new UserServiceException(UserServiceMessageCode.IO_ERROR, "An unknown error has occurred", ex);
        }
    }

    @Override
    public void saveMetadataPropertyAsJson(
        UUID ownerKey, UUID serviceKey, String fileName, String content
    ) throws FileSystemException, UserServiceException {
        try {
            final Path path = this.prepareMetadataFile(ownerKey, serviceKey, fileName, content);

            FileUtils.writeStringToFile(path.toFile(), content, Charset.forName("UTF-8"));
        } catch (final FileSystemException ex) {
            throw ex;
        } catch (final Exception ex) {
            throw new UserServiceException(UserServiceMessageCode.IO_ERROR, "An unknown error has occurred", ex);
        }
    }

    @Override
    public void deleteAllFiles(UUID ownerKey, UUID serviceKey) {
        Assert.notNull(ownerKey, "Expected a non-null owner key");
        Assert.notNull(serviceKey, "Expected a non-null service key");

        try {
            final UserServiceFileNamingStrategyContext ctx          = UserServiceFileNamingStrategyContext.of(ownerKey, serviceKey);
            final Path                                 absolutePath = this.namingStrategy.getDir(ctx);

            if (!FileUtils.deleteQuietly(absolutePath.toFile())) {
                fileManagerLogger.error(String.format("Asset [%s]/[%s] file-system was not deleted", ownerKey, serviceKey));
            }
        } catch (final Exception ex) {
            // Ignore
        }
    }

    @Override
    public void reset(UUID ownerKey, UUID serviceKey) throws FileSystemException, UserServiceException {
        Assert.notNull(ownerKey, "Expected a non-null owner key");
        Assert.notNull(serviceKey, "Expected a non-null service key");

        try {
            final UserServiceFileNamingStrategyContext ctx          = UserServiceFileNamingStrategyContext.of(ownerKey, serviceKey);
            final Path                                 absolutePath = this.namingStrategy.resolvePath(ctx, METADATA_PATH);
            final File                                 file         = absolutePath.toFile();

            if (file.exists() && !FileUtils.deleteQuietly(file)) {
                fileManagerLogger.error(String.format("Failed to delete metadata path for service. [key=%s]", serviceKey));
            }
        } catch (final FileSystemException ex) {
            throw ex;
        } catch (final Exception ex) {
            throw new UserServiceException(UserServiceMessageCode.IO_ERROR, "An unknown error has occurred", ex);
        }
    }
}
