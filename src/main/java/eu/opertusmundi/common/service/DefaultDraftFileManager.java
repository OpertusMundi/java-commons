package eu.opertusmundi.common.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import eu.opertusmundi.common.model.asset.AssetAdditionalResourceCommandDto;
import eu.opertusmundi.common.model.asset.AssetContractAnnexCommandDto;
import eu.opertusmundi.common.model.asset.AssetFileNamingStrategyContext;
import eu.opertusmundi.common.model.asset.AssetMessageCode;
import eu.opertusmundi.common.model.asset.AssetRepositoryException;
import eu.opertusmundi.common.model.asset.DraftFileNamingStrategyContext;
import eu.opertusmundi.common.model.asset.FileResourceCommandDto;
import eu.opertusmundi.common.model.contract.provider.ProviderUploadContractCommand;
import eu.opertusmundi.common.model.file.FileDto;
import eu.opertusmundi.common.model.file.FileSystemException;
import eu.opertusmundi.common.model.file.FileSystemMessageCode;

@Service
public class DefaultDraftFileManager implements DraftFileManager {

    private static final Logger logger = LoggerFactory.getLogger(DefaultDraftFileManager.class);

    private static final Logger draftRepositoryLogger = LoggerFactory.getLogger("DRAFT_REPOSITORY");

    private static final String RESOURCE_PATH = "/resources";

    private static final String ADDITIONAL_RESOURCE_PATH = "/additional-resources";

    private static final String METADATA_PATH = "/metadata";

    private static final String CONTRACT_PATH = "/contract";

    private static final String CONTRACT_ANNEX_PATH = "/contract/annexes";

    private static final Set<PosixFilePermission> DEFAULT_DIRECTORY_PERMISSIONS = PosixFilePermissions.fromString("rwxrwxr-x");

    @Autowired
    private DefaultDraftFileNamingStrategy draftNamingStrategy;

    @Autowired
    private DefaultAssetFileNamingStrategy assetNamingStrategy;

    @Override
    public List<FileDto> getResources(UUID publisherKey, UUID draftKey) throws FileSystemException, AssetRepositoryException {
        return this.getResources(publisherKey, draftKey, RESOURCE_PATH);
    }

    @Override
    public List<FileDto> getAdditionalResources(UUID publisherKey, UUID draftKey) throws FileSystemException, AssetRepositoryException {
        return this.getResources(publisherKey, draftKey, ADDITIONAL_RESOURCE_PATH);
    }

    private List<FileDto> getResources(UUID publisherKey, UUID draftKey, String relativePath) throws FileSystemException, AssetRepositoryException {
        Assert.notNull(publisherKey, "Expected non-null publisher key");
        Assert.notNull(draftKey, "Expected non-null draft key");

        try {
            final DraftFileNamingStrategyContext ctx     = DraftFileNamingStrategyContext.of(publisherKey, draftKey);
            final Path                           userDir = this.draftNamingStrategy.getDir(ctx);
            final Path                           target  = Paths.get(userDir.toString(), relativePath);

            final List<FileDto> resources = new ArrayList<FileDto>();
            final File                   dir       = target.toFile();

            if (dir.exists()) {
                for (final File entry : dir.listFiles()) {
                    // Ignore any folders e.g. the metadata folder
                    if (entry.isFile()) {
                        resources.add(new FileDto(
                            entry.getName(), Paths.get(relativePath, entry.getName()).toString(), entry.length(), entry.lastModified()
                        ));
                    }
                }
            }

            return resources;
        } catch (final FileSystemException ex) {
            throw ex;
        } catch (final Exception ex) {
            logger.error(
                String.format(
                    "Failed to load resource files. [publisherKey=%s, draftKey=%s, relativePath=%s]",
                    publisherKey, draftKey, relativePath
                ), ex
            );

            throw new AssetRepositoryException("An unknown error has occurred", ex);
        }
    }

    @Override
    public void addResource(
        FileResourceCommandDto command, InputStream input
    ) throws AssetRepositoryException, FileSystemException {
        Assert.notNull(command, "Expected a non-null command");
        Assert.isTrue(command.getSize() > 0, "Expected file size to be greater than 0");

        this.saveResourceFile(command.getPublisherKey(), command.getDraftKey(), RESOURCE_PATH, command.getFileName(), input);
    }

