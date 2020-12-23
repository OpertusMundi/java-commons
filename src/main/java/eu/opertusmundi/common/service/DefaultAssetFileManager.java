package eu.opertusmundi.common.service;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.opertusmundi.common.model.AssetFileNamingStrategyContext;
import eu.opertusmundi.common.model.FileSystemMessageCode;
import eu.opertusmundi.common.model.asset.AssetMessageCode;
import eu.opertusmundi.common.model.asset.AssetRepositoryException;
import eu.opertusmundi.common.model.file.FileDto;
import eu.opertusmundi.common.model.file.FileSystemException;

@Service
public class DefaultAssetFileManager implements AssetFileManager {

    private static final Logger logger = LoggerFactory.getLogger(DefaultAssetFileManager.class);

    @Autowired
    private DefaultAssetFileNamingStrategy fileNamingStrategy;

    @Override
    public List<FileDto> getFiles(UUID key) throws FileSystemException, AssetRepositoryException {
        try {
            if (key == null) {
                return new ArrayList<FileDto>();
            }

            final AssetFileNamingStrategyContext ctx     = AssetFileNamingStrategyContext.of(key);
            final Path                           userDir = this.fileNamingStrategy.getDir(ctx);
            final Path                           target  = Paths.get(userDir.toString());

            final List<FileDto> result = new ArrayList<FileDto>();
            final File          dir    = target.toFile();

            if (dir.exists()) {
                for (final File entry : dir.listFiles()) {
                    if (entry.isFile()) {
                        result.add(new FileDto(entry.getName(), "/" + entry.getName().toString(), entry.length(), entry.lastModified()));
                    }
                }
            }

            return result;
        } catch (final FileSystemException ex) {
            throw ex;
        } catch (final Exception ex) {
            logger.error("[FileSystem] Failed to load files", ex);

            throw new AssetRepositoryException("An unknown error has occurred");
        }
    }

    @Override
    public void uploadFile(UUID key, String fileName, InputStream input) throws AssetRepositoryException, FileSystemException {
        try {
            final AssetFileNamingStrategyContext ctx          = AssetFileNamingStrategyContext.of(key);
            final Path                           relativePath = Paths.get("/", fileName);
            final Path                           absolutePath = this.fileNamingStrategy.resolvePath(ctx, relativePath);
            final File                           localFile    = absolutePath.toFile();

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
    public void deletePath(UUID key, String path) throws FileSystemException, AssetRepositoryException {
        try {
            final AssetFileNamingStrategyContext ctx          = AssetFileNamingStrategyContext.of(key);
            final Path                           absolutePath = this.fileNamingStrategy.resolvePath(ctx, path);
            final File                           file         = absolutePath.toFile();

            if (!file.exists()) {
                throw new FileSystemException(FileSystemMessageCode.PATH_NOT_FOUND, "Path does not exist");
            }

            Files.delete(absolutePath);
        } catch (final FileSystemException ex) {
            throw ex;
        } catch (final Exception ex) {
            throw new AssetRepositoryException(AssetMessageCode.IO_ERROR, "An unknown error has occurred");
        }
    }

    @Override
    public Path resolveFilePath(UUID key, String fileName) throws FileSystemException, AssetRepositoryException {
        try {
            final AssetFileNamingStrategyContext ctx          = AssetFileNamingStrategyContext.of(key);
            final Path                           assetDir     = this.fileNamingStrategy.getDir(ctx);
            final Path                           absolutePath = Paths.get(assetDir.toString(), fileName);
            final File                           file         = absolutePath.toFile();

            if (!file.exists()) {
                throw new FileSystemException(FileSystemMessageCode.PATH_NOT_FOUND, "File does not exist");
            }

            return file.toPath();
        } catch (final FileSystemException ex) {
            throw ex;
        } catch (final Exception ex) {
            logger.warn("[FileSystem] Failed to resolve path [/{}] for asset [{}]", fileName, key);

            throw new AssetRepositoryException(AssetMessageCode.IO_ERROR, "An unknown error has occurred");
        }
    }

}
