package eu.opertusmundi.common.repository;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.locationtech.jts.geom.Geometry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.fasterxml.jackson.databind.JsonNode;

import eu.opertusmundi.common.domain.AccountEntity;
import eu.opertusmundi.common.domain.ProviderAssetDraftEntity;
import eu.opertusmundi.common.model.asset.AssetDraftDto;
import eu.opertusmundi.common.model.asset.AssetMessageCode;
import eu.opertusmundi.common.model.asset.EnumProviderAssetDraftStatus;
import eu.opertusmundi.common.model.asset.ServiceResourceDto;
import eu.opertusmundi.common.model.catalogue.client.CatalogueItemCommandDto;
import eu.opertusmundi.common.model.catalogue.client.CatalogueItemVisibilityCommandDto;
import eu.opertusmundi.common.model.catalogue.client.EnumAssetType;
import eu.opertusmundi.common.model.catalogue.client.EnumSpatialDataServiceType;
import eu.opertusmundi.common.model.ingest.ResourceIngestionDataDto;
import eu.opertusmundi.common.model.ingest.ServerIngestPublishResponseDto;
import eu.opertusmundi.common.service.AssetDraftException;

@Repository
@Transactional(readOnly = true)
public interface DraftRepository extends JpaRepository<ProviderAssetDraftEntity, Integer> {

    @Query("SELECT a FROM Account a WHERE a.key = :key")
    Optional<AccountEntity> findAccountByKey(@Param("key") UUID key);

    @Query("SELECT a FROM ProviderAssetDraft a WHERE "
           + "(a.status in :status or :status is null) and "
           + "(a.account.key = :publisherKey or CAST(:publisherKey as org.hibernate.type.UUIDCharType) is null) and "
           + "(a.type in :type or :type is null) and "
           + "(a.serviceType in :serviceType or :serviceType is null) "
    )
    Page<ProviderAssetDraftEntity> findAllByPublisherAndStatus(
        @Param("publisherKey") UUID publisherKey,
        @Param("status") Set<EnumProviderAssetDraftStatus> status,
        @Param("type") Set<EnumAssetType> type,
        @Param("serviceType") Set<EnumSpatialDataServiceType> serviceType,
        Pageable pageable
    );

    @Query("SELECT d FROM ProviderAssetDraft d WHERE "
            + "(d.status in :status or :status is null) and "
            + "(d.vendorAccount.key = :ownerKey) and "
            + "(d.account.key = :publisherKey) and "
            + "(d.type in :type or :type is null) and "
            + "(d.serviceType in :serviceType or :serviceType is null) "
     )
     Page<ProviderAssetDraftEntity> findAllByOwnerAndPublisherAndStatus(
         @Param("ownerKey") UUID ownerKey,
         @Param("publisherKey") UUID publisherKey,
         @Param("status") Set<EnumProviderAssetDraftStatus> status,
         @Param("type") Set<EnumAssetType> type,
         @Param("serviceType") Set<EnumSpatialDataServiceType> serviceType,
         Pageable pageable
     );

    @Query("SELECT a FROM ProviderAssetDraft a WHERE "
           + "(a.parentId = :parentId) and "
           + "(not status in ('HELPDESK_REJECTED', 'PROVIDER_REJECTED', 'PUBLISHED'))")
    List<ProviderAssetDraftEntity> findAllByParentId(String parentId);

    @Query("SELECT a FROM ProviderAssetDraft a WHERE a.account.key = :publisherKey")
    Page<ProviderAssetDraftEntity> findAllByPublisher(
        @Param("publisherKey") UUID publisherKey, Pageable pageable
    );

    @Query("SELECT a FROM ProviderAssetDraft a WHERE a.status in :status")
    Page<ProviderAssetDraftEntity> findAllByStatus(
        @Param("status") Set<EnumProviderAssetDraftStatus> status, Pageable pageable
    );

    @Query("SELECT a FROM ProviderAssetDraft a WHERE a.key = :key and a.account.key = :publisherKey")
    Optional<ProviderAssetDraftEntity> findOneByPublisherAndKey(
        @Param("publisherKey") UUID publisherKey, @Param("key") UUID assetKey
    );

    @Query("SELECT a FROM ProviderAssetDraft a WHERE a.key = :key and a.account.key = :publisherKey and a.vendorAccount.key = :ownerKey")
    Optional<ProviderAssetDraftEntity> findOneByOwnerAndPublisherAndKey(
        @Param("ownerKey") UUID ownerKey, @Param("publisherKey") UUID publisherKey, @Param("key") UUID assetKey
    );

