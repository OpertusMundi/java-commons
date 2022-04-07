package eu.opertusmundi.common.service;

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
import eu.opertusmundi.common.model.file.DirectoryDto;
import eu.opertusmundi.common.model.file.FileDto;
import eu.opertusmundi.common.model.file.FileSystemException;
import eu.opertusmundi.common.model.file.FileSystemMessageCode;

@Service
public class DefaultAssetFileManager implements AssetFileManager {

    private static final Logger logger = LoggerFactory.getLogger(DefaultAssetFileManager.class);

    private static final Logger assetRepositoryLogger = LoggerFactory.getLogger("ASSET_REPOSITORY");

    private static final String RESOURCE_PATH = "/resources";

    private static final String ADDITIONAL_RESOURCE_PATH = "/additional-resources";

    private static final String CONTRACT_PATH = "/contract";
    
    private static final String CONTRACT_ANNEX_PATH = "/contract/annexes";

    private static final String METADATA_PATH = "/metadata";
    

    @Autowired
    private DefaultAssetFileNamingStrategy fileNamingStrategy;

    @Autowired
    private DirectoryTraverse directoryTraverse;


    @Override
    public Path resolveResourcePath(String pid, String fileName) throws FileSystemException, AssetRepositoryException {
        return this.resolveResourcePath(pid, RESOURCE_PATH, fileName);
    }
    
    @Override
    public Path resolveUploadedContractPath(String pid) throws FileSystemException, AssetRepositoryException {
    	final AssetFileNamingStrategyContext ctx	=	AssetFileNamingStrategyContext.of(pid);
        Path contractPath							=	null;
        try {
            Path absolutePath 						=	this.fileNamingStrategy.resolvePath(ctx, Paths.get(CONTRACT_PATH));
			DirectoryDto dir 						=	this.directoryTraverse.getDirectoryInfo(absolutePath);

	        contractPath 							=	absolutePath.resolve(dir.getFiles().get(0).getName());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return contractPath;
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
        return this.resolveResourcePath(pid, ADDITIONAL_RESOURCE_PATH, fileName);
    }
    
    @Override
    public Path resolveContractAnnexPath(String pid, String fileName) throws FileSystemException, AssetRepositoryException {
        return this.resolveResourcePath(pid, CONTRACT_ANNEX_PATH, fileName);
    }

    private Path resolveResourcePath(String pid, String path, String fileName) throws FileSystemException, AssetRepositoryException {
        Assert.isTrue(!StringUtils.isBlank(pid), "Expected a non-empty pid");
        Assert.isTrue(!StringUtils.isBlank(fileName), "Expected a non-empty file name");

        try {
            final AssetFileNamingStrategyContext ctx          = AssetFileNamingStrategyContext.of(pid);
            final Path                           relativePath = Paths.get(path, fileName);
            final Path                           absolutePath = this.fileNamingStrategy.resolvePath(ctx, relativePath);
            final File                           file         = absolutePath.toFile();

            if (!file.exists()) {
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
