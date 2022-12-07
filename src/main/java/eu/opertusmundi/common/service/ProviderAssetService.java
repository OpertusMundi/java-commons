package eu.opertusmundi.common.service;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;

import eu.opertusmundi.common.model.EnumSortingOrder;
import eu.opertusmundi.common.model.Message;
import eu.opertusmundi.common.model.PageResultDto;
import eu.opertusmundi.common.model.asset.AssetAdditionalResourceCommandDto;
import eu.opertusmundi.common.model.asset.AssetContractAnnexCommandDto;
import eu.opertusmundi.common.model.asset.AssetDraftDto;
import eu.opertusmundi.common.model.asset.AssetDraftReviewCommandDto;
import eu.opertusmundi.common.model.asset.AssetDraftSetStatusCommandDto;
import eu.opertusmundi.common.model.asset.AssetRepositoryException;
import eu.opertusmundi.common.model.asset.EnumProviderAssetDraftSortField;
import eu.opertusmundi.common.model.asset.EnumProviderAssetDraftStatus;
import eu.opertusmundi.common.model.asset.EnumProviderSubSortField;
import eu.opertusmundi.common.model.asset.ExternalUrlFileResourceCommandDto;
import eu.opertusmundi.common.model.asset.FileResourceCommandDto;
import eu.opertusmundi.common.model.asset.MetadataProperty;
import eu.opertusmundi.common.model.asset.ResourceDto;
import eu.opertusmundi.common.model.asset.UserFileResourceCommandDto;
import eu.opertusmundi.common.model.catalogue.CatalogueServiceException;
import eu.opertusmundi.common.model.catalogue.client.CatalogueHarvestImportCommandDto;
import eu.opertusmundi.common.model.catalogue.client.CatalogueItemCommandDto;
import eu.opertusmundi.common.model.catalogue.client.CatalogueItemMetadataCommandDto;
import eu.opertusmundi.common.model.catalogue.client.DraftApiCommandDto;
import eu.opertusmundi.common.model.catalogue.client.DraftFromAssetCommandDto;
import eu.opertusmundi.common.model.catalogue.client.EnumAssetType;
import eu.opertusmundi.common.model.catalogue.client.EnumDraftStatus;
import eu.opertusmundi.common.model.catalogue.client.EnumSpatialDataServiceType;
import eu.opertusmundi.common.model.catalogue.client.UnpublishAssetCommand;
import eu.opertusmundi.common.model.contract.provider.ProviderUploadContractCommand;
import eu.opertusmundi.common.model.file.FileSystemException;
import eu.opertusmundi.common.model.ingest.ResourceIngestionDataDto;
import eu.opertusmundi.common.model.ingest.ServerIngestPublishResponseDto;
import eu.opertusmundi.common.model.ingest.ServerIngestResultResponseDto;
import eu.opertusmundi.common.model.payment.provider.ProviderAccountSubscriptionDto;

public interface ProviderAssetService {

    /**
     * Search drafts
     *
     * @param ownerKey
     * @param publisherKey
     * @param status
     * @param type
     * @param serviceType
     * @param pageIndex
     * @param pageSize
     * @param orderBy
     * @param order
     * @return
     */
    PageResultDto<AssetDraftDto> findAllDraft(
        UUID ownerKey, UUID publisherKey,
        Set<EnumProviderAssetDraftStatus> status, Set<EnumAssetType> type, Set<EnumSpatialDataServiceType> serviceType,
        int pageIndex, int pageSize,
        EnumProviderAssetDraftSortField orderBy, EnumSortingOrder order
    );

    default PageResultDto<AssetDraftDto> findAllDraft(
        UUID ownerKey, UUID publisherKey,
        Set<EnumProviderAssetDraftStatus> status, Set<EnumAssetType> type, Set<EnumSpatialDataServiceType> serviceType,
        int pageIndex, int pageSize
    ) {
        return this.findAllDraft(
            ownerKey, publisherKey, status, type, serviceType, pageIndex, pageSize,
            EnumProviderAssetDraftSortField.MODIFIED_ON, EnumSortingOrder.DESC
        );
    }

    /**
     * Get one draft by key
     *
     * @param ownerKey
     * @param publisherKey
     * @param draftKey
     * @param boolean locked
     * @return
     */
    AssetDraftDto findOneDraft(UUID ownerKey, UUID publisherKey, UUID draftKey, boolean locked);

    default AssetDraftDto findOneDraft(UUID publisherKey, UUID draftKey, boolean locked) {
        return this.findOneDraft(publisherKey, publisherKey, draftKey, locked);
    }