    @Query("SELECT a FROM ProviderAssetDraft a WHERE a.key = :key")
    Optional<ProviderAssetDraftEntity> findOneByKey(@Param("key") UUID assetKey);

    @Query("SELECT a.id FROM ProviderAssetDraft a WHERE a.key = :key")
    Integer getIdFromKey(UUID key);

    @Transactional(readOnly = false)
    default AssetDraftDto update(CatalogueItemCommandDto command) throws AssetDraftException {
        return this.update(command, EnumProviderAssetDraftStatus.DRAFT, null, null);
    }

    @Transactional(readOnly = false)
    default AssetDraftDto update(
        CatalogueItemCommandDto command,
        EnumProviderAssetDraftStatus status,
        String processDefinition,
        String processInstance
    ) throws AssetDraftException {
        Assert.notNull(command, "Expected a non-null command");

        Assert.isTrue(
            status == EnumProviderAssetDraftStatus.DRAFT || status == EnumProviderAssetDraftStatus.SUBMITTED,
            "Expected status in [DRAFT, SUBMITTED]"
        );

        final ZonedDateTime now = ZonedDateTime.now();

        // Check provider
        final AccountEntity account = this.findAccountByKey(command.getPublisherKey()).orElse(null);

        if (account == null) {
            throw new AssetDraftException(AssetMessageCode.PROVIDER_NOT_FOUND);
        }
        // Check owner
        final AccountEntity owner = this.findAccountByKey(command.getOwnerKey()).orElse(null);

        if (owner == null) {
            throw new AssetDraftException(AssetMessageCode.VENDOR_ACCOUNT_NOT_FOUND);
        }

        // Check draft
        ProviderAssetDraftEntity draft = null;

        if (command.getDraftKey() != null) {
            draft = command.getOwnerKey().equals(command.getPublisherKey())
                ? this.findOneByPublisherAndKey(command.getPublisherKey(), command.getDraftKey()).orElse(null)
                : this.findOneByOwnerAndPublisherAndKey(command.getOwnerKey(), command.getPublisherKey(), command.getDraftKey()).orElse(null);

            if (draft == null) {
                throw new AssetDraftException(AssetMessageCode.DRAFT_NOT_FOUND);
            }
        } else {
            draft = new ProviderAssetDraftEntity();
            draft.setAssetDraft(draft.getKey());
        }

        if (draft.getStatus() != EnumProviderAssetDraftStatus.DRAFT) {
            throw new AssetDraftException(
                AssetMessageCode.INVALID_STATE,
                String.format("Expected status is [%s]. Found [%s]", status, draft.getStatus())
            );
        }

        draft.setAccount(account);
        draft.setCommand(command);
        draft.setIngested(command.isIngested());
        draft.setModifiedOn(now);
        if (processDefinition != null) {
            draft.setProcessDefinition(processDefinition);
        }
        if (processInstance != null) {
            draft.setProcessInstance(processInstance);
        }
        draft.setParentId(command.getParentId());
        draft.setServiceType(command.getSpatialDataServiceType());
        draft.setStatus(status);
        draft.setTitle(command.getTitle());
        draft.setType(command.getType());
        draft.setVendorAccount(owner);
        draft.setVersion(command.getVersion());

        this.saveAndFlush(draft);

        return draft.toDto();
    }


    @Transactional(readOnly = false)
    default AssetDraftDto update(CatalogueItemVisibilityCommandDto command) throws AssetDraftException {
        Assert.notNull(command, "Expected a non-null command");

        final ZonedDateTime now = ZonedDateTime.now();

        // Check draft
        final ProviderAssetDraftEntity draft = this.findOneByPublisherAndKey(command.getPublisherKey(), command.getDraftKey()).orElse(null);
        if (draft == null) {
            throw new AssetDraftException(AssetMessageCode.DRAFT_NOT_FOUND);
        }
        if (draft.getStatus() != EnumProviderAssetDraftStatus.PENDING_PROVIDER_REVIEW) {
            throw new AssetDraftException(
                AssetMessageCode.INVALID_STATE,
                String.format("Expected status [PENDING_PROVIDER_REVIEW]. Found [%s]", draft.getStatus())
            );
        }
        draft.getCommand().setVisibility(command.getVisibility());
        draft.setModifiedOn(now);

        this.saveAndFlush(draft);

        return draft.toDto();
    }

