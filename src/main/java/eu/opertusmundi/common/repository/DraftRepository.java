package eu.opertusmundi.common.repository;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.locationtech.jts.geom.Geometry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.fasterxml.jackson.databind.JsonNode;

import eu.opertusmundi.common.domain.AccountEntity;
import eu.opertusmundi.common.domain.HelpdeskAccountEntity;
import eu.opertusmundi.common.domain.ProviderAssetDraftEntity;
import eu.opertusmundi.common.model.Message;
import eu.opertusmundi.common.model.asset.AssetDraftDto;
import eu.opertusmundi.common.model.asset.AssetDraftReviewCommandDto;
import eu.opertusmundi.common.model.asset.AssetMessageCode;
import eu.opertusmundi.common.model.asset.EnumProviderAssetDraftStatus;
import eu.opertusmundi.common.model.asset.EnumResourceType;
import eu.opertusmundi.common.model.asset.ExternalUrlResourceDto;
import eu.opertusmundi.common.model.asset.FileResourceDto;
import eu.opertusmundi.common.model.asset.ResourceDto;
import eu.opertusmundi.common.model.asset.ServiceResourceDto;
import eu.opertusmundi.common.model.catalogue.client.CatalogueItemCommandDto;
import eu.opertusmundi.common.model.catalogue.client.CatalogueItemMetadataCommandDto;
import eu.opertusmundi.common.model.catalogue.client.EnumAssetType;
import eu.opertusmundi.common.model.catalogue.client.EnumSpatialDataServiceType;
import eu.opertusmundi.common.model.ingest.ResourceIngestionDataDto;
import eu.opertusmundi.common.model.ingest.ServerIngestPublishResponseDto;
import eu.opertusmundi.common.service.AssetDraftException;

@Repository
@Transactional(readOnly = true)
public interface DraftRepository extends JpaRepository<ProviderAssetDraftEntity, Integer> {

    @Query("SELECT a FROM Account a WHERE a.key = :key")
    Optional<AccountEntity> findAccountByKey(UUID key);

    @Query("SELECT a FROM HelpdeskAccount a WHERE a.key = :key")
    Optional<HelpdeskAccountEntity> findHelpdeskAccountByKey(UUID key);

    @Query("SELECT d FROM ProviderAssetDraft d WHERE "
           + "(d.status in :includeStatus or :includeStatus is null) and "
           + "(not d.status in :excludeStatus or :excludeStatus is null) and "
           + "(d.account.key = :publisherKey or CAST(:publisherKey as org.hibernate.type.UUIDCharType) is null) and "
           + "(d.type in :type or :type is null) and "
           + "(d.serviceType in :serviceType or :serviceType is null) "
    )
    Page<ProviderAssetDraftEntity> findAllByPublisherAndStatus(
        UUID publisherKey,
        Set<EnumProviderAssetDraftStatus> includeStatus,
        Set<EnumProviderAssetDraftStatus> excludeStatus,
        Set<EnumAssetType> type,
        Set<EnumSpatialDataServiceType> serviceType,
        Pageable pageable
    );

    default Page<AssetDraftDto> findAllObjectsByPublisherAndStatus(
        UUID publisherKey,
        Set<EnumProviderAssetDraftStatus> includeStatus,
        Set<EnumProviderAssetDraftStatus> excludeStatus,
        Set<EnumAssetType> type,
        Set<EnumSpatialDataServiceType> serviceType,
        Pageable pageable
    ) {
        final var page = this.findAllByPublisherAndStatus(
            publisherKey, includeStatus, excludeStatus, type, serviceType, pageable
        ).map(ProviderAssetDraftEntity::toDto);
        return page;
    }

    @Query("SELECT d FROM ProviderAssetDraft d WHERE "
            + "(d.status in :includeStatus or :includeStatus is null) and "
            + "(not d.status in :excludeStatus or :excludeStatus is null) and "
            + "(d.vendorAccount.key = :ownerKey) and "
            + "(d.account.key = :publisherKey) and "
            + "(d.type in :type or :type is null) and "
            + "(d.serviceType in :serviceType or :serviceType is null) "
     )
     Page<ProviderAssetDraftEntity> findAllByOwnerAndPublisherAndStatus(
         UUID ownerKey,
         UUID publisherKey,
         Set<EnumProviderAssetDraftStatus> includeStatus,
         Set<EnumProviderAssetDraftStatus> excludeStatus,
         Set<EnumAssetType> type,
         Set<EnumSpatialDataServiceType> serviceType,
         Pageable pageable
     );