    @Override
    public void addAdditionalResource(
        AssetAdditionalResourceCommandDto command, InputStream input
    ) throws AssetRepositoryException, FileSystemException {
        Assert.notNull(command, "Expected a non-null command");
        Assert.isTrue(command.getSize() > 0, "Expected file size to be greater than 0");

        this.saveResourceFile(command.getPublisherKey(), command.getDraftKey(), ADDITIONAL_RESOURCE_PATH, command.getFileName(), input);
    }

    private void saveResourceFile(
        UUID publisherKey, UUID draftKey, String path, String fileName, InputStream input
    ) throws AssetRepositoryException, FileSystemException {
        Assert.notNull(publisherKey, "Expected a non-null publisher key");
        Assert.notNull(draftKey, "Expected a non-null draft key");
        Assert.hasText(fileName, "Expected a non-empty file name");

        try {
            final DraftFileNamingStrategyContext ctx          = DraftFileNamingStrategyContext.of(publisherKey, draftKey);
            final Path                           relativePath = Paths.get(path, fileName);
            final Path                           absolutePath = this.draftNamingStrategy.resolvePath(ctx, relativePath);
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
            throw new AssetRepositoryException(AssetMessageCode.IO_ERROR, "An unknown error has occurred", ex);
        }
    }

    private void saveResourceFile(
        UUID publisherKey, UUID draftKey, String path, String fileName, byte[] data
    ) throws AssetRepositoryException, FileSystemException {
        Assert.notNull(publisherKey, "Expected a non-null publisher key");
        Assert.notNull(draftKey, "Expected a non-null draft key");
        Assert.hasText(path, "Expected a non-empty path");
        Assert.hasText(fileName, "Expected a non-empty file name");

        try {
            final DraftFileNamingStrategyContext ctx          = DraftFileNamingStrategyContext.of(publisherKey, draftKey);
            final Path                           relativePath = Paths.get(path, fileName);
            final Path                           absolutePath = this.draftNamingStrategy.resolvePath(ctx, relativePath);
            final File                           localFile    = absolutePath.toFile();

            // Create parent directory
            if (!absolutePath.getParent().toFile().exists()) {
                Files.createDirectories(absolutePath.getParent());
                Files.setPosixFilePermissions(absolutePath.getParent(), DEFAULT_DIRECTORY_PERMISSIONS);
            }

            if (localFile.exists()) {
                FileUtils.deleteQuietly(localFile);
            }

            Files.write(absolutePath, data, StandardOpenOption.CREATE_NEW);
        } catch (final FileSystemException ex) {
            throw ex;
        } catch (final Exception ex) {
            throw new AssetRepositoryException(AssetMessageCode.IO_ERROR, "An unknown error has occurred", ex);
        }
    }

    @Override
    public void setContract(
		ProviderUploadContractCommand command, byte[] data
    ) throws AssetRepositoryException, FileSystemException {
        Assert.notNull(command, "Expected a non-null command");
        Assert.isTrue(command.getSize() > 0, "Expected file size to be greater than 0");

        final UUID publisherKey = command.getPublisherKey();
        final UUID draftKey     = command.getDraftKey();

        // Delete contract (if one exists)
        try {
            final DraftFileNamingStrategyContext ctx          = DraftFileNamingStrategyContext.of(publisherKey, draftKey);
            final Path                           contractPath = this.draftNamingStrategy.resolvePath(ctx, CONTRACT_PATH);
            final File[]                         allPaths     = contractPath.toFile().listFiles();
            if (allPaths != null) {
                final List<File> files = Stream.of(allPaths)
                    .filter(f -> f.isFile())
                    .collect(Collectors.toList());

                Assert.isTrue(files.size() < 2, "Expected one or no files in the contract path");

                if (files.size() == 1) {
                    // Do not ignore the delete operation exception. If a contract
                    // file is replaced and deletion fails, getting the most recent
                    // contract may not be possible
                    FileUtils.delete(files.get(0));
            }
            }
        } catch (final IOException ex) {
            throw new FileSystemException(FileSystemMessageCode.IO_ERROR, "Failed to delete existing contract", ex);
        }

        this.saveResourceFile(command.getPublisherKey(), command.getDraftKey(), CONTRACT_PATH, command.getFileName(), data);
    }