    /**
     * Update draft metadata. Optionally, if the geometry argument is not null,
     * update geometry too.
     *
     * @param publisherKey
     * @param draftKey
     * @param metadata
     * @param geometry
     * @throws AssetDraftException
     */
    @Transactional(readOnly = false)
    default void updateMetadataAndGeometry(
        UUID publisherKey, UUID draftKey, JsonNode metadata, @Nullable Geometry geometry
    ) throws AssetDraftException {
        final ProviderAssetDraftEntity draft = this.findOneByPublisherAndKey(publisherKey, draftKey).orElse(null);

        if (draft == null) {
            throw new AssetDraftException(AssetMessageCode.DRAFT_NOT_FOUND);
        }

        if (draft.getStatus() != EnumProviderAssetDraftStatus.SUBMITTED) {
            throw new AssetDraftException(
                AssetMessageCode.INVALID_STATE,
                String.format("Expected status is [%s]. Found [%s]", EnumProviderAssetDraftStatus.SUBMITTED, draft.getStatus())
            );
        }

        // Update geometry only if it is not already set and a non-null geometry
        // is specified
        if (draft.getCommand().getGeometry() == null && geometry != null) {
            draft.getCommand().setGeometry(geometry);
        }

		draft.getCommand().setAutomatedMetadata(metadata);
		// NOTE: Workaround for updating metadata. Property command of entity
		// ProviderAssetDraftEntity is annotated with @Convert for serializing a
		// CatalogueItemCommandDto instance to JSON; Hence updating only the metadata
		// nested property wont trigger an update and convertToDatabaseColumn will not be
		// invoked.
		draft.setModifiedOn(ZonedDateTime.now());

        this.saveAndFlush(draft);
    }

    /**
     * Updates ingestion data for an asset resource
     * 
     * If an entry already exists for the specified resource key, the operation
     * does not update the asset metadata.
     * 
     * @param publisherKey
     * @param draftKey
     * @param resourceKey
     * @param data
     * @throws AssetDraftException
     */
    @Transactional(readOnly = false)
    default void updateResourceIngestionData(
        UUID publisherKey, UUID draftKey, String resourceKey, ResourceIngestionDataDto data
    ) throws AssetDraftException {
        final ProviderAssetDraftEntity draft = this.findOneByPublisherAndKey(publisherKey, draftKey).orElse(null);

        if (draft == null) {
            throw new AssetDraftException(AssetMessageCode.DRAFT_NOT_FOUND);
        }

        final EnumProviderAssetDraftStatus expectedStatus = draft.getCommand().getType() == EnumAssetType.SERVICE
            ? EnumProviderAssetDraftStatus.SUBMITTED
            : EnumProviderAssetDraftStatus.POST_PROCESSING;

        if (draft.getStatus() != expectedStatus) {
            throw new AssetDraftException(
                AssetMessageCode.INVALID_STATE,
                String.format("Expected status is [%s]. Found [%s]", expectedStatus, draft.getStatus())
            );
        }

        // Initialize ingestion data if needed
        List<ResourceIngestionDataDto> assetIngestionData = draft.getCommand().getIngestionInfo();
        if (assetIngestionData == null) {
            assetIngestionData = new ArrayList<ResourceIngestionDataDto>();
            draft.getCommand().setIngestionInfo(assetIngestionData);
        }

        // If a record already exists, ignore the update
        final ResourceIngestionDataDto existing = draft.getCommand().getIngestionInfo().stream()
            .filter(i -> i.getKey().equals(resourceKey))
            .findFirst()
            .orElse(null);
        
        if (existing == null) {
            assetIngestionData.add(data);
        }

        // NOTE: Workaround for updating ingestion data. Property command of entity
        // ProviderAssetDraftEntity is annotated with @Convert for serializing a
        // CatalogueItemCommandDto instance to JSON; Hence updating only the metadata
        // nested property wont trigger an update and convertToDatabaseColumn will not be
        // invoked.
        draft.setModifiedOn(ZonedDateTime.now());

        this.saveAndFlush(draft);
    }

