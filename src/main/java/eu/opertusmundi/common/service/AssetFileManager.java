package eu.opertusmundi.common.service;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import eu.opertusmundi.common.model.asset.AssetRepositoryException;
import eu.opertusmundi.common.model.file.FileDto;
import eu.opertusmundi.common.model.file.FileSystemException;

public interface AssetFileManager {

    List<FileDto> getFiles(UUID key) throws FileSystemException, AssetRepositoryException;

    void uploadFile(UUID key, String fileName, InputStream input) throws FileSystemException, AssetRepositoryException;

    void deletePath(UUID key, String fileName) throws FileSystemException, AssetRepositoryException;

    Path resolveFilePath(UUID key, String fileName) throws FileSystemException, AssetRepositoryException;

}