    @Override
    public void addContractAnnex(
        AssetContractAnnexCommandDto command, byte[] data
    ) throws AssetRepositoryException, FileSystemException {
        Assert.notNull(command, "Expected a non-null command");
        Assert.isTrue(command.getSize() > 0, "Expected file size to be greater than 0");

        this.saveResourceFile(command.getPublisherKey(), command.getDraftKey(), CONTRACT_ANNEX_PATH, command.getFileName(), data);
    }

    @Override
    public void deleteResource(UUID publisherKey, UUID draftKey, String fileName) throws FileSystemException, AssetRepositoryException {
        this.deleteResource(publisherKey, draftKey, RESOURCE_PATH, fileName);
    }

    @Override
    public void deleteAdditionalResource(UUID publisherKey, UUID draftKey, String fileName) throws FileSystemException, AssetRepositoryException {
        this.deleteResource(publisherKey, draftKey, ADDITIONAL_RESOURCE_PATH, fileName);
    }

    @Override
    public void deleteContract(UUID publisherKey, UUID draftKey, String fileName) throws FileSystemException, AssetRepositoryException {
        this.deleteResource(publisherKey, draftKey, CONTRACT_PATH, fileName);
    }

    @Override
    public void deleteContractAnnex(UUID publisherKey, UUID draftKey, String fileName) throws FileSystemException, AssetRepositoryException {
        this.deleteResource(publisherKey, draftKey, CONTRACT_ANNEX_PATH, fileName);
    }

    private void deleteResource(
        UUID publisherKey, UUID draftKey, String path, String fileName
    ) throws FileSystemException, AssetRepositoryException {
        Assert.notNull(publisherKey, "Expected a non-null publisher key");
        Assert.notNull(draftKey, "Expected a non-null draft key");
        Assert.isTrue(!StringUtils.isBlank(fileName), "Expected a non-empty file name");

        try {
            final DraftFileNamingStrategyContext ctx          = DraftFileNamingStrategyContext.of(publisherKey, draftKey);
            final Path                           relativePath = Paths.get(path, fileName);
            final Path                           absolutePath = this.draftNamingStrategy.resolvePath(ctx, relativePath);
            final File                           file         = absolutePath.toFile();

            if (!file.exists()) {
                throw new FileSystemException(FileSystemMessageCode.PATH_NOT_FOUND, "Path does not exist");
            }

            if (!FileUtils.deleteQuietly(absolutePath.toFile())) {
                draftRepositoryLogger.error(String.format("Resource [%s] was not deleted", relativePath));
            }
        } catch (final FileSystemException ex) {
            throw ex;
        } catch (final Exception ex) {
            throw new AssetRepositoryException(AssetMessageCode.IO_ERROR, "An unknown error has occurred", ex);
        }
    }

    @Override
    public Path resolveResourcePath(UUID publisherKey, UUID draftKey, String fileName) throws FileSystemException, AssetRepositoryException {
        return this.resolveResourcePath(publisherKey, draftKey, RESOURCE_PATH, fileName);
    }

    @Override
    public Path resolveMetadataPropertyPath(
        UUID publisherKey, UUID draftKey, String fileName
    ) throws FileSystemException, AssetRepositoryException {
        Assert.notNull(publisherKey, "Expected a non-null publisher key");
        Assert.notNull(draftKey, "Expected a non-null draft key");
        Assert.isTrue(!StringUtils.isBlank(fileName), "Expected a non-empty file name");

        try {
        final DraftFileNamingStrategyContext ctx          = DraftFileNamingStrategyContext.of(publisherKey, draftKey);
        final Path                           relativePath = Paths.get(METADATA_PATH,  fileName);
        final Path                           absolutePath = this.draftNamingStrategy.resolvePath(ctx, relativePath);

        return absolutePath;
        } catch (final FileSystemException ex) {
            throw ex;
        } catch (final Exception ex) {
            logger.warn("Failed to resolve metadata path. [fileName={}, draftKey={}]", fileName, draftKey);

            throw new AssetRepositoryException(AssetMessageCode.IO_ERROR, "An unknown error has occurred", ex);
        }
    }

