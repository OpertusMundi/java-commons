package eu.opertusmundi.common.service;

import static eu.opertusmundi.common.model.asset.AssetResourcePaths.ADDITIONAL_RESOURCE_PATH;
import static eu.opertusmundi.common.model.asset.AssetResourcePaths.CONTRACT_ANNEX_PATH;
import static eu.opertusmundi.common.model.asset.AssetResourcePaths.CONTRACT_PATH;
import static eu.opertusmundi.common.model.asset.AssetResourcePaths.IPR_RESOURCE_PATH;
import static eu.opertusmundi.common.model.asset.AssetResourcePaths.METADATA_PATH;
import static eu.opertusmundi.common.model.asset.AssetResourcePaths.RESOURCE_PATH;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import eu.opertusmundi.common.model.asset.AssetFileNamingStrategyContext;
import eu.opertusmundi.common.model.asset.AssetMessageCode;
import eu.opertusmundi.common.model.asset.AssetRepositoryException;
import eu.opertusmundi.common.model.file.FileDto;
import eu.opertusmundi.common.model.file.FileSystemException;
import eu.opertusmundi.common.model.file.FileSystemMessageCode;

@Service
public class DefaultAssetFileManager implements AssetFileManager {

    private static final Logger logger = LoggerFactory.getLogger(DefaultAssetFileManager.class);

    private static final Logger assetRepositoryLogger = LoggerFactory.getLogger("ASSET_REPOSITORY");

    @Autowired
    private DefaultAssetFileNamingStrategy fileNamingStrategy;

    @Override
    public Path resolveResourcePath(String pid, String fileName) throws FileSystemException, AssetRepositoryException {
        // Always check if an IPR protected resource exists
        final var protectedResource = this.resolveResourcePath(pid, IPR_RESOURCE_PATH, fileName, false);
        if (protectedResource.toFile().exists()) {
            return protectedResource;
        }

        return this.resolveResourcePath(pid, RESOURCE_PATH, fileName, true);
    }

    @Override
    public Path resolveContractPath(String pid) throws FileSystemException, AssetRepositoryException {
        Assert.hasText(pid, "Expected a non-empty pid");

        final AssetFileNamingStrategyContext ctx = AssetFileNamingStrategyContext.of(pid);
        try {
            final Path absolutePath = this.fileNamingStrategy.resolvePath(ctx, Paths.get(CONTRACT_PATH));
            final File dir          = absolutePath.toFile();

            if (dir.exists()) {
                for (final File entry : dir.listFiles()) {
                    // Ignore any folders
                    if (entry.isFile()) {
                        return entry.toPath();
                    }
                }
            }
        } catch (final IOException ex) {
            logger.error("Failed to find uploaded contract. [pid={}, relativePath={}, message={}]", pid, CONTRACT_PATH, ex.getMessage());

            throw new AssetRepositoryException("An unknown error has occurred", ex);
        }
        return null;
    }

    @Override
    public List<FileDto> getAdditionalResources(String pid) throws FileSystemException, AssetRepositoryException {
        return this.getResources(pid, ADDITIONAL_RESOURCE_PATH);
    }

    private List<FileDto> getResources(String pid, String relativePath) throws FileSystemException, AssetRepositoryException {
        Assert.isTrue(!StringUtils.isBlank(pid), "Expected a non-empty pid");
        Assert.isTrue(!StringUtils.isBlank(relativePath), "Expected a non-empty relative path");

        try {
            final AssetFileNamingStrategyContext ctx     = AssetFileNamingStrategyContext.of(pid);
            final Path                           userDir = this.fileNamingStrategy.getDir(ctx);
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
            logger.error("Failed to load resource files. [pid={}, relativePath={}, message={}]", pid, relativePath, ex.getMessage());

            throw new AssetRepositoryException("An unknown error has occurred", ex);
        }
    }

    @Override
    public Path resolveMetadataPropertyPath(
        String pid, String fileName
    ) throws FileSystemException, AssetRepositoryException {
        Assert.isTrue(!StringUtils.isBlank(pid), "Expected a non-empty pid");
        Assert.isTrue(!StringUtils.isBlank(fileName), "Expected a non-empty file name");

        try {
        final AssetFileNamingStrategyContext ctx          = AssetFileNamingStrategyContext.of(pid);
        final Path                           relativePath = Paths.get(METADATA_PATH,  fileName);
        final Path                           absolutePath = this.fileNamingStrategy.resolvePath(ctx, relativePath);

        return absolutePath;
        } catch (final FileSystemException ex) {
            throw ex;
        } catch (final Exception ex) {
            assetRepositoryLogger.warn("[FileSystem] Failed to resolve metadata path [{}] for asset [{}]", fileName, pid);

            throw new AssetRepositoryException(AssetMessageCode.IO_ERROR, "An unknown error has occurred", ex);
        }
    }

    @Override
    public Path resolveAdditionalResourcePath(String pid, String fileName) throws FileSystemException, AssetRepositoryException {
        return this.resolveResourcePath(pid, ADDITIONAL_RESOURCE_PATH, fileName, true);
    }

    @Override
    public Path resolveContractAnnexPath(String pid, String fileName) throws FileSystemException, AssetRepositoryException {
        return this.resolveResourcePath(pid, CONTRACT_ANNEX_PATH, fileName, true);
    }

    private Path resolveResourcePath(
        String pid, String path, String fileName, boolean throwIfNotExists
    ) throws FileSystemException, AssetRepositoryException {
        Assert.isTrue(!StringUtils.isBlank(pid), "Expected a non-empty pid");
        Assert.isTrue(!StringUtils.isBlank(fileName), "Expected a non-empty file name");

        try {
            final AssetFileNamingStrategyContext ctx          = AssetFileNamingStrategyContext.of(pid);
            final Path                           relativePath = Paths.get(path, fileName);
            final Path                           absolutePath = this.fileNamingStrategy.resolvePath(ctx, relativePath);
            final File                           file         = absolutePath.toFile();

            if (throwIfNotExists && !file.exists()) {
                throw new FileSystemException(FileSystemMessageCode.PATH_NOT_FOUND, "File does not exist");
            }

            return file.toPath();
        } catch (final FileSystemException ex) {
            throw ex;
        } catch (final Exception ex) {
            assetRepositoryLogger.warn("[FileSystem] Failed to resolve path [{}] for asset [{}]", fileName, pid);

            throw new AssetRepositoryException(AssetMessageCode.IO_ERROR, "An unknown error has occurred", ex);
        }
    }

}