    default Page<AssetDraftDto> findAllObjectsByOwnerAndPublisherAndStatus(
         UUID ownerKey,
         UUID publisherKey,
         Set<EnumProviderAssetDraftStatus> includeStatus,
         Set<EnumProviderAssetDraftStatus> excludeStatus,
         Set<EnumAssetType> type,
         Set<EnumSpatialDataServiceType> serviceType,
         Pageable pageable
    ) {
        final var page = this.findAllByOwnerAndPublisherAndStatus(
            ownerKey, publisherKey, includeStatus, excludeStatus, type, serviceType, pageable
        ).map(ProviderAssetDraftEntity::toDto);
        return page;
    }

    @Query("SELECT a FROM ProviderAssetDraft a WHERE "
           + "(a.parentId = :parentId) and "
           + "(not status in ('HELPDESK_REJECTED', 'PROVIDER_REJECTED', 'PUBLISHED', 'CANCELLED'))")
    List<ProviderAssetDraftEntity> findAllByParentId(String parentId);

    @Query("SELECT a FROM ProviderAssetDraft a WHERE a.account.key = :publisherKey")
    Page<ProviderAssetDraftEntity> findAllByPublisher(
        @Param("publisherKey") UUID publisherKey, Pageable pageable
    );

    @Query("SELECT a FROM ProviderAssetDraft a WHERE a.status in :status")
    Page<ProviderAssetDraftEntity> findAllByStatus(
        @Param("status") Set<EnumProviderAssetDraftStatus> status, Pageable pageable
    );

    @Query("SELECT a FROM ProviderAssetDraft a WHERE a.key = :draftKey and a.account.key = :publisherKey")
    Optional<ProviderAssetDraftEntity> findOneByPublisherAndKey(UUID publisherKey, UUID draftKey);

    @Query("SELECT a FROM ProviderAssetDraft a WHERE a.key = :key and a.account.key = :publisherKey and a.vendorAccount.key = :ownerKey")
    Optional<ProviderAssetDraftEntity> findOneByOwnerAndPublisherAndKey(
        @Param("ownerKey") UUID ownerKey, @Param("publisherKey") UUID publisherKey, @Param("key") UUID assetKey
    );

    @Query("SELECT a FROM ProviderAssetDraft a WHERE a.key = :key")
    Optional<ProviderAssetDraftEntity> findOneByKey(UUID key);

    @Query("SELECT a FROM ProviderAssetDraft a WHERE a.key = :key")
    default AssetDraftDto findOneObjectByKey(UUID key) {
        return this.findOneByKey(key).map(e -> e.toDto(true)).orElse(null);
    }

    @Query("SELECT a.id FROM ProviderAssetDraft a WHERE a.key = :key")
    Integer getIdFromKey(UUID key);

    @Transactional(readOnly = false)
    default AssetDraftDto update(CatalogueItemCommandDto command) throws AssetDraftException {
        return this.update(command, null, null, null);
    }

    @Transactional(readOnly = false)
    default AssetDraftDto update(
        CatalogueItemCommandDto command,
        EnumProviderAssetDraftStatus status,
        String processDefinition,
        String processInstance
    ) throws AssetDraftException {
        return this.update(command, status, processDefinition, processInstance, false);
    }

    @Transactional(readOnly = false)
    default AssetDraftDto update(
        CatalogueItemCommandDto command,
        EnumProviderAssetDraftStatus status,
        String processDefinition,
        String processInstance,
        boolean resetErrors
    ) throws AssetDraftException {
        Assert.notNull(command, "Expected a non-null command");

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
        if (status == null) {
            status = draft.getStatus();
        }
        Assert.isTrue(
            (status == EnumProviderAssetDraftStatus.DRAFT) ||
            (draft.getType() != EnumAssetType.BUNDLE && status == EnumProviderAssetDraftStatus.SUBMITTED) ||
            (draft.getType() == EnumAssetType.BUNDLE && status == EnumProviderAssetDraftStatus.PENDING_HELPDESK_REVIEW),
            "Expected status in [DRAFT, SUBMITTED, PENDING_HELPDESK_REVIEW]"
        );

        if (draft.getStatus() != EnumProviderAssetDraftStatus.DRAFT && draft.getStatus() != EnumProviderAssetDraftStatus.SUBMITTED) {
            throw new AssetDraftException(
                AssetMessageCode.INVALID_STATE,
                String.format("Expected status is [%s]. Found [%s]", status, draft.getStatus())
            );
        }

        draft.setAccount(account);
        draft.setCommand(command);
        draft.setDataProfilingEnabled(command.isDataProfilingEnabled());
        draft.setIprProtectionEnabled(command.isIprProtectionEnabled());

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

        if (resetErrors) {
            draft.setWorkflowErrorDetails(null);
            draft.setWorkflowErrorMessages(null);

            draft.setHelpdeskErrorMessage(null);
            draft.setHelpdeskSetErrorAccount(null);

            draft.setHelpdeskReviewAccount(null);
            draft.setProviderReviewAccount(null);
        }

        this.saveAndFlush(draft);

        return draft.toDto();
    }


