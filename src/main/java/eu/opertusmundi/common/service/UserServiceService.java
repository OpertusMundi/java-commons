package eu.opertusmundi.common.service;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;

import eu.opertusmundi.common.model.EnumSortingOrder;
import eu.opertusmundi.common.model.Message;
import eu.opertusmundi.common.model.PageResultDto;
import eu.opertusmundi.common.model.asset.AssetRepositoryException;
import eu.opertusmundi.common.model.asset.MetadataProperty;
import eu.opertusmundi.common.model.asset.service.EnumUserServiceSortField;
import eu.opertusmundi.common.model.asset.service.EnumUserServiceStatus;
import eu.opertusmundi.common.model.asset.service.EnumUserServiceType;
import eu.opertusmundi.common.model.asset.service.UserServiceCommandDto;
import eu.opertusmundi.common.model.asset.service.UserServiceDto;
import eu.opertusmundi.common.model.file.FileSystemException;
import eu.opertusmundi.common.model.ingest.ServerIngestPublishResponseDto;
import eu.opertusmundi.common.model.ingest.ServerIngestResultResponseDto;

public interface UserServiceService {

    /**
     * Search services
     *
     * @param ownerKey
     * @param parentKey
     * @param status
     * @param type
     * @param serviceType
     * @param pageIndex
     * @param pageSize
     * @param orderBy
     * @param order
     * @return
     */
    PageResultDto<UserServiceDto> findAll(
        UUID ownerKey, UUID parentKey,
        Set<EnumUserServiceStatus> status, Set<EnumUserServiceType> serviceType,
        int pageIndex, int pageSize,
        EnumUserServiceSortField orderBy, EnumSortingOrder order
    );

    default PageResultDto<UserServiceDto> findAll(
        UUID ownerKey, UUID parentKey,
        Set<EnumUserServiceStatus> status, Set<EnumUserServiceType> serviceType,
        int pageIndex, int pageSize
    ) {
        return this.findAll(
            ownerKey, parentKey, status, serviceType, pageIndex, pageSize,
            EnumUserServiceSortField.UPDATED_ON, EnumSortingOrder.DESC
        );
    }

    /**
     * Get one service by owner, parent and key
     *
     * @param ownerKey
     * @param parentKey
     * @param serviceKey
     * @param boolean locked
     * @return
     */
    UserServiceDto findOne(UUID ownerKey, UUID parentKey, UUID serviceKey);

    /**
     * Get one service by key
     *
     * @param serviceKey
     * @return
     */
    UserServiceDto findOne(UUID serviceKey);

    /**
     * Create service
     *
     * @param command
     * @return
     * @throws UserServiceException
     */
    UserServiceDto create(UserServiceCommandDto command) throws UserServiceException;

    /**
     * Delete an existing service
     *
     * The status must be {@link EnumUserServiceStatus#PROCESSING}
     *
     * @param ownerKey
     * @param parentKey
     * @param serviceKey
     */
    void delete(UUID ownerKey, UUID parentKey, UUID serviceKey) throws UserServiceException;

    /**
     * Publish service to the GeoServer
     *
     * @param ownerKey
     * @param parentKey
     * @param serviceKey
     * @throws UserServiceException
     */
    void publish(UUID ownerKey, UUID parentKey, UUID serviceKey) throws UserServiceException;

    /**
     * Cancel a service publication
     *
     * The status is set to {@link EnumUserServiceStatus#FAILED} and error
     * details are set for the workflow instance.
     *
     * @param ownerKey
     * @param serviceKey
     * @param errorDetails
     * @param errorMessages
     * @throws UserServiceException
     */
    void cancelPublishOperation(UUID ownerKey, UUID serviceKey, String errorDetails, List<Message> errorMessages) throws UserServiceException;

    /**
     * Update service metadata
     *
     * @param ownerKey
     * @param serviceKey
     * @param resourceKey
     * @param metadata
     * @throws FileSystemException
     * @throws UserServiceException
     */
    void updateMetadata(UUID ownerKey, UUID serviceKey, JsonNode metadata) throws FileSystemException, UserServiceException;

    /**
     * Update service resource ingestion information
     *
     * @param ownerKey
     * @param serviceKey
     * @param resourceKey
     * @param data
     * @throws UserServiceException
     */
    void updateResourceIngestionData(
        UUID ownerKey, UUID serviceKey, ServerIngestResultResponseDto data
    ) throws UserServiceException;

    /**
     * Update WMS/WFS endpoints
     *
     * @param ownerKey
     * @param serviceKey
     * @param resourceKey
     * @param data
     * @throws UserServiceException
     */
    void updateResourceIngestionData(
        UUID ownerKey, UUID serviceKey, ServerIngestPublishResponseDto data
    ) throws UserServiceException;

    /**
     * Resolve path to metadata property file for the resource of the specified service
     *
     * @param ownerKey
     * @param publisherKey
     * @param serviceKey
     * @param resourceKey
     * @param propertyName
     * @return
     *
     * @throws FileSystemException If an I/O error occurs
     * @throws AssetRepositoryException If resolve operation fails
     */
    MetadataProperty resolveMetadataProperty(
        UUID ownerKey, UUID publisherKey, UUID serviceKey, String propertyName
    ) throws FileSystemException, AssetRepositoryException;

    /**
     * Release a record lock
     *
     * @param userKey
     * @param serviceKey
     * @throws UserServiceException
     */
    void releaseLock(UUID userKey, UUID serviceKey) throws UserServiceException;

}