    /**
     * Get one draft by key
     *
     * @param draftKey
     * @return
     */
    AssetDraftDto findOneDraft(UUID draftKey);

    /**
     * Create API draft
     *
     * @param command
     * @return
     * @throws AssetDraftException
     */
    AssetDraftDto createApiDraft(DraftApiCommandDto command) throws AssetDraftException;

    /**
     * Create a new draft from an existing asset
     *
     * @param command
     * @return
     * @throws AssetDraftException
     */
    AssetDraftDto createDraftFromAsset(DraftFromAssetCommandDto command) throws AssetDraftException;

    /**
     * Create one or more drafts by importing records from a harvested catalogue
     *
     * @param command
     * @throws AssetDraftException
     */
    Map<String, AssetDraftDto> importFromCatalogue(CatalogueHarvestImportCommandDto command) throws AssetDraftException;

    /**
     * Update a draft
     *
     * The status must be {@link EnumProviderAssetDraftStatus#DRAFT}
     *
     * @param command
     * @return
     */
    AssetDraftDto updateDraft(CatalogueItemCommandDto command) throws AssetDraftException;

    /**
     * Update draft metadata
     *
     * The status must be {@link EnumProviderAssetDraftStatus#PENDING_PROVIDER_REVIEW}
     *
     * @param command
     * @return
     */
    AssetDraftDto updateDraftMetadata(CatalogueItemMetadataCommandDto command) throws AssetDraftException;

    /**
     * Delete a draft
     *
     * The status must be one of:
     * {@link EnumProviderAssetDraftStatus#DRAFT}
     * {@link EnumProviderAssetDraftStatus#HELPDESK_REJECTED}
     * {@link EnumProviderAssetDraftStatus#PROVIDER_REJECTED}
     *
     * @param ownerKey
     * @param publisherKey
     * @param draftKey
     */
    void deleteDraft(UUID ownerKey, UUID publisherKey, UUID draftKey) throws AssetDraftException;

    /**
     * Submit a draft to OP HelpDesk for review
     *
     * The status must be {@link EnumProviderAssetDraftStatus#DRAFT}
     *
     * @param command
     * @return
     */
    void submitDraft(CatalogueItemCommandDto command) throws AssetDraftException;

    /**
     * Update draft status
     *
     * @param command
     * @throws AssetDraftException
     */
    void updateStatus(AssetDraftSetStatusCommandDto command) throws AssetDraftException;

    /**
     * Accept or reject a draft from a HelpDesk account
     *
     * @param command
     * @throws AssetDraftException
     */
    void reviewHelpDesk(AssetDraftReviewCommandDto command) throws AssetDraftException;

    /**
     * Accept or reject a draft from a provider account
     *
     * @param command
     * @throws AssetDraftException
     */
    void reviewProvider(AssetDraftReviewCommandDto command) throws AssetDraftException;

    /**
     * Publish draft to catalogue service
     *
     * @param ownerKey
     * @param publisherKey
     * @param draftKey
     * @throws AssetDraftException
     */
    void publishDraft(UUID ownerKey, UUID publisherKey, UUID draftKey) throws AssetDraftException;

    /**
     * Cancel a draft publication
     *
     * The draft status is set to {@link EnumDraftStatus#DRAFT} and error
     * details are set for the workflow instance.
     *
     * @param publisherKey
     * @param draftKey
     * @param errorDetails
     * @param errorMessages
     * @throws AssetDraftException
     */
    void cancelPublishDraft(UUID publisherKey, UUID draftKey, String errorDetails, List<Message> errorMessages) throws AssetDraftException;

    /**
     * Update draft metadata
     *
     * @param publisherKey
     * @param draftKey
     * @param resourceKey
     * @param metadata
     * @throws FileSystemException
     * @throws AssetDraftException
     */
    void updateMetadata(UUID publisherKey, UUID draftKey, String resourceKey, JsonNode metadata) throws FileSystemException, AssetDraftException;

    /**
     * Update draft ingestion information
     *
     * @param publisherKey
     * @param draftKey
     * @param resourceKey
     * @param data
     * @throws AssetDraftException
     */
    void updateResourceIngestionData(
        UUID publisherKey, UUID draftKey, String resourceKey, ServerIngestResultResponseDto data
    ) throws AssetDraftException;

    /**
     * Update draft WMS/WFS publication information
     *
     * @param publisherKey
     * @param draftKey
     * @param resourceKey
     * @param data
     * @throws AssetDraftException
     */
    void updateResourceIngestionData(
        UUID publisherKey, UUID draftKey, String resourceKey, ServerIngestPublishResponseDto data
    ) throws AssetDraftException;

