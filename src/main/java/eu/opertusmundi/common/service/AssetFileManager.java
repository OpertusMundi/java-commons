package eu.opertusmundi.common.service;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import eu.opertusmundi.common.model.asset.AssetRepositoryException;
import eu.opertusmundi.common.model.asset.AssetResourceDto;
import eu.opertusmundi.common.model.file.FileSystemException;

public interface AssetFileManager {

    List<AssetResourceDto> getResources(UUID key) throws FileSystemException, AssetRepositoryException;

    void uploadResource(UUID key, String fileName, InputStream input) throws FileSystemException, AssetRepositoryException;

    void deleteResource(UUID key, String fileName) throws FileSystemException, AssetRepositoryException;

    Path resolveResourcePath(UUID key, String fileName) throws FileSystemException, AssetRepositoryException;
    
	void saveMetadataAsText(UUID key, String fileName, String content) throws AssetRepositoryException; 

}
