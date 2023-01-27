package eu.opertusmundi.common.service;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import eu.opertusmundi.common.model.asset.AssetAdditionalResourceCommandDto;
import eu.opertusmundi.common.model.asset.AssetContractAnnexCommandDto;
import eu.opertusmundi.common.model.asset.AssetRepositoryException;
import eu.opertusmundi.common.model.asset.FileResourceCommandDto;
import eu.opertusmundi.common.model.contract.provider.ProviderUploadContractCommand;
import eu.opertusmundi.common.model.file.FileDto;
import eu.opertusmundi.common.model.file.FileSystemException;

public interface DraftFileManager {

    List<FileDto> getResources(UUID publisherKey, UUID draftKey) throws FileSystemException, AssetRepositoryException;

    List<FileDto> getAdditionalResources(UUID publisherKey, UUID draftKey) throws FileSystemException, AssetRepositoryException;

    void addResource(
        FileResourceCommandDto command, InputStream input
    ) throws FileSystemException, AssetRepositoryException;

    void addAdditionalResource(
        AssetAdditionalResourceCommandDto command, InputStream input
    ) throws FileSystemException, AssetRepositoryException;

    void setContract(ProviderUploadContractCommand command, byte[] data) throws AssetRepositoryException, FileSystemException;

    void addContractAnnex(AssetContractAnnexCommandDto command, byte[] data) throws FileSystemException, AssetRepositoryException;

    void deleteResource(UUID publisherKey, UUID draftKey, String fileName) throws FileSystemException, AssetRepositoryException;

    void deleteAdditionalResource(UUID publisherKey, UUID draftKey, String fileName) throws FileSystemException, AssetRepositoryException;

    void deleteContract(UUID publisherKey, UUID draftKey, String fileName) throws FileSystemException, AssetRepositoryException;

    void deleteContractAnnex(UUID publisherKey, UUID draftKey, String fileName) throws FileSystemException, AssetRepositoryException;

    default Path resolveResourcePath(
        UUID publisherKey, UUID draftKey, String fileName
    ) throws FileSystemException, AssetRepositoryException {
        return this.resolveResourcePath(publisherKey, draftKey, fileName, false, true);
    }
    
    Path resolveResourcePath(
        UUID publisherKey, UUID draftKey, String fileName, boolean withIprProtection, boolean throwIfNotExists
    ) throws FileSystemException, AssetRepositoryException;

    Path resolveAdditionalResourcePath(UUID publisherKey, UUID draftKey, String fileName) throws FileSystemException, AssetRepositoryException;

    Path resolveContractPath(UUID publisherKey, UUID draftKey) throws FileSystemException, AssetRepositoryException;

    Path resolveContractAnnexPath(UUID publisherKey, UUID draftKey, String fileName) throws FileSystemException, AssetRepositoryException;

    Path resolveMetadataPropertyPath(UUID publisherKey, UUID draftKey, String fileName) throws FileSystemException, AssetRepositoryException;

    void saveMetadataAsText(UUID publisherKey, UUID draftKey, String fileName, String content) throws AssetRepositoryException;

    void saveMetadataPropertyAsImage(UUID publisherKey, UUID draftKey, String fileName, String content) throws FileSystemException, AssetRepositoryException;

    void saveMetadataPropertyAsJson(UUID publisherKey, UUID draftKey, String fileName, String content) throws FileSystemException, AssetRepositoryException;

    void deleteAllFiles(UUID publisherKey, UUID draftKey);

    void linkDraftFilesToAsset(UUID publisherKey, UUID draftKey, String pid) throws FileSystemException, AssetRepositoryException;

    /**
     * Reset draft files
     *
     * @param publisherKey
     * @param draftKey
     * @throws FileSystemException
     * @throws AssetRepositoryException
     */
    void resetDraft(UUID publisherKey, UUID draftKey) throws FileSystemException, AssetRepositoryException;
}