    @Override
    public Path resolveAdditionalResourcePath(UUID publisherKey, UUID draftKey, String fileName) throws FileSystemException, AssetRepositoryException {
        return this.resolveResourcePath(publisherKey, draftKey, ADDITIONAL_RESOURCE_PATH, fileName);
    }

    @Override
    public Path resolveContractPath(UUID publisherKey, UUID draftKey) throws FileSystemException, AssetRepositoryException {
    	Assert.notNull(publisherKey, "Expected a non-null publisher key");
        Assert.notNull(draftKey, "Expected a non-null draft key");

        try {
            final DraftFileNamingStrategyContext ctx          = DraftFileNamingStrategyContext.of(publisherKey, draftKey);
            final Path                           contractPath = this.draftNamingStrategy.resolvePath(ctx, CONTRACT_PATH);
            final File[]                         allPaths     = contractPath.toFile().listFiles();
            if (allPaths != null) {
                final Path absolutePath = Stream.of(allPaths)
                    .filter(f -> f.isFile())
                    .map(File::toPath)
                    .findFirst()
                    .orElse(null);

                return absolutePath;
            }
            return null;
        } catch (final FileSystemException ex) {
            throw ex;
        } catch (final Exception ex) {
            logger.warn("Failed to contract path. [publisherKey={}, draftKey={}]", publisherKey, draftKey);

            throw new AssetRepositoryException(AssetMessageCode.IO_ERROR, "An unknown error has occurred", ex);
        }
    }

    @Override
    public Path resolveContractAnnexPath(UUID publisherKey, UUID draftKey, String fileName) throws FileSystemException, AssetRepositoryException {
        return this.resolveResourcePath(publisherKey, draftKey, CONTRACT_ANNEX_PATH, fileName);
    }

    private Path resolveResourcePath(UUID publisherKey, UUID draftKey, String path, String fileName) throws FileSystemException, AssetRepositoryException {
        Assert.notNull(publisherKey, "Expected a non-null publisher key");
        Assert.notNull(draftKey, "Expected a non-null draft key");
        Assert.hasText(fileName, "Expected a non-empty file name");

        try {
            final DraftFileNamingStrategyContext ctx          = DraftFileNamingStrategyContext.of(publisherKey, draftKey);
            final Path                           relativePath = Paths.get(path, fileName);
            final Path                           absolutePath = this.draftNamingStrategy.resolvePath(ctx, relativePath);
            final File                           file         = absolutePath.toFile();

            if (!file.exists()) {
                throw new FileSystemException(FileSystemMessageCode.PATH_NOT_FOUND, "File does not exist");
            }

            return file.toPath();
        } catch (final FileSystemException ex) {
            throw ex;
        } catch (final Exception ex) {
            logger.warn("Failed to resolve metadata path. [fileName={}, draftKey={}]", fileName, draftKey);

            throw new AssetRepositoryException(AssetMessageCode.IO_ERROR, "An unknown error has occurred", ex);
        }
    }