    @Transactional(readOnly = false)
    default void updateResourceIngestionData(
        UUID publisherKey, UUID draftKey, String resourceKey, ServerIngestPublishResponseDto data
    ) throws AssetDraftException {
        final ProviderAssetDraftEntity draft = this.findOneByPublisherAndKey(publisherKey, draftKey).orElse(null);

        if (draft == null) {
            throw new AssetDraftException(AssetMessageCode.DRAFT_NOT_FOUND);
        }

        final EnumProviderAssetDraftStatus expectedStatus = draft.getCommand().getType() == EnumAssetType.SERVICE
            ? EnumProviderAssetDraftStatus.SUBMITTED
            : EnumProviderAssetDraftStatus.POST_PROCESSING;

        if (draft.getStatus() != expectedStatus) {
            throw new AssetDraftException(
                AssetMessageCode.INVALID_STATE,
                String.format("Expected status is [%s]. Found [%s]", expectedStatus, draft.getStatus())
            );
        }

        // Initialize ingestion data if needed
        final ResourceIngestionDataDto ingestionData = draft.getCommand().getIngestionInfo().stream()
            .filter(i -> i.getKey().equals(resourceKey))
            .findFirst()
            .get();

        if (!StringUtils.isBlank(data.getWms())) {
            ingestionData.addEndpoint(EnumSpatialDataServiceType.WMS, data.getWms());
        }
        if (!StringUtils.isBlank(data.getWfs())) {
            ingestionData.addEndpoint(EnumSpatialDataServiceType.WFS, data.getWfs());
        }

        // NOTE: Workaround for updating ingestion data. Property command of entity
        // ProviderAssetDraftEntity is annotated with @Convert for serializing a
        // CatalogueItemCommandDto instance to JSON; Hence updating only the metadata
        // nested property wont trigger an update and convertToDatabaseColumn will not be
        // invoked.
        draft.setModifiedOn(ZonedDateTime.now());

        this.saveAndFlush(draft);
    }

    @Transactional(readOnly = false)
    default AssetDraftDto addServiceResource(
        UUID publisherKey, UUID draftKey, ServiceResourceDto resource
    ) throws AssetDraftException {
        final ProviderAssetDraftEntity draft = this.findOneByPublisherAndKey(publisherKey, draftKey).orElse(null);

        if (draft == null) {
            throw new AssetDraftException(AssetMessageCode.DRAFT_NOT_FOUND);
        }

        final EnumProviderAssetDraftStatus expectedStatus = draft.getCommand().getType() == EnumAssetType.SERVICE
            ? EnumProviderAssetDraftStatus.SUBMITTED
            : EnumProviderAssetDraftStatus.POST_PROCESSING;

        if (draft.getStatus() != expectedStatus) {
            throw new AssetDraftException(
                AssetMessageCode.INVALID_STATE,
                String.format("Expected status is [%s]. Found [%s]", EnumProviderAssetDraftStatus.POST_PROCESSING, draft.getStatus())
            );
        }

        // Initialize ingestion data if needed
        draft.getCommand().addServiceResource(resource);
        // NOTE: Workaround for updating ingestion data. Property command of entity
        // ProviderAssetDraftEntity is annotated with @Convert for serializing a
        // CatalogueItemCommandDto instance to JSON; Hence updating only the metadata
        // nested property wont trigger an update and convertToDatabaseColumn will not be
        // invoked.
        draft.setModifiedOn(ZonedDateTime.now());

        return this.saveAndFlush(draft).toDto();
    }

    @Transactional(readOnly = false)
    default EnumProviderAssetDraftStatus acceptHelpDesk(UUID publisherKey, UUID draftKey) throws AssetDraftException {
        final ProviderAssetDraftEntity     draft      = this.findOneByPublisherAndKey(publisherKey, draftKey).orElse(null);
        final EnumProviderAssetDraftStatus currStatus = EnumProviderAssetDraftStatus.PENDING_HELPDESK_REVIEW;
        final EnumProviderAssetDraftStatus nextStatus = EnumProviderAssetDraftStatus.PENDING_PROVIDER_REVIEW;

        if (draft == null) {
            throw new AssetDraftException(AssetMessageCode.DRAFT_NOT_FOUND);
        }
        if (draft.getStatus() != currStatus) {
            throw new AssetDraftException(
                AssetMessageCode.INVALID_STATE,
                String.format("Expected status [%s]. Found [%s]", currStatus, draft.getStatus())
            );
        }

        draft.setModifiedOn(ZonedDateTime.now());
        draft.setStatus(nextStatus);

        return nextStatus;
    }

    @Transactional(readOnly = false)
    default EnumProviderAssetDraftStatus acceptProvider(UUID publisherKey, UUID draftKey) throws AssetDraftException {
        final ProviderAssetDraftEntity     draft     = this.findOneByPublisherAndKey(publisherKey, draftKey).orElse(null);
        final EnumProviderAssetDraftStatus currStatus = EnumProviderAssetDraftStatus.PENDING_PROVIDER_REVIEW;
        final EnumProviderAssetDraftStatus nextStatus = EnumProviderAssetDraftStatus.POST_PROCESSING;

        if (draft == null) {
            throw new AssetDraftException(AssetMessageCode.DRAFT_NOT_FOUND);
        }
        if (draft.getStatus() != currStatus) {
            throw new AssetDraftException(
                AssetMessageCode.INVALID_STATE,
                String.format("Expected status [%s]. Found [%s]", currStatus, draft.getStatus())
            );
        }

        draft.setModifiedOn(ZonedDateTime.now());
        draft.setStatus(nextStatus);

        return nextStatus;
    }

