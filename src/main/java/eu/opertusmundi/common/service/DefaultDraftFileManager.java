package eu.opertusmundi.common.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import eu.opertusmundi.common.model.AssetFileNamingStrategyContext;
import eu.opertusmundi.common.model.DraftFileNamingStrategyContext;
import eu.opertusmundi.common.model.FileSystemMessageCode;
import eu.opertusmundi.common.model.asset.AssetFileAdditionalResourceCommandDto;
import eu.opertusmundi.common.model.asset.AssetMessageCode;
import eu.opertusmundi.common.model.asset.AssetRepositoryException;
import eu.opertusmundi.common.model.asset.AssetResourceCommandDto;
import eu.opertusmundi.common.model.file.FileDto;
import eu.opertusmundi.common.model.file.FileSystemException;

@Service
public class DefaultDraftFileManager implements DraftFileManager {

    private static final Logger logger = LoggerFactory.getLogger(DefaultDraftFileManager.class);
    
    private static final Logger draftRepositoryLogger = LoggerFactory.getLogger("DRAFT_REPOSITORY");

    private static final String RESOURCE_PATH = "/resources";
    
    private static final String ADDITIONAL_RESOURCE_PATH = "/additional-resources";
    
    private static final String METADATA_PATH = "/metadata";
    
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
                String.format("[FileSystem] Failed to load files from path [%s]/[%s]/[%s]", publisherKey, draftKey, relativePath), ex
            );

            throw new AssetRepositoryException("An unknown error has occurred");
        }
    }

    @Override
    public void uploadResource(
        AssetResourceCommandDto command, InputStream input
    ) throws AssetRepositoryException, FileSystemException {
        Assert.notNull(command, "Expected a non-null command");
        Assert.isTrue(command.getSize() > 0, "Expected file size to be greater than 0");
        
        this.saveResourceFile(command.getPublisherKey(), command.getDraftKey(), RESOURCE_PATH, command.getFileName(), input);
    }

    @Override
    public void uploadAdditionalResource(
        AssetFileAdditionalResourceCommandDto command, InputStream input
    ) throws AssetRepositoryException, FileSystemException {
        Assert.notNull(command, "Expected a non-null command");
        Assert.isTrue(command.getSize() > 0, "Expected file size to be greater than 0");
        
        this.saveResourceFile(command.getPublisherKey(), command.getDraftKey(), ADDITIONAL_RESOURCE_PATH, command.getFileName(), input);
    }
    
    private void saveResourceFile(
        UUID publisherKey, UUID draftKey, String path, String fileName, InputStream input
    ) throws AssetRepositoryException, FileSystemException {
        Assert.notNull(publisherKey, "Exepcted a non-null publisher key");
        Assert.notNull(draftKey, "Exepcted a non-null draft key");
        Assert.isTrue(!StringUtils.isBlank(fileName), "Expected a non-empty file name");
        
        try {
            final DraftFileNamingStrategyContext ctx          = DraftFileNamingStrategyContext.of(publisherKey, draftKey);
            final Path                           relativePath = Paths.get(path, fileName);
            final Path                           absolutePath = this.draftNamingStrategy.resolvePath(ctx, relativePath);
            final File                           localFile    = absolutePath.toFile();

            // Create parent directory
            if (!absolutePath.getParent().toFile().exists()) {
                FileUtils.forceMkdir(absolutePath.getParent().toFile());
            }
            
            if (localFile.exists()) {
                FileUtils.deleteQuietly(localFile);
            }

            Files.copy(input, absolutePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (final FileSystemException ex) {
            throw ex;
        } catch (final Exception ex) {
            throw new AssetRepositoryException(AssetMessageCode.IO_ERROR, "An unknown error has occurred");
        }
    }

    @Override
    public void deleteResource(UUID publisherKey, UUID draftKey, String fileName) throws FileSystemException, AssetRepositoryException {
        this.deleteResource(publisherKey, draftKey, RESOURCE_PATH, fileName);
    }

    @Override
    public void deleteAdditionalResource(UUID publisherKey, UUID draftKey, String fileName) throws FileSystemException, AssetRepositoryException {
        this.deleteResource(publisherKey, draftKey, ADDITIONAL_RESOURCE_PATH, fileName);
    }

    private void deleteResource(
        UUID publisherKey, UUID draftKey, String path, String fileName
    ) throws FileSystemException, AssetRepositoryException {
        Assert.notNull(publisherKey, "Exepcted a non-null publisher key");
        Assert.notNull(draftKey, "Exepcted a non-null draft key");
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
            throw new AssetRepositoryException(AssetMessageCode.IO_ERROR, "An unknown error has occurred");
        }
    }

    @Override
    public Path resolveResourcePath(UUID publisherKey, UUID draftKey, String fileName) throws FileSystemException, AssetRepositoryException {       
        return this.resolveResourcePath(publisherKey, draftKey, RESOURCE_PATH, fileName);
    }
    
    public Path resolveMetadataPropertyPath(
        UUID publisherKey, UUID draftKey, String fileName
    ) throws FileSystemException, AssetRepositoryException {
        Assert.notNull(publisherKey, "Exepcted a non-null publisher key");
        Assert.notNull(draftKey, "Exepcted a non-null draft key");
        Assert.isTrue(!StringUtils.isBlank(fileName), "Expected a non-empty file name");
        
        try {
        final DraftFileNamingStrategyContext ctx          = DraftFileNamingStrategyContext.of(publisherKey, draftKey);
        final Path                           relativePath = Paths.get(METADATA_PATH,  fileName);
        final Path                           absolutePath = this.draftNamingStrategy.resolvePath(ctx, relativePath);
        
        return absolutePath;
        } catch (final FileSystemException ex) {
            throw ex;
        } catch (final Exception ex) {
            logger.warn("[FileSystem] Failed to resolve metadata path [{}] for asset [{}]", fileName, draftKey);

            throw new AssetRepositoryException(AssetMessageCode.IO_ERROR, "An unknown error has occurred");
        }
    }

    @Override
    public Path resolveAdditionalResourcePath(UUID publisherKey, UUID draftKey, String fileName) throws FileSystemException, AssetRepositoryException {       
        return this.resolveResourcePath(publisherKey, draftKey, ADDITIONAL_RESOURCE_PATH, fileName);
    }

    private Path resolveResourcePath(UUID publisherKey, UUID draftKey, String path, String fileName) throws FileSystemException, AssetRepositoryException {
        Assert.notNull(publisherKey, "Exepcted a non-null publisher key");
        Assert.notNull(draftKey, "Exepcted a non-null draft key");
        Assert.isTrue(!StringUtils.isBlank(fileName), "Expected a non-empty file name");
        
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
            logger.warn("[FileSystem] Failed to resolve path [{}] for asset [{}]", fileName, draftKey);

            throw new AssetRepositoryException(AssetMessageCode.IO_ERROR, "An unknown error has occurred");
        }
    }
    
    private File prepareMetadataFile(
        UUID publisherKey, UUID draftKey, String fileName, String content
    ) throws FileSystemException, IOException {
        Assert.notNull(publisherKey, "Exepcted a non-null publisher key");
        Assert.notNull(draftKey, "Exepcted a non-null draft key");
        Assert.isTrue(!StringUtils.isBlank(fileName), "Expected a non-empty file name");
        Assert.isTrue(!StringUtils.isBlank(content), "Expected non-empty content");
        
        final DraftFileNamingStrategyContext ctx          = DraftFileNamingStrategyContext.of(publisherKey, draftKey);
        final Path                           relativePath = Paths.get(METADATA_PATH,  fileName);
        final Path                           absolutePath = this.draftNamingStrategy.resolvePath(ctx, relativePath);
        final File                           localFile    = absolutePath.toFile();
        
        // Create parent directory
        if (!absolutePath.getParent().toFile().exists()) {
            FileUtils.forceMkdir(absolutePath.getParent().toFile());
        }
        
        if (localFile.exists()) {
            FileUtils.deleteQuietly(localFile);
        }
        
        return localFile;
    }
    
    @Override
    public void saveMetadataAsText(UUID publisherKey, UUID draftKey, String fileName, String content) throws AssetRepositoryException {
        try {
            final File file = this.prepareMetadataFile(publisherKey, draftKey, fileName, content);

            FileUtils.writeStringToFile(file, content, Charset.forName("UTF-8"));
        } catch (final FileSystemException ex) {
            throw ex;
        } catch (final Exception ex) {
            throw new AssetRepositoryException(AssetMessageCode.IO_ERROR, "An unknown error has occurred");
        }
    }

    public void saveMetadataPropertyAsImage(
        UUID publisherKey, UUID draftKey, String fileName, String content
    ) throws FileSystemException, AssetRepositoryException {
        try {
            final File   file = this.prepareMetadataFile(publisherKey, draftKey, fileName, content);
            final byte[] data = Base64.getDecoder().decode(content);
            
            FileUtils.writeByteArrayToFile(file, data);
        } catch (final FileSystemException ex) {
            throw ex;
        } catch (final Exception ex) {
            throw new AssetRepositoryException(AssetMessageCode.IO_ERROR, "An unknown error has occurred");
        }
    }

    public void saveMetadataPropertyAsJson(
        UUID publisherKey, UUID draftKey, String fileName, String content
    ) throws FileSystemException, AssetRepositoryException {
        try {
            final File file = this.prepareMetadataFile(publisherKey, draftKey, fileName, content);

            FileUtils.writeStringToFile(file, content, Charset.forName("UTF-8"));
        } catch (final FileSystemException ex) {
            throw ex;
        } catch (final Exception ex) {
            throw new AssetRepositoryException(AssetMessageCode.IO_ERROR, "An unknown error has occurred");
        }
    }

    public void deleteAllFiles(UUID publisherKey, UUID draftKey) {
        Assert.notNull(publisherKey, "Exepcted a non-null publisher key");
        Assert.notNull(draftKey, "Exepcted a non-null draft key");
        
        try {
            final DraftFileNamingStrategyContext ctx          = DraftFileNamingStrategyContext.of(publisherKey, draftKey);
            final Path                           absolutePath = this.draftNamingStrategy.getDir(ctx);

            if (!FileUtils.deleteQuietly(absolutePath.toFile())) {
                draftRepositoryLogger.error(String.format("Asset [%s]/[%s] file-system was not deleted", publisherKey, draftKey));
            }
        } catch (Exception ex) {
            // Ignore
        }
    }
    
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
            throw new AssetRepositoryException(AssetMessageCode.IO_ERROR, "An unknown error has occurred");
        }
    }

}