    @Transactional(readOnly = false)
    default AssetDraftDto update(CatalogueItemMetadataCommandDto command) throws AssetDraftException {
        Assert.notNull(command, "Expected a non-null command");

        final ZonedDateTime now = ZonedDateTime.now();

        // Check draft
        final ProviderAssetDraftEntity draft = this.findOneByPublisherAndKey(command.getPublisherKey(), command.getDraftKey()).orElse(null);
        if (draft == null) {
            throw new AssetDraftException(AssetMessageCode.DRAFT_NOT_FOUND);
        }

        /*
         * Update metadata only when status is:
         *
         * PENDING_PROVIDER_REVIEW  : Provider update during review
         * POST_PROCESSING          : Sample generation before publishing
         */
        if (draft.getStatus() != EnumProviderAssetDraftStatus.PENDING_PROVIDER_REVIEW &&
            draft.getStatus() != EnumProviderAssetDraftStatus.POST_PROCESSING
        ) {
            throw new AssetDraftException(
                AssetMessageCode.INVALID_STATE,
                String.format("Expected status in [PENDING_PROVIDER_REVIEW, POST_PROCESSING]. Found [%s]", draft.getStatus())
            );
        }
        if (command.getVisibility() != null) {
            draft.getCommand().setVisibility(command.getVisibility());
        }
        if (command.getSampleAreas() != null) {
            draft.getCommand().addServiceResourceSampleAreas(command.getResourceKey().toString(), command.getSampleAreas());
        }
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
            draft.setComputedGeometry(true);
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

        // Get ingestion data for the specified resource
        final ResourceIngestionDataDto ingestionData = draft.getCommand().getIngestionInfo().stream()
            .filter(i -> i.getKey().equals(resourceKey))
            .findFirst()
            .get();

        if (!StringUtils.isBlank(data.getWmsDescribeLayer())) {
            ingestionData.addEndpoint(EnumSpatialDataServiceType.WMS, data.getWmsDescribeLayer());
        }
        if (!StringUtils.isBlank(data.getWfsDescribeFeatureType())) {
            ingestionData.addEndpoint(EnumSpatialDataServiceType.WFS, data.getWfsDescribeFeatureType());
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
    default AssetDraftDto addResource(
        UUID publisherKey, UUID draftKey, ResourceDto resource
    ) throws AssetDraftException {
        final ProviderAssetDraftEntity draft = this.findOneByPublisherAndKey(publisherKey, draftKey).orElse(null);
        if (draft == null) {
            throw new AssetDraftException(AssetMessageCode.DRAFT_NOT_FOUND);
        }

        final EnumProviderAssetDraftStatus expectedStatus = resource.getType() == EnumResourceType.EXTERNAL_URL
            ? EnumProviderAssetDraftStatus.DRAFT
            : EnumProviderAssetDraftStatus.SUBMITTED;

        if (draft.getStatus() != expectedStatus) {
            throw new AssetDraftException(
                    AssetMessageCode.INVALID_STATE,
                String.format("Invalid draft status [expected=%s, found=%s", expectedStatus, draft.getStatus())
            );
        }

        if (resource instanceof final ServiceResourceDto s) {
            draft.getCommand().addServiceResource(s);
        }
        if (resource instanceof final ExternalUrlResourceDto u) {
            // Always override category from draft
            u.setCategory(draft.getType());
            draft.getCommand().addExternalUrlResource(u);
        }
        if (resource instanceof final FileResourceDto f && f.getParentId() != null) {
            final var parent = draft.getCommand().getResources().stream()
                .filter(r -> r.getId().equals(f.getParentId()))
                .findFirst()
                .orElse(null);
            if (parent == null) {
                throw new AssetDraftException(
                    AssetMessageCode.INVALID_STATE,
                    String.format("File resource parent was not found [parent=%s]", f.getParentId())
                );
            }
            draft.getCommand().addExternalUrlFileResource(f);
        }

        // NOTE: Workaround for updating ingestion data. Property command of entity
        // ProviderAssetDraftEntity is annotated with @Convert for serializing a
        // CatalogueItemCommandDto instance to JSON; Hence updating only the metadata
        // nested property wont trigger an update and convertToDatabaseColumn will not be
        // invoked.
        draft.setModifiedOn(ZonedDateTime.now());

        return this.saveAndFlush(draft).toDto();
    }

    @Transactional(readOnly = false)
    default EnumProviderAssetDraftStatus acceptHelpDesk(AssetDraftReviewCommandDto command) throws AssetDraftException {
        final var draft         = this.findOneByPublisherAndKey(command.getPublisherKey(), command.getDraftKey()).orElse(null);
        final var helpdeskUser  = this.findHelpdeskAccountByKey(command.getReviewerKey()).get();
        final var currentStatus = EnumProviderAssetDraftStatus.PENDING_HELPDESK_REVIEW;
        final var nextStatus    = EnumProviderAssetDraftStatus.PENDING_PROVIDER_REVIEW;

        if (draft == null) {
            throw new AssetDraftException(AssetMessageCode.DRAFT_NOT_FOUND);
        }
        if (draft.getStatus() != currentStatus) {
            throw new AssetDraftException(
                AssetMessageCode.INVALID_STATE,
                String.format("Expected status [%s]. Found [%s]", currentStatus, draft.getStatus())
            );
        }

        draft.setHelpdeskReviewAccount(helpdeskUser);
        draft.setModifiedOn(ZonedDateTime.now());
        draft.setStatus(nextStatus);

        return nextStatus;
    }

    @Transactional(readOnly = false)
    default EnumProviderAssetDraftStatus acceptProvider(AssetDraftReviewCommandDto command) throws AssetDraftException {
        final var draft           = this.findOneByPublisherAndKey(command.getPublisherKey(), command.getDraftKey()).orElse(null);
        final var marketplaceUser = this.findAccountByKey(command.getReviewerKey()).get();
        final var currentStatus   = EnumProviderAssetDraftStatus.PENDING_PROVIDER_REVIEW;
        final var nextStatus      = EnumProviderAssetDraftStatus.POST_PROCESSING;

        if (draft == null) {
            throw new AssetDraftException(AssetMessageCode.DRAFT_NOT_FOUND);
        }
        if (draft.getStatus() != currentStatus) {
            throw new AssetDraftException(
                AssetMessageCode.INVALID_STATE,
                String.format("Expected status [%s]. Found [%s]", currentStatus, draft.getStatus())
            );
        }

        draft.setModifiedOn(ZonedDateTime.now());
        draft.setProviderReviewAccount(marketplaceUser);
        draft.setStatus(nextStatus);

        return nextStatus;
    }

    @Transactional(readOnly = false)
    default EnumProviderAssetDraftStatus rejectHelpDesk(AssetDraftReviewCommandDto command) throws AssetDraftException {
        final var draft         = this.findOneByPublisherAndKey(command.getPublisherKey(), command.getDraftKey()).orElse(null);
        final var helpdeskUser  = this.findHelpdeskAccountByKey(command.getReviewerKey()).get();
        final var currentStatus = EnumProviderAssetDraftStatus.PENDING_HELPDESK_REVIEW;
        final var nextStatus    = EnumProviderAssetDraftStatus.HELPDESK_REJECTED;

        if (draft == null) {
            throw new AssetDraftException(AssetMessageCode.DRAFT_NOT_FOUND);
        }
        if (draft.getStatus() != currentStatus) {
            throw new AssetDraftException(
                AssetMessageCode.INVALID_STATE,
                String.format("Expected status [%s]. Found [%s]", currentStatus, draft.getStatus())
            );
        }

        draft.setHelpdeskReviewAccount(helpdeskUser);
        draft.setModifiedOn(ZonedDateTime.now());
        draft.setStatus(nextStatus);
        draft.setHelpdeskRejectionReason(command.getReason());

        return nextStatus;
    }

    @Transactional(readOnly = false)
    default EnumProviderAssetDraftStatus rejectProvider(AssetDraftReviewCommandDto command) throws AssetDraftException {
        final var draft           = this.findOneByPublisherAndKey(command.getPublisherKey(), command.getDraftKey()).orElse(null);
        final var marketplaceUser = this.findAccountByKey(command.getReviewerKey()).get();
        final var currentStatus   = EnumProviderAssetDraftStatus.PENDING_PROVIDER_REVIEW;
        final var nextStatus      = EnumProviderAssetDraftStatus.PROVIDER_REJECTED;

        if (draft == null) {
            throw new AssetDraftException(AssetMessageCode.DRAFT_NOT_FOUND);
        }
        if (draft.getStatus() != currentStatus) {
            throw new AssetDraftException(
                AssetMessageCode.INVALID_STATE,
                String.format("Expected status [%s]. Found [%s]", currentStatus, draft.getStatus())
            );
        }

        draft.setModifiedOn(ZonedDateTime.now());
        draft.setProviderRejectionReason(command.getReason());
        draft.setProviderReviewAccount(marketplaceUser);
        draft.setStatus(nextStatus);

        return nextStatus;
    }

    @Transactional(readOnly = false)
    default void setErrorMessage(UUID helpdeskUserKey, UUID publisherKey, UUID draftKey, String message) throws AssetDraftException {
        final var draft        = this.findOneByPublisherAndKey(publisherKey, draftKey).orElse(null);
        final var helpdeskUser = this.findHelpdeskAccountByKey(helpdeskUserKey).get();
        if (draft == null) {
            throw new AssetDraftException(AssetMessageCode.DRAFT_NOT_FOUND);
        }
        if (draft.getStatus() == EnumProviderAssetDraftStatus.DRAFT ||
            draft.getStatus() == EnumProviderAssetDraftStatus.PUBLISHED
        ) {
            throw new AssetDraftException(
                AssetMessageCode.INVALID_STATE,
                String.format("Invalid draft status found. [status=%s]", draft.getStatus())
            );
        }
        draft.setHelpdeskSetErrorAccount(helpdeskUser);
        draft.setModifiedOn(ZonedDateTime.now());
        draft.setHelpdeskErrorMessage(message);

        this.saveAndFlush(draft);
    }

    @Transactional(readOnly = false)
    default void resetDraft(UUID publisherKey, UUID draftKey, String errorDetails, List<Message> errorMessages) throws AssetDraftException {
        final ProviderAssetDraftEntity draft = this.findOneByPublisherAndKey(publisherKey, draftKey).orElse(null);

        if (draft == null) {
            throw new AssetDraftException(AssetMessageCode.DRAFT_NOT_FOUND);
        }
        if (draft.getStatus() == EnumProviderAssetDraftStatus.DRAFT ||
            draft.getStatus() == EnumProviderAssetDraftStatus.PUBLISHED
        ) {
            throw new AssetDraftException(
                AssetMessageCode.INVALID_STATE,
                String.format("Invalid draft status found. [status=%s]", draft.getStatus())
            );
        }

        draft.setModifiedOn(ZonedDateTime.now());
        draft.setStatus(EnumProviderAssetDraftStatus.DRAFT);
        draft.setWorkflowErrorDetails(errorDetails);
        draft.setWorkflowErrorMessages(errorMessages);

        // Reset profile data
        draft.getCommand().setAutomatedMetadata(null);
        if (draft.isComputedGeometry()) {
            draft.getCommand().setGeometry(null);
            draft.setComputedGeometry(false);
        }
        // Reset ingest data
        draft.getCommand().setIngestionInfo(null);
        // Reset resources
        final var initialResources = draft.getCommand().getResources();
        final var resources = initialResources.stream()
            .filter(r -> {
                switch(r.getType()) {
                   case ASSET : {
                       return true;
                   }
                   case FILE : {
                        final var parent = initialResources.stream()
                            .filter(ir -> ir.getId().equals(r.getParentId()))
                            .findFirst()
                            .orElse(null);

                        // Ignore file resources that are from external URLs.
                        // The publish workflow will download these files again.
                        return parent == null || parent.getType() != EnumResourceType.EXTERNAL_URL;
                    }
                   case EXTERNAL_URL : {
                       return true;
                   }
                   case SERVICE : {
                        // Service resources are auto-generated by the BPM
                        // worker during the get capabilities task service;
                        // Hence they are always ignored
                        return false;
                    }
                   default : {
                       throw new AssetDraftException(
                           AssetMessageCode.RESOURCE_TYPE_NOT_SUPPORTED,
                           String.format("Resource type is not supported [type=%s]", r.getType())
                       );
                    }
                }
            })
            .toList();

        draft.getCommand().setResources(resources);

        this.saveAndFlush(draft);
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