    @Transactional(readOnly = false)
    default EnumProviderAssetDraftStatus rejectHelpDesk(UUID publisherKey, UUID draftKey, String reason) throws AssetDraftException {
        final ProviderAssetDraftEntity     draft     = this.findOneByPublisherAndKey(publisherKey, draftKey).orElse(null);
        final EnumProviderAssetDraftStatus currStatus = EnumProviderAssetDraftStatus.PENDING_HELPDESK_REVIEW;
        final EnumProviderAssetDraftStatus nextStatus = EnumProviderAssetDraftStatus.HELPDESK_REJECTED;

        if (draft == null) {
            throw new AssetDraftException(AssetMessageCode.DRAFT_NOT_FOUND);
        }
        if (draft.getStatus() != currStatus) {
            throw new AssetDraftException(
                AssetMessageCode.INVALID_STATE,
                String.format("Expected status [%s]. Found [%s]", currStatus, draft.getStatus())
            );
        }

        draft.setModifiedOn(ZonedDateTime.now());
        draft.setStatus(nextStatus);
        draft.setHelpdeskRejectionReason(reason);

        return nextStatus;
    }

    @Transactional(readOnly = false)
    default EnumProviderAssetDraftStatus rejectProvider(UUID publisherKey, UUID draftKey, String reason) throws AssetDraftException {
        final ProviderAssetDraftEntity draft = this.findOneByPublisherAndKey(publisherKey, draftKey).orElse(null);
        final EnumProviderAssetDraftStatus currStatus = EnumProviderAssetDraftStatus.PENDING_PROVIDER_REVIEW;
        final EnumProviderAssetDraftStatus nextStatus = EnumProviderAssetDraftStatus.PROVIDER_REJECTED;

        if (draft == null) {
            throw new AssetDraftException(AssetMessageCode.DRAFT_NOT_FOUND);
        }
        if (draft.getStatus() != currStatus) {
            throw new AssetDraftException(
                AssetMessageCode.INVALID_STATE,
                String.format("Expected status [%s]. Found [%s]", currStatus, draft.getStatus())
            );
        }

        draft.setModifiedOn(ZonedDateTime.now());
        draft.setStatus(nextStatus);
        draft.setProviderRejectionReason(reason);

        return nextStatus;
    }

    @Transactional(readOnly = false)
    default void publish(UUID publisherKey, UUID draftKey, String pid) throws AssetDraftException {
        final ProviderAssetDraftEntity draft = this.findOneByPublisherAndKey(publisherKey, draftKey).orElse(null);

        if (draft == null) {
            throw new AssetDraftException(AssetMessageCode.DRAFT_NOT_FOUND);
        }

        // Set modified on only the first time the status changes
        if (draft.getStatus() != EnumProviderAssetDraftStatus.PUBLISHED) {
            draft.setModifiedOn(ZonedDateTime.now());
        }

        draft.setAssetPublished(pid);
        draft.setStatus(EnumProviderAssetDraftStatus.PUBLISHED);
    }

    @Transactional(readOnly = false)
    default void updateStatus(UUID publisherKey, UUID draftKey, EnumProviderAssetDraftStatus status) throws AssetDraftException {
        final ProviderAssetDraftEntity draft = this.findOneByPublisherAndKey(publisherKey, draftKey).orElse(null);

        if (draft == null) {
            throw new AssetDraftException(AssetMessageCode.DRAFT_NOT_FOUND);
        }

        // Set modified on only the first time the status changes
        if (draft.getStatus() != status) {
            draft.setModifiedOn(ZonedDateTime.now());
        }

        draft.setStatus(status);
    }

    @Transactional(readOnly = false)
    default void delete(UUID publisherKey, UUID assetKey) {
        Assert.notNull(publisherKey, "Expected a non-null publisher key");
        Assert.notNull(assetKey, "Expected a non-null asset key");

        // Check draft
        final ProviderAssetDraftEntity draft = this.findOneByPublisherAndKey(publisherKey, assetKey).orElse(null);

        if (draft == null) {
            throw new AssetDraftException(AssetMessageCode.DRAFT_NOT_FOUND);
        }

        this.delete(draft);
    }

}
