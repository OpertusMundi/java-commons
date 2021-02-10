package eu.opertusmundi.common.service;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import eu.opertusmundi.common.model.asset.AssetFileAdditionalResourceCommandDto;
import eu.opertusmundi.common.model.asset.AssetRepositoryException;
import eu.opertusmundi.common.model.asset.AssetResourceCommandDto;
import eu.opertusmundi.common.model.file.FileDto;
import eu.opertusmundi.common.model.file.FileSystemException;

public interface AssetFileManager {

    List<FileDto> getResources(UUID publisherKey, UUID draftKey) throws FileSystemException, AssetRepositoryException;

    List<FileDto> getAdditionalResources(UUID publisherKey, UUID draftKey) throws FileSystemException, AssetRepositoryException;

    void uploadResource(
        AssetResourceCommandDto command, InputStream input
    ) throws FileSystemException, AssetRepositoryException;

    void uploadAdditionalResource(
        AssetFileAdditionalResourceCommandDto command, InputStream input
    ) throws FileSystemException, AssetRepositoryException;

    void deleteResource(UUID publisherKey, UUID draftKey, String fileName) throws FileSystemException, AssetRepositoryException;

    void deleteAdditionalResource(UUID publisherKey, UUID draftKey, String fileName) throws FileSystemException, AssetRepositoryException;

    Path resolveResourcePath(UUID publisherKey, UUID draftKey, String fileName) throws FileSystemException, AssetRepositoryException;

    Path resolveAdditionalResourcePath(UUID publisherKey, UUID draftKey, String fileName) throws FileSystemException, AssetRepositoryException;

    void saveMetadataAsText(UUID publisherKey, UUID draftKey, String fileName, String content) throws AssetRepositoryException;

    void deleteAllFiles(UUID publisherKey, UUID draftKey);

}
