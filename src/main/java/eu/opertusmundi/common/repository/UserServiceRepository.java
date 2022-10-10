package eu.opertusmundi.common.repository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.locationtech.jts.geom.Geometry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.fasterxml.jackson.databind.JsonNode;

import eu.opertusmundi.common.domain.AccountEntity;
import eu.opertusmundi.common.domain.UserServiceEntity;
import eu.opertusmundi.common.model.Message;
import eu.opertusmundi.common.model.asset.UserServiceMessageCode;
import eu.opertusmundi.common.model.asset.service.EnumUserServiceStatus;
import eu.opertusmundi.common.model.asset.service.EnumUserServiceType;
import eu.opertusmundi.common.model.asset.service.UserServiceCommandDto;
import eu.opertusmundi.common.model.asset.service.UserServiceDto;
import eu.opertusmundi.common.model.catalogue.client.EnumSpatialDataServiceType;
import eu.opertusmundi.common.model.ingest.ResourceIngestionDataDto;
import eu.opertusmundi.common.model.ingest.ServerIngestPublishResponseDto;
import eu.opertusmundi.common.service.UserServiceException;

@Repository
@Transactional(readOnly = true)
public interface UserServiceRepository extends JpaRepository<UserServiceEntity, Integer> {

    @Query("SELECT a FROM Account a WHERE a.key = :key")
    Optional<AccountEntity> findAccountByKey(UUID key);

    @Query("SELECT s.id FROM UserService s WHERE s.key = :key")
    Integer getIdFromKey(UUID key);

    @Query("SELECT s FROM UserService s WHERE "
         + "(s.status != 'DELETED') and "
         + "(s.status in :status or :status is null) and "
         + "(s.serviceType in :serviceType or :serviceType is null) and "
         + "(s.account.key = :ownerKey)"
    )
    Page<UserServiceEntity> findAllByOwnerAndStatus(
        UUID ownerKey,
        Set<EnumUserServiceStatus> status,
        Set<EnumUserServiceType> serviceType,
        Pageable pageable
    );

    @Query("SELECT s FROM UserService s WHERE "
         + "(s.status != 'DELETED') and "
         + "(s.status in :status or :status is null) and "
         + "(s.account.key = :ownerKey) and "
         + "(s.account.parent.key = :parentKey) and "
         + "(s.serviceType in :serviceType or :serviceType is null) "
    )
    Page<UserServiceEntity> findAllByOwnerAndParentAndStatus(
        UUID ownerKey,
        UUID parentKey,
        Set<EnumUserServiceStatus> status,
        Set<EnumUserServiceType> serviceType,
        Pageable pageable
    );

    @Query("SELECT s FROM UserService s WHERE s.account.key = :ownerKey")
    Page<UserServiceEntity> findAllByOwner(UUID ownerKey, Pageable pageable);

    @Query("SELECT s FROM UserService s WHERE s.status in :status")
    Page<UserServiceEntity> findAllByStatus(Set<EnumUserServiceStatus> status, Pageable pageable);

    @Query("SELECT s FROM UserService s WHERE s.key = :key")
    Optional<UserServiceEntity> findOneByKey(UUID key);

    @Query("SELECT s FROM UserService s WHERE s.key = :serviceKey and s.account.key = :ownerKey")
    Optional<UserServiceEntity> findOneByOwnerAndKey(UUID ownerKey, UUID serviceKey);

    @Query("SELECT s FROM UserService s WHERE s.key = :serviceKey and s.account.key = :ownerKey and s.account.parent.key = :parentKey")
    Optional<UserServiceEntity> findOneByOwnerAndParentAndKey(UUID ownerKey, UUID parentKey, UUID serviceKey);

    @Modifying
    @Transactional(readOnly = false)
    @Query("UPDATE UserService s "
         + "SET s.processDefinition = :processDefinition, s.processInstance = :processInstance "
         + "WHERE s.key = :key and s.processInstance is null")
    void setWorkflowInstance(UUID key, String processDefinition, String processInstance);

    @Transactional(readOnly = false)
    default UserServiceDto create(UserServiceCommandDto command) throws UserServiceException {
        Assert.notNull(command, "Expected a non-null command");

        final ZonedDateTime now = ZonedDateTime.now();

        // Check owner
        final AccountEntity owner = this.findAccountByKey(command.getOwnerKey()).orElse(null);

        if (owner == null) {
            throw new UserServiceException(UserServiceMessageCode.USER_NOT_FOUND);
        }

        // Check service
        final UserServiceEntity service = new UserServiceEntity();

        service.setAbstractText(command.getAbstractText());
        service.setAccount(owner);
        service.setComputedGeometry(false);
        service.setCrs(command.getCrs());
        service.setEncoding(command.getEncoding());
        service.setFileName(command.getFileName());
        service.setFileSize(command.getFileSize());
        service.setFormat(command.getFormat());
        service.setPath(command.getPath());
        service.setServiceType(command.getServiceType());
        service.setStatus(EnumUserServiceStatus.PROCESSING);
        service.setTitle(command.getTitle());
        service.setUpdatedOn(now);
        service.setVersion(command.getVersion());

        this.saveAndFlush(service);

        return service.toDto();
    }