    private Path prepareMetadataFile(
        UUID publisherKey, UUID draftKey, String fileName, String content
    ) throws FileSystemException, IOException {
        Assert.notNull(publisherKey, "Expected a non-null publisher key");
        Assert.notNull(draftKey, "Expected a non-null draft key");
        Assert.isTrue(!StringUtils.isBlank(fileName), "Expected a non-empty file name");
        Assert.isTrue(!StringUtils.isBlank(content), "Expected non-empty content");

        final DraftFileNamingStrategyContext ctx          = DraftFileNamingStrategyContext.of(publisherKey, draftKey);
        final Path                           relativePath = Paths.get(METADATA_PATH,  fileName);
        final Path                           absolutePath = this.draftNamingStrategy.resolvePath(ctx, relativePath);
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
    public void saveMetadataAsText(UUID publisherKey, UUID draftKey, String fileName, String content) throws AssetRepositoryException {
        try {
            final Path path = this.prepareMetadataFile(publisherKey, draftKey, fileName, content);

            FileUtils.writeStringToFile(path.toFile(), content, Charset.forName("UTF-8"));
        } catch (final FileSystemException ex) {
            throw ex;
        } catch (final Exception ex) {
            throw new AssetRepositoryException(AssetMessageCode.IO_ERROR, "An unknown error has occurred", ex);
        }
    }

    @Override
    public void saveMetadataPropertyAsImage(
        UUID publisherKey, UUID draftKey, String fileName, String content
    ) throws FileSystemException, AssetRepositoryException {
        try {
            final Path   path = this.prepareMetadataFile(publisherKey, draftKey, fileName, content);
            final byte[] data = Base64.getDecoder().decode(content);

            FileUtils.writeByteArrayToFile(path.toFile(), data);
        } catch (final FileSystemException ex) {
            throw ex;
        } catch (final Exception ex) {
            throw new AssetRepositoryException(AssetMessageCode.IO_ERROR, "An unknown error has occurred", ex);
        }
    }

    @Override
    public void saveMetadataPropertyAsJson(
        UUID publisherKey, UUID draftKey, String fileName, String content
    ) throws FileSystemException, AssetRepositoryException {
        try {
            final Path path = this.prepareMetadataFile(publisherKey, draftKey, fileName, content);

            FileUtils.writeStringToFile(path.toFile(), content, Charset.forName("UTF-8"));
        } catch (final FileSystemException ex) {
            throw ex;
        } catch (final Exception ex) {
            throw new AssetRepositoryException(AssetMessageCode.IO_ERROR, "An unknown error has occurred", ex);
        }
    }

    @Override
    public void deleteAllFiles(UUID publisherKey, UUID draftKey) {
        Assert.notNull(publisherKey, "Expected a non-null publisher key");
        Assert.notNull(draftKey, "Expected a non-null draft key");

        try {
            final DraftFileNamingStrategyContext ctx          = DraftFileNamingStrategyContext.of(publisherKey, draftKey);
            final Path                           absolutePath = this.draftNamingStrategy.getDir(ctx);

            if (!FileUtils.deleteQuietly(absolutePath.toFile())) {
                draftRepositoryLogger.error(String.format("Asset [%s]/[%s] file-system was not deleted", publisherKey, draftKey));
            }
        } catch (final Exception ex) {
            // Ignore
        }
    }

    @Override
    public void linkDraftFilesToAsset(UUID publisherKey, UUID draftKey, String pid) throws FileSystemException, AssetRepositoryException {
        try {
            final AssetFileNamingStrategyContext assetCtx = AssetFileNamingStrategyContext.of(pid);
            final Path                           link     = this.assetNamingStrategy.resolvePath(assetCtx, "/");

            if (link.toFile().exists()) {
                return;
            }

            final DraftFileNamingStrategyContext draftCtx = DraftFileNamingStrategyContext.of(publisherKey, draftKey);
            final Path                           target   = this.draftNamingStrategy.resolvePath(draftCtx, "/");

            Files.createSymbolicLink(link, target);
        } catch (final FileSystemException ex) {
            throw ex;
        } catch (final Exception ex) {
            throw new AssetRepositoryException(AssetMessageCode.IO_ERROR, "An unknown error has occurred", ex);
        }
    }

    @Override
    public void resetDraft(UUID publisherKey, UUID draftKey) throws FileSystemException, AssetRepositoryException {
        Assert.notNull(publisherKey, "Expected a non-null publisher key");
        Assert.notNull(draftKey, "Expected a non-null draft key");

        try {
            final DraftFileNamingStrategyContext ctx          = DraftFileNamingStrategyContext.of(publisherKey, draftKey);
            final Path                           absolutePath = this.draftNamingStrategy.resolvePath(ctx, METADATA_PATH);
            final File                           file         = absolutePath.toFile();

            if (file.exists() && !FileUtils.deleteQuietly(file)) {
                draftRepositoryLogger.error(String.format("Failed to delete metadata path for draft. [key=%s]", draftKey));
            }
        } catch (final FileSystemException ex) {
            throw ex;
        } catch (final Exception ex) {
            throw new AssetRepositoryException(AssetMessageCode.IO_ERROR, "An unknown error has occurred", ex);
        }
    }
}
