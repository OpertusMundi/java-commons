package eu.opertusmundi.common.service;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;

import eu.opertusmundi.common.model.EnumSortingOrder;
import eu.opertusmundi.common.model.PageResultDto;
import eu.opertusmundi.common.model.asset.AssetDraftDto;
import eu.opertusmundi.common.model.asset.AssetDraftReviewCommandDto;
import eu.opertusmundi.common.model.asset.AssetDraftSetStatusCommandDto;
import eu.opertusmundi.common.model.asset.AssetFileAdditionalResourceCommandDto;
import eu.opertusmundi.common.model.asset.AssetRepositoryException;
import eu.opertusmundi.common.model.asset.EnumProviderAssetDraftSortField;
import eu.opertusmundi.common.model.asset.EnumProviderAssetDraftStatus;
import eu.opertusmundi.common.model.asset.FileResourceCommandDto;
import eu.opertusmundi.common.model.asset.MetadataProperty;
import eu.opertusmundi.common.model.asset.ServiceResourceDto;
import eu.opertusmundi.common.model.catalogue.client.CatalogueHarvestImportCommandDto;
import eu.opertusmundi.common.model.catalogue.client.CatalogueItemCommandDto;
import eu.opertusmundi.common.model.catalogue.client.DraftApiCommandDto;
import eu.opertusmundi.common.model.file.FileSystemException;
import eu.opertusmundi.common.model.ingest.ServerIngestPublishResponseDto;
import eu.opertusmundi.common.model.ingest.ServerIngestResultResponseDto;

public interface ProviderAssetService {

    /**
     * Search drafts
     *
     * @param publisherKey
     * @param status
     * @param pageIndex
     * @param pageSize
     * @param orderBy
     * @param order
     * @return
     */
    PageResultDto<AssetDraftDto> findAllDraft(
        UUID publisherKey, Set<EnumProviderAssetDraftStatus> status, int pageIndex, int pageSize,
        EnumProviderAssetDraftSortField orderBy, EnumSortingOrder order
    );

    default PageResultDto<AssetDraftDto> findAllDraft(
        UUID publisherKey, Set<EnumProviderAssetDraftStatus> status, int pageIndex, int pageSize
    ) {
        return this.findAllDraft(
            publisherKey, status, pageIndex, pageSize, EnumProviderAssetDraftSortField.MODIFIED_ON, EnumSortingOrder.DESC
        );
    }

    /**
     * Get one draft by key
     *
     * @param publisherKey
     * @param draftKey
     * @return
     */
    AssetDraftDto findOneDraft(UUID publisherKey, UUID draftKey);

    /**
     * Create API draft
     *
     * @param command
     * @return
     * @throws AssetDraftException
     */
    AssetDraftDto createApiDraft(DraftApiCommandDto command) throws AssetDraftException;

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
     * @param draft
     * @return
     */
    AssetDraftDto updateDraft(CatalogueItemCommandDto command) throws AssetDraftException;

    /**
     * Delete a draft
     *
     * The status must be one of:
     * {@link EnumProviderAssetDraftStatus#DRAFT}
     * {@link EnumProviderAssetDraftStatus#HELPDESK_REJECTED}
     * {@link EnumProviderAssetDraftStatus#PROVIDER_REJECTED}
     *
     * @param publisheKey
     * @param draftKey
     */
    void deleteDraft(UUID publisherKey, UUID draftKey) throws AssetDraftException;

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
     * Accept a draft from a HelpDesk account
     *
     * @param publisherKey
     * @param draftKey
     * @throws AssetDraftException
     */
    void acceptHelpDesk(UUID publisherKey, UUID draftKey) throws AssetDraftException;

    /**
     * Reject a draft from a HelpDesk account
     *
     * @param publisherKey
     * @param draftKey
     * @param reason
     * @throws AssetDraftException
     */
    void rejectHelpDesk(UUID publisherKey, UUID draftKey, String reason) throws AssetDraftException;

    /**
     * Accept a draft from a provider account
     *
     * @param command
     * @throws AssetDraftException
     */
    void acceptProvider(AssetDraftReviewCommandDto command) throws AssetDraftException;

    /**
     * Reject a draft from a provider account
     *
     * @param command
     * @throws AssetDraftException
     */
    void rejectProvider(AssetDraftReviewCommandDto command) throws AssetDraftException;

    /**
     * Publish draft to catalogue service
     *
     * @param publisherKey
     * @param draftKey
     * @throws AssetDraftException
     */
    void publishDraft(UUID publisherKey, UUID draftKey) throws AssetDraftException;

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
    void updateMetadata(UUID publisherKey, UUID draftKey, UUID resourceKey, JsonNode metadata) throws FileSystemException, AssetDraftException;

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
        UUID publisherKey, UUID draftKey, UUID resourceKey, ServerIngestResultResponseDto data
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
        UUID publisherKey, UUID draftKey, UUID resourceKey, ServerIngestPublishResponseDto data
    ) throws AssetDraftException;

    /**
     * Uploads a file resource to the selected asset
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
    AssetDraftDto addFileResource(
        FileResourceCommandDto command, InputStream input
    ) throws FileSystemException, AssetRepositoryException, AssetDraftException;

    /**
     * Adds a service resource to the specified asset
     * 
     * @param publisherKey
     * @param draftKey
     * @param resource
     * @return
     * @throws AssetDraftException
     */
    AssetDraftDto addServiceResource(UUID publisherKey, UUID draftKey, ServiceResourceDto resource) throws AssetDraftException;

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
        AssetFileAdditionalResourceCommandDto command, InputStream input
    ) throws FileSystemException, AssetRepositoryException, AssetDraftException;

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
    Path resolveAssetAdditionalResource(String pid, UUID resourceKey) throws FileSystemException, AssetRepositoryException;

    /**
     * Resolve the path of an additional file resource of a draft asset
     *
     * @param publisherKey
     * @param draftKey
     * @param resourceKey
     * @return
     *
     * @throws FileSystemException If an I/O error occurs
     * @throws AssetRepositoryException If resolve operation fails
     */
    Path resolveDraftAdditionalResource(UUID publisherKey, UUID draftKey, UUID resourceKey) throws FileSystemException, AssetRepositoryException;

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
        String pid, UUID resourceKey, String propertyName
    ) throws FileSystemException, AssetRepositoryException;

    /**
     * Resolve path to metadata property file for a specific resource of a draft asset
     *
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
        UUID publisherKey, UUID draftKey, UUID resourceKey, String propertyName
    ) throws FileSystemException, AssetRepositoryException;

}