    @Transactional(readOnly = false)
    default void updateMetadata(UUID ownerKey, UUID serviceKey, JsonNode metadata) throws UserServiceException {
        final UserServiceEntity service = this.findOneByOwnerAndKey(ownerKey, serviceKey).orElse(null);

        if (service == null) {
            throw new UserServiceException(UserServiceMessageCode.SERVICE_NOT_FOUND);
        }

        if (service.getStatus() != EnumUserServiceStatus.PROCESSING) {
            throw new UserServiceException(
                UserServiceMessageCode.INVALID_STATE,
                String.format("Expected status is [%s]. Found [%s]", EnumUserServiceStatus.PROCESSING, service.getStatus())
            );
        }

        service.setAutomatedMetadata(metadata);
        service.setUpdatedOn(ZonedDateTime.now());

        this.saveAndFlush(service);
    }

    @Transactional(readOnly = false)
    default void publish(UUID ownerKey, UUID serviceKey) throws UserServiceException {
        final UserServiceEntity service = this.findOneByOwnerAndKey(ownerKey, serviceKey).orElse(null);

        if (service == null) {
            throw new UserServiceException(UserServiceMessageCode.SERVICE_NOT_FOUND);
        }

        // Set modified on only the first time the status changes
        if (service.getStatus() != EnumUserServiceStatus.PUBLISHED) {
            service.setUpdatedOn(ZonedDateTime.now());
        }
        service.setStatus(EnumUserServiceStatus.PUBLISHED);
    }

    @Transactional(readOnly = false)
    default void reset(UUID ownerKey, UUID serviceKey, String errorDetails, List<Message> errorMessages) throws UserServiceException {
        final UserServiceEntity service = this.findOneByOwnerAndKey(ownerKey, serviceKey).orElse(null);

        if (service == null) {
            throw new UserServiceException(UserServiceMessageCode.SERVICE_NOT_FOUND);
        }
        if (service.getStatus() != EnumUserServiceStatus.PROCESSING) {
            throw new UserServiceException(
                UserServiceMessageCode.INVALID_STATE,
                String.format("Invalid service status found. [status=%s]", service.getStatus())
            );
        }

        service.setUpdatedOn(ZonedDateTime.now());
        service.setStatus(EnumUserServiceStatus.FAILED);
        service.setWorkflowErrorDetails(errorDetails);
        service.setWorkflowErrorMessages(errorMessages);

        // Reset profile data
        service.setAutomatedMetadata(null);
        if (service.isComputedGeometry()) {
            service.setGeometry(null);
            service.setComputedGeometry(false);
        }
        // Reset ingest data
        service.setIngestData(null);

        this.saveAndFlush(service);
    }

    @Transactional(readOnly = false)
    default void setErrorMessage(UUID ownerKey, UUID serviceKey, String message) throws UserServiceException {
        final UserServiceEntity service = this.findOneByOwnerAndKey(ownerKey, serviceKey).orElse(null);

        if (service == null) {
            throw new UserServiceException(UserServiceMessageCode.SERVICE_NOT_FOUND);
        }
        if (service.getStatus() == EnumUserServiceStatus.FAILED ||
            service.getStatus() == EnumUserServiceStatus.PUBLISHED
        ) {
            throw new UserServiceException(
                UserServiceMessageCode.INVALID_STATE,
                String.format("Invalid service status found. [status=%s]", service.getStatus())
            );
        }
        service.setUpdatedOn(ZonedDateTime.now());
        service.setHelpdeskErrorMessage(message);

        this.saveAndFlush(service);
    }

    @Transactional(readOnly = false)
    default void updateStatus(UUID parentKey, UUID serviceKey, EnumUserServiceStatus status) throws UserServiceException {
        final UserServiceEntity service = this.findOneByOwnerAndKey(parentKey, serviceKey).orElse(null);

        if (service == null) {
            throw new UserServiceException(UserServiceMessageCode.SERVICE_NOT_FOUND);
        }

        // Set modified on only the first time the status changes
        if (service.getStatus() != status) {
            service.setUpdatedOn(ZonedDateTime.now());
        }

        service.setStatus(status);
    }