    /**
     * Adds a file from the user's file system to the selected asset
     *
     * @param command Resource metadata. If a file with the same name already exists for the asset, it is overwritten
     *
     * @return The updated draft
     *
     * @throws FileSystemException
     * @throws AssetRepositoryException
     * @throws AssetDraftException
     */
    AssetDraftDto addFileResourceFromFileSystem(
        UserFileResourceCommandDto command
    ) throws FileSystemException, AssetRepositoryException, AssetDraftException;

    /**
     * Adds an uploaded file to the selected asset
     *
     * @param command Resource metadata. If a file with the same name already exists for the asset, it is overwritten
     * @param input An input stream of the uploaded file. The caller should close the stream.
     *
     * @return The updated draft
     *
     * @throws FileSystemException
     * @throws AssetRepositoryException
     * @throws AssetDraftException
     */
    AssetDraftDto addFileResourceFromUpload(
        FileResourceCommandDto command, InputStream input
    ) throws FileSystemException, AssetRepositoryException, AssetDraftException;

    /**
     * Download a file from a URL and add it as a file resource to the selected
     * asset
     *
     * @param command Resource metadata
     *
     * @return The updated draft
     *
     * @throws FileSystemException
     * @throws AssetRepositoryException
     * @throws AssetDraftException
     */
    AssetDraftDto addFileResourceFromExternalUrl(
        ExternalUrlFileResourceCommandDto command
    ) throws FileSystemException, AssetRepositoryException, AssetDraftException;

    /**
     * Adds a resource to the specified asset
     *
     * @param publisherKey
     * @param draftKey
     * @param resource
     * @return
     * @throws AssetDraftException
     */
    AssetDraftDto addResource(UUID publisherKey, UUID draftKey, ResourceDto resource) throws AssetDraftException;

    /**
     * Uploads an additional resource file for the selected asset
     *
     * @param command Resource metadata. If a file with the same name already already exists for the asset, it is overwritten
     * @param input An input stream of the uploaded file. The caller should close the stream.
     *
     * @return The updated draft
     *
     * @throws FileSystemException
     * @throws AssetRepositoryException
     * @throws AssetDraftException
     */
    AssetDraftDto addAdditionalResource(
        AssetAdditionalResourceCommandDto command, InputStream input
    ) throws FileSystemException, AssetRepositoryException, AssetDraftException;

    /**
     * Sets the contract file for the selected asset
     *
     * @param command Uploaded contract. If a file with the same name already already exists for the asset, it is overwritten
     * @param data A byte array with the uploaded file
     *
     * @return
     *
     * @throws FileSystemException
     * @throws AssetRepositoryException
     * @throws AssetDraftException
     */
    void setContract(
		ProviderUploadContractCommand command, byte[] data
    ) throws FileSystemException, AssetRepositoryException, AssetDraftException;

    /**
     * Adds an additional contract annex file for the selected asset
     *
     * @param command Contract annex metadata. If a file with the same name already already exists for the asset, it is overwritten
     * @param data A byte array with the uploaded file
     *
     * @return The updated draft
     *
     * @throws FileSystemException
     * @throws AssetRepositoryException
     * @throws AssetDraftException
     */
    AssetDraftDto addContractAnnex(
        AssetContractAnnexCommandDto command, byte[] data
    ) throws FileSystemException, AssetRepositoryException, AssetDraftException;

    /**
     * Resolve the path of an uploaded contract of a draft asset
     *
     * @param pid
     *
     * @throws FileSystemException If an I/O error occurs
     * @throws AssetRepositoryException If resolve operation fails
     */
    Path resolveDraftContractPath(
		UUID ownerKey, UUID publisherKey, UUID draftKey
    ) throws FileSystemException, AssetRepositoryException;

    /**
     * Resolve the path of an uploaded contract of an asset
     *
     * @param ownerKey
     * @param publisherKey
     * @return path
     *
     * @throws FileSystemException If an I/O error occurs
     * @throws AssetRepositoryException If resolve operation fails
     */
    Path resolveAssetContractPath(
         String pid
    ) throws FileSystemException, AssetRepositoryException;

    /**
     * Resolve the path of an annex for the uploaded contract of an asset
     *
     * @param pid
     * @param resourceKey
     * @return
     *
     * @throws FileSystemException If an I/O error occurs
     * @throws AssetRepositoryException If resolve operation fails
     */
    Path resolveAssetContractAnnexPath(String pid, String resourceKey) throws FileSystemException, AssetRepositoryException;

