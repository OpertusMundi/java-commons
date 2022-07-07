package eu.opertusmundi.common.service;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import eu.opertusmundi.common.model.asset.service.UserServiceCommandDto;
import eu.opertusmundi.common.model.file.FileDto;
import eu.opertusmundi.common.model.file.FileSystemException;

public interface UserServiceFileManager {

    List<FileDto> getResources(UUID ownerKey, UUID serviceKey) throws FileSystemException, UserServiceException;

    void uploadResource(UserServiceCommandDto command, InputStream input) throws FileSystemException, UserServiceException;

    void deleteResource(UUID ownerKey, UUID serviceKey, String fileName) throws FileSystemException, UserServiceException;

    Path resolveResourcePath(UUID ownerKey, UUID serviceKey, String fileName) throws FileSystemException, UserServiceException;

    Path resolveMetadataPropertyPath(UUID ownerKey, UUID serviceKey, String fileName) throws FileSystemException, UserServiceException;

    void saveMetadataAsText(UUID ownerKey, UUID serviceKey, String fileName, String content) throws UserServiceException;

    void saveMetadataPropertyAsImage(UUID ownerKey, UUID serviceKey, String fileName, String content) throws FileSystemException, UserServiceException;

    void saveMetadataPropertyAsJson(UUID ownerKey, UUID serviceKey, String fileName, String content) throws FileSystemException, UserServiceException;

    void deleteAllFiles(UUID ownerKey, UUID serviceKey);

    /**
     * Reset service files
     *
     * @param ownerKey
     * @param serviceKey
     * @throws FileSystemException
     * @throws UserServiceException
     */
    void reset(UUID ownerKey, UUID serviceKey) throws FileSystemException, UserServiceException;
}