    /**
     * Update service metadata. Optionally, if the geometry argument is not null,
     * update geometry too.
     *
     * @param parentKey
     * @param serviceKey
     * @param metadata
     * @param geometry
     * @throws UserServiceException
     */
    @Transactional(readOnly = false)
    default void updateMetadataAndGeometry(
        UUID ownerKey, UUID serviceKey, JsonNode metadata, @Nullable Geometry geometry
    ) throws UserServiceException {
        final UserServiceEntity service = this.findOneByOwnerAndKey(ownerKey, serviceKey).orElse(null);

        if (service == null) {
            throw new UserServiceException(UserServiceMessageCode.SERVICE_NOT_FOUND);
        }

        if (service.getStatus() != EnumUserServiceStatus.PROCESSING) {
            throw new UserServiceException(
                UserServiceMessageCode.INVALID_STATE,
                String.format("Expected status is [%s]. Found [%s]", EnumUserServiceStatus.PROCESSING, service.getStatus())
            );
        }

        // Update geometry only if it is not already set and a non-null geometry
        // is specified
        if (service.getGeometry() == null && geometry != null) {
            service.setGeometry(geometry);
            service.setComputedGeometry(true);
        }

        service.setAutomatedMetadata(metadata);
        service.setUpdatedOn(ZonedDateTime.now());

        this.saveAndFlush(service);
    }

    /**
     * Updates ingestion data for an asset resource
     *
     * If an entry already exists for the specified resource key, the operation
     * does not update the asset metadata.
     *
     * @param parentKey
     * @param serviceKey
     * @param resourceKey
     * @param data
     * @throws UserServiceException
     */
    @Transactional(readOnly = false)
    default void updateResourceIngestionData(
        UUID ownerKey, UUID serviceKey, ResourceIngestionDataDto data
    ) throws UserServiceException {
        final UserServiceEntity service = this.findOneByOwnerAndKey(ownerKey, serviceKey).orElse(null);

        if (service == null) {
            throw new UserServiceException(UserServiceMessageCode.SERVICE_NOT_FOUND);
        }

        final EnumUserServiceStatus expectedStatus = EnumUserServiceStatus.PROCESSING;

        if (service.getStatus() != expectedStatus) {
            throw new UserServiceException(
                UserServiceMessageCode.INVALID_STATE,
                String.format("Expected status is [%s]. Found [%s]", expectedStatus, service.getStatus())
            );
        }

        // Initialize ingestion data if needed
        if (service.getIngestData() == null) {
            service.setIngestData(data);
        }

        service.setUpdatedOn(ZonedDateTime.now());

        this.saveAndFlush(service);
    }

    @Transactional(readOnly = false)
    default void updateResourceIngestionData(
        UUID ownerKey, UUID serviceKey, ServerIngestPublishResponseDto data
    ) throws UserServiceException {
        final UserServiceEntity service = this.findOneByOwnerAndKey(ownerKey, serviceKey).orElse(null);

        if (service == null) {
            throw new UserServiceException(UserServiceMessageCode.SERVICE_NOT_FOUND);
        }

        final EnumUserServiceStatus expectedStatus = EnumUserServiceStatus.PROCESSING;

        if (service.getStatus() != expectedStatus) {
            throw new UserServiceException(
                UserServiceMessageCode.INVALID_STATE,
                String.format("Expected status is [%s]. Found [%s]", expectedStatus, service.getStatus())
            );
        }

        // Get ingestion data for the specified resource
        final ResourceIngestionDataDto ingestionData = service.getIngestData();

        if (!StringUtils.isBlank(data.getWmsDescribeLayer())) {
            ingestionData.addEndpoint(EnumSpatialDataServiceType.WMS, data.getWmsDescribeLayer());
        }
        if (!StringUtils.isBlank(data.getWfsDescribeFeatureType())) {
            ingestionData.addEndpoint(EnumSpatialDataServiceType.WFS, data.getWfsDescribeFeatureType());
        }

        service.setUpdatedOn(ZonedDateTime.now());

        this.saveAndFlush(service);
    }


    @Transactional(readOnly = false)
    default void remove(UUID ownerKey, UUID serviceKey) {
        Assert.notNull(ownerKey, "Expected a non-null owner key");
        Assert.notNull(serviceKey, "Expected a non-null service key");

        // Check service
        final UserServiceEntity service = this.findOneByOwnerAndKey(ownerKey, serviceKey).orElse(null);

        if (service == null) {
            throw new UserServiceException(UserServiceMessageCode.SERVICE_NOT_FOUND);
        }
        if (service.getStatus() != EnumUserServiceStatus.FAILED &&
            service.getStatus() != EnumUserServiceStatus.PUBLISHED
        ) {
            throw new UserServiceException(
                UserServiceMessageCode.INVALID_STATE,
                String.format("Expected status is [FAILED, PUBLISHED]. Found [%s]", service.getStatus())
            );
        }

        if (service.getStatus() == EnumUserServiceStatus.FAILED) {
            // All resource have been already deleted
            this.delete(service);
        } else {
            // Keep record for billing
            service.setStatus(EnumUserServiceStatus.DELETED);
            service.setUpdatedOn(ZonedDateTime.now());

            this.saveAndFlush(service);
        }
    }

}