    /**
     * Resolve the path of an annex for the uploaded contract of a draft asset
     *
     * @param ownerKey
     * @param publisherKey
     * @param draftKey
     * @param resourceKey
     * @return
     *
     * @throws FileSystemException If an I/O error occurs
     * @throws AssetRepositoryException If resolve operation fails
     */
    Path resolveDraftContractAnnexPath(
        UUID ownerKey, UUID publisherKey, UUID draftKey, String resourceKey
    ) throws FileSystemException, AssetRepositoryException;

    /**
     * Resolve the path of an additional file resource of an asset
     *
     * @param pid
     * @param resourceKey
     * @return
     *
     * @throws FileSystemException If an I/O error occurs
     * @throws AssetRepositoryException If resolve operation fails
     */
    Path resolveAssetAdditionalResource(String pid, String resourceKey) throws FileSystemException, AssetRepositoryException;

    /**
     * Resolve the path of an additional file resource of a draft asset
     *
     * @param ownerKey
     * @param publisherKey
     * @param draftKey
     * @param resourceKey
     * @return
     *
     * @throws FileSystemException If an I/O error occurs
     * @throws AssetRepositoryException If resolve operation fails
     */
    Path resolveDraftAdditionalResource(
        UUID ownerKey, UUID publisherKey, UUID draftKey, String resourceKey
    ) throws FileSystemException, AssetRepositoryException;

    /**
     * Resolve path to metadata property file for a specific resource of a draft asset
     *
     * @param pid
     * @param resourceKey
     * @param propertyName
     * @return
     *
     * @throws FileSystemException If an I/O error occurs
     * @throws AssetRepositoryException If resolve operation fails
     */
    MetadataProperty resolveAssetMetadataProperty(
        String pid, String resourceKey, String propertyName
    ) throws FileSystemException, AssetRepositoryException;

    /**
     * Resolve path to metadata property file for a specific resource of a draft asset
     *
     * @param ownerKey
     * @param publisherKey
     * @param draftKey
     * @param resourceKey
     * @param propertyName
     * @return
     *
     * @throws FileSystemException If an I/O error occurs
     * @throws AssetRepositoryException If resolve operation fails
     */
    MetadataProperty resolveDraftMetadataProperty(
        UUID ownerKey, UUID publisherKey, UUID draftKey, String resourceKey, String propertyName
    ) throws FileSystemException, AssetRepositoryException;

    /**
     * Update metadata property links
     *
     * @param id
     * @param resources
     * @param metadata
     * @param status
     * @throws AssetDraftException
     */
    void updateMetadataPropertyLinks(
        String id, List<ResourceDto> resources, JsonNode metadata, EnumProviderAssetDraftStatus status
    ) throws AssetDraftException;

    /**
     * Unpublish asset
     *
     * @param command
     * @throws CatalogueServiceException
     */
    void unpublishAsset(UnpublishAssetCommand command) throws CatalogueServiceException;

    /**
     * Find all registered subscriptions for the specified provider
     *
     * @param publisherKey
     * @param type
     * @param pageIndex
     * @param pageSize
     * @param orderBy
     * @param order
     * @return
     */
    PageResultDto<ProviderAccountSubscriptionDto> findAllSubscriptions(
        UUID publisherKey, int pageIndex, int pageSize,
        EnumProviderSubSortField orderBy, EnumSortingOrder order
    );

    default PageResultDto<ProviderAccountSubscriptionDto> findAllSubscriptions(
        UUID publisherKey, Set<EnumSpatialDataServiceType> serviceType,
        int pageIndex, int pageSize
    ) {
        return this.findAllSubscriptions(
            publisherKey, pageIndex, pageSize,
            EnumProviderSubSortField.ADDED_ON, EnumSortingOrder.DESC
        );
    }

    /**
     * Release a record lock
     *
     * @param userKey
     * @param draftKey
     * @throws AssetDraftException
     */
    void releaseLock(UUID userKey, UUID draftKey) throws AssetDraftException;

    /**
     * Get draft ingestion data
     *
     * This method is using caching. If the most recent updates are required,
     * {@link #findOneDraft} should be used.
     *
     * @param publisherKey
     * @param draftKey
     * @return
     * throws AssetDraftException if draft or ingestion data is not found
     */
    List<ResourceIngestionDataDto> getServices(UUID publisherKey, UUID draftKey) throws AssetDraftException;

}
