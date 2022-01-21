package eu.opertusmundi.common.service;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.camunda.bpm.engine.rest.dto.VariableValueDto;
import org.camunda.bpm.engine.rest.dto.runtime.ProcessInstanceDto;
import org.camunda.bpm.engine.rest.dto.task.TaskDto;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.io.WKTReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import eu.opertusmundi.common.domain.AccountEntity;
import eu.opertusmundi.common.domain.AssetAdditionalResourceEntity;
import eu.opertusmundi.common.domain.AssetFileTypeEntity;
import eu.opertusmundi.common.domain.AssetMetadataPropertyEntity;
import eu.opertusmundi.common.domain.AssetResourceEntity;
import eu.opertusmundi.common.domain.ProviderAssetDraftEntity;
import eu.opertusmundi.common.domain.RecordLockEntity;
import eu.opertusmundi.common.model.EnumLockResult;
import eu.opertusmundi.common.model.EnumRecordLock;
import eu.opertusmundi.common.model.EnumSortingOrder;
import eu.opertusmundi.common.model.PageResultDto;
import eu.opertusmundi.common.model.RecordLockDto;
import eu.opertusmundi.common.model.asset.AssetAdditionalResourceDto;
import eu.opertusmundi.common.model.asset.AssetDraftDto;
import eu.opertusmundi.common.model.asset.AssetDraftReviewCommandDto;
import eu.opertusmundi.common.model.asset.AssetDraftSetStatusCommandDto;
import eu.opertusmundi.common.model.asset.AssetFileAdditionalResourceCommandDto;
import eu.opertusmundi.common.model.asset.AssetFileAdditionalResourceDto;
import eu.opertusmundi.common.model.asset.AssetMessageCode;
import eu.opertusmundi.common.model.asset.AssetRepositoryException;
import eu.opertusmundi.common.model.asset.EnumAssetAdditionalResource;
import eu.opertusmundi.common.model.asset.EnumMetadataPropertyType;
import eu.opertusmundi.common.model.asset.EnumProviderAssetDraftSortField;
import eu.opertusmundi.common.model.asset.EnumProviderAssetDraftStatus;
import eu.opertusmundi.common.model.asset.EnumProviderSubSortField;
import eu.opertusmundi.common.model.asset.EnumResourceType;
import eu.opertusmundi.common.model.asset.FileResourceCommandDto;
import eu.opertusmundi.common.model.asset.FileResourceDto;
import eu.opertusmundi.common.model.asset.MetadataProperty;
import eu.opertusmundi.common.model.asset.ResourceDto;
import eu.opertusmundi.common.model.asset.ServiceResourceDto;
import eu.opertusmundi.common.model.asset.UserFileResourceCommandDto;
import eu.opertusmundi.common.model.catalogue.CatalogueServiceException;
import eu.opertusmundi.common.model.catalogue.CatalogueServiceMessageCode;
import eu.opertusmundi.common.model.catalogue.client.CatalogueHarvestImportCommandDto;
import eu.opertusmundi.common.model.catalogue.client.CatalogueItemCommandDto;
import eu.opertusmundi.common.model.catalogue.client.CatalogueItemDetailsDto;
import eu.opertusmundi.common.model.catalogue.client.CatalogueItemMetadataCommandDto;
import eu.opertusmundi.common.model.catalogue.client.DraftApiCommandDto;
import eu.opertusmundi.common.model.catalogue.client.DraftApiFromAssetCommandDto;
import eu.opertusmundi.common.model.catalogue.client.DraftApiFromFileCommandDto;
import eu.opertusmundi.common.model.catalogue.client.DraftFromAssetCommandDto;
import eu.opertusmundi.common.model.catalogue.client.EnumAssetType;
import eu.opertusmundi.common.model.catalogue.client.EnumDeliveryMethod;
import eu.opertusmundi.common.model.catalogue.client.EnumSpatialDataServiceType;
import eu.opertusmundi.common.model.catalogue.client.UnpublishAssetCommand;
import eu.opertusmundi.common.model.catalogue.server.CatalogueFeature;
import eu.opertusmundi.common.model.contract.provider.ProviderTemplateContractHistoryDto;
import eu.opertusmundi.common.model.file.FilePathCommand;
import eu.opertusmundi.common.model.file.FileSystemException;
import eu.opertusmundi.common.model.file.FileSystemMessageCode;
import eu.opertusmundi.common.model.ingest.ResourceIngestionDataDto;
import eu.opertusmundi.common.model.ingest.ServerIngestPublishResponseDto;
import eu.opertusmundi.common.model.ingest.ServerIngestResultResponseDto;
import eu.opertusmundi.common.model.payment.provider.ProviderAccountSubscriptionDto;
import eu.opertusmundi.common.model.workflow.EnumProcessInstanceVariable;
import eu.opertusmundi.common.repository.AccountRepository;
import eu.opertusmundi.common.repository.AccountSubscriptionRepository;
import eu.opertusmundi.common.repository.AssetAdditionalResourceRepository;
import eu.opertusmundi.common.repository.AssetFileTypeRepository;
import eu.opertusmundi.common.repository.AssetMetadataPropertyRepository;
import eu.opertusmundi.common.repository.AssetResourceRepository;
import eu.opertusmundi.common.repository.DraftRepository;
import eu.opertusmundi.common.repository.RecordLockRepository;
import eu.opertusmundi.common.repository.contract.ProviderTemplateContractHistoryRepository;
import eu.opertusmundi.common.util.BpmEngineUtils;
import eu.opertusmundi.common.util.BpmInstanceVariablesBuilder;
import feign.FeignException;

// TODO: Scheduler job for deleting orphaned resources

@Service
public class DefaultProviderAssetService implements ProviderAssetService {

    private static final Logger logger = LoggerFactory.getLogger(DefaultProviderAssetService.class);

    private static final String WORKFLOW_SELL_ASSET = "workflow-provider-publish-asset";

    private static final String WORKFLOW_UNPUBLISH_ASSET = "workflow-provider-remove-asset";

    private static final String TASK_REVIEW = "task-review";

    private static final String MESSAGE_PROVIDER_REVIEW = "provider-publish-asset-user-acceptance-message";

    private static final String METADATA_PROPERTY_MBR = "mbr";

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AssetFileTypeRepository assetFileTypeRepository;

    @Autowired
    private AssetMetadataPropertyRepository assetMetadataPropertyRepository;

    @Autowired
    private RecordLockRepository recordLockRepository;

    @Autowired
    private DraftRepository draftRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private AssetResourceRepository assetResourceRepository;

    @Autowired
    private AssetAdditionalResourceRepository assetAdditionalResourceRepository;

    @Autowired
    private AccountSubscriptionRepository subscriptionRepository;

    @Autowired
    private ProviderTemplateContractHistoryRepository contractRepository;

    @Autowired
    private UserFileManager userFileManager;

    @Autowired
    private DraftFileManager draftFileManager;

    @Autowired
    private AssetFileManager assetFileManager;

    @Autowired
    private CatalogueService catalogueService;

    @Autowired
    private PersistentIdentifierService pidService;

    @Autowired
    private BpmEngineUtils bpmEngine;

    @Override
    public PageResultDto<AssetDraftDto> findAllDraft(
        UUID ownerKey, UUID publisherKey,
        Set<EnumProviderAssetDraftStatus> status, Set<EnumAssetType> type, Set<EnumSpatialDataServiceType> serviceType,
        int pageIndex, int pageSize,
        EnumProviderAssetDraftSortField orderBy, EnumSortingOrder order
    ) {
        final Direction   direction   = order == EnumSortingOrder.DESC ? Direction.DESC : Direction.ASC;
        final PageRequest pageRequest = PageRequest.of(pageIndex, pageSize, Sort.by(direction, orderBy.getValue()));

        if (status != null && status.isEmpty()) {
            status = null;
        }
        if (type != null && type.isEmpty()) {
            type = null;
        }
        if (serviceType != null && serviceType.isEmpty()) {
            serviceType = null;
        }

        final Page<ProviderAssetDraftEntity> entities = ownerKey == null || ownerKey.equals(publisherKey)
            ? this.draftRepository.findAllByPublisherAndStatus(publisherKey, status, type, serviceType, pageRequest)
            : this.draftRepository.findAllByOwnerAndPublisherAndStatus(ownerKey, publisherKey, status, type, serviceType, pageRequest);

        final Page<AssetDraftDto> items = entities.map(ProviderAssetDraftEntity::toDto);
        final long                count   = items.getTotalElements();
        final List<AssetDraftDto> records = items.getContent();

        // Get locks
        final List<UUID>          keys  = records.stream().map(r -> r.getKey()).collect(Collectors.toList());
        final List<RecordLockDto> locks = this.recordLockRepository.findAllForDraftsAsObjects(keys);

        records.forEach(r -> {
            final RecordLockDto lock = locks.stream()
                .filter(l -> l.getRecordId() == r.getId())
                .findFirst()
                .orElse(null);
            r.setLock(lock);
        });

        return PageResultDto.of(pageIndex, pageSize, records, count);
    }

    @Override
    @Transactional
    public AssetDraftDto findOneDraft(UUID ownerKey, UUID publisherKey, UUID draftKey, boolean locked) {
        Assert.notNull(ownerKey,     "Expected a non-null owner key");
        Assert.notNull(publisherKey, "Expected a non-null publisher key");
        Assert.notNull(draftKey,     "Expected a non-null draft key");

        final ProviderAssetDraftEntity e = ownerKey.equals(publisherKey)
            ? this.draftRepository.findOneByPublisherAndKey(publisherKey, draftKey).orElse(null)
            : this.draftRepository.findOneByOwnerAndPublisherAndKey(ownerKey, publisherKey, draftKey).orElse(null);

        final AssetDraftDto draft = e != null ? e.toDto() : null;

        // Optionally, lock the record
        if (draft != null) {
            final Pair<EnumLockResult, RecordLockDto> lock = this.getLock(ownerKey, draftKey, locked);
            draft.setLock(lock.getRight());
        }

        return draft;
    }

    @Override
    public AssetDraftDto findOneDraft(UUID draftKey) {
        final ProviderAssetDraftEntity e = this.draftRepository.findOneByKey(draftKey).orElse(null);

        final AssetDraftDto draft = e != null ? e.toDto() : null;

        return draft;
    }

    @Override
    @Transactional
    public AssetDraftDto createApiDraft(DraftApiCommandDto command) throws AssetDraftException {
        switch (command.getType()) {
            case ASSET :
                return createApiDraftFromAsset((DraftApiFromAssetCommandDto) command);

            case FILE :
                return createApiDraftFromFile((DraftApiFromFileCommandDto) command);

            default:
                throw new AssetDraftException(
                    AssetMessageCode.API_COMMAND_NOT_SUPPORTED,
                    String.format("API command type [%s] is not supported", command.getType())
                );
        }
    }

    /**
     * Create one or more drafts by importing records from a harvested catalogue
     *
     * @param command
     * @throws AssetDraftException
     */
    @Override
    @Transactional
    public Map<String, AssetDraftDto> importFromCatalogue(CatalogueHarvestImportCommandDto command) throws AssetDraftException{
        final Map<String, AssetDraftDto> result = new HashMap<>();

        try {
            for (final String id : command.getIds()) {
                final CatalogueFeature        feature      = this.catalogueService.findOneHarvested(id);

                if(feature == null) {
                    throw new AssetDraftException(
                        AssetMessageCode.HARVEST_ITEM_NOT_FOUND,
                        String.format("Cannot find harvested item with id [%s]", id)
                    );
                }

                final CatalogueItemCommandDto draftCommand = new CatalogueItemCommandDto(feature);

                draftCommand.setAutomatedMetadata(null);
                draftCommand.setIngested(false);
                draftCommand.setIngestionInfo(null);
                draftCommand.setPublisherKey(command.getPublisherKey());
                draftCommand.setTitle(feature.getProperties().getTitle());
                // TODO: Set default version number
                draftCommand.setVersion(Optional.ofNullable(feature.getProperties().getVersion()).orElse("1"));

                final AssetDraftDto draft = this.updateDraft(draftCommand);

                result.put(id, draft);
            }

            return result;
        } catch (final AssetDraftException ex) {
            throw ex;
        } catch(final Exception ex) {
            throw new AssetDraftException(AssetMessageCode.ERROR, "Failed to create draft from harvested asset", ex);
        }
    }

    private AssetDraftDto createApiDraftFromAsset(DraftApiFromAssetCommandDto command) throws AssetDraftException {
        try {
            final CatalogueFeature feature = this.catalogueService.findOneFeature(command.getPid());

            // Feature must exist
            if(feature == null) {
                throw new AssetDraftException(
                    AssetMessageCode.API_COMMAND_ASSET_NOT_FOUND,
                    String.format("Cannot find asset with PID [%s]", command.getPid())
                );
            }
            // Feature asset type must be VECTOR
            final EnumAssetType sourceAssetType = EnumAssetType.fromString(feature.getProperties().getType());
            if (sourceAssetType != EnumAssetType.VECTOR) {
                throw new AssetDraftException(
                    AssetMessageCode.API_COMMAND_ASSET_TYPE_NOT_SUPPORTED,
                    String.format("Asset type [%s] not supported for service creation", sourceAssetType)
                );
            }
            // Publisher must own the asset
            if (!command.getPublisherKey().equals(feature.getProperties().getPublisherId())) {
                throw new AssetDraftException(
                    AssetMessageCode.API_COMMAND_ASSET_ACCESS_DENIED,
                    String.format("Provider does not own asset with PID [%s]", command.getPid())
                );
            }

            // Create draft
            final CatalogueItemCommandDto draftCommand = new CatalogueItemCommandDto(feature);

            draftCommand.setAutomatedMetadata(null);
            draftCommand.setDeliveryMethod(EnumDeliveryMethod.DIGITAL_PLATFORM);
            draftCommand.setIngested(true);
            draftCommand.setIngestionInfo(null);
            draftCommand.setOwnerKey(command.getOwnerKey());
            draftCommand.setParentDataSourceId(command.getPid());
            draftCommand.setParentId(null);
            draftCommand.setPublisherKey(command.getPublisherKey());
            draftCommand.setSpatialDataServiceType(command.getServiceType());
            draftCommand.setTitle(command.getTitle());
            draftCommand.setType(EnumAssetType.SERVICE);
            draftCommand.setVersion(command.getVersion());

            AssetDraftDto draft = this.updateDraft(draftCommand);

            // Copy resources for new draft
            final List<AssetResourceEntity> resources = this.assetResourceRepository
                .findAllResourcesByAssetPid(feature.getId());

            for (final AssetResourceEntity r : resources) {
                final FileResourceCommandDto resourceCommand = new FileResourceCommandDto();

                resourceCommand.setCategory(r.getCategory());
                resourceCommand.setDraftKey(draft.getKey());
                resourceCommand.setFileName(r.getFileName());
                resourceCommand.setFormat(r.getFormat());
                resourceCommand.setOwnerKey(command.getOwnerKey());
                resourceCommand.setPublisherKey(command.getPublisherKey());
                resourceCommand.setSize(r.getSize());

                final Path resourcePath = this.assetFileManager.resolveResourcePath(command.getPid(), r.getFileName());
                try (final InputStream input = Files.newInputStream(resourcePath)) {
                    draft = this.addFileResource(resourceCommand, input);
                } catch(final Exception ex) {
                    throw new AssetDraftException(
                        AssetMessageCode.API_COMMAND_RESOURCE_COPY,
                        String.format("Failed to copy resource file [%s]", r.getFileName()), ex
                    );
                }
            }

            // If required, get lock
            if (command.isLocked()) {
                final Pair<EnumLockResult, RecordLockDto> lock = this.getLock(command.getOwnerKey(), draft.getKey(), true);
                draft.setLock(lock.getRight());
            }

            return draft;
        } catch (final CatalogueServiceException ex) {
            throw new AssetDraftException(AssetMessageCode.CATALOGUE_SERVICE, "Failed to create API draft from asset", ex);
        } catch (final AssetDraftException ex) {
            throw ex;
        } catch(final Exception ex) {
            throw new AssetDraftException(AssetMessageCode.ERROR, "Failed to create API draft from asset", ex);
        }
    }

    private AssetDraftDto createApiDraftFromFile(DraftApiFromFileCommandDto command) throws AssetDraftException {
        try {
            // Resolve resource file
            final FilePathCommand fileCommand = FilePathCommand.builder()
                .path(command.getPath())
                .userId(command.getUserId())
                .build();

            final Path resourcePath = this.userFileManager.resolveFilePath(fileCommand);

            // Create draft
            final CatalogueItemCommandDto draftCommand = new CatalogueItemCommandDto();

            draftCommand.setDeliveryMethod(EnumDeliveryMethod.DIGITAL_PLATFORM);
            draftCommand.setFormat(command.getFormat());
            draftCommand.setIngested(true);
            draftCommand.setOwnerKey(command.getOwnerKey());
            draftCommand.setPublisherKey(command.getPublisherKey());
            draftCommand.setSpatialDataServiceType(command.getServiceType());
            draftCommand.setTitle(command.getTitle());
            draftCommand.setType(EnumAssetType.SERVICE);
            draftCommand.setVersion(command.getVersion());

            AssetDraftDto draft = this.updateDraft(draftCommand);

            // Get file format and category
            final AssetFileTypeEntity format = this.assetFileTypeRepository
                .findOneByCategoryAndFormat(EnumAssetType.VECTOR, command.getFormat()).get();

            // Add resource
            final FileResourceCommandDto resourceCommand = new FileResourceCommandDto();

            resourceCommand.setCategory(EnumAssetType.VECTOR);
            resourceCommand.setCrs(command.getCrs());
            resourceCommand.setDraftKey(draft.getKey());
            resourceCommand.setEncoding(command.getEncoding());
            resourceCommand.setFileName(FilenameUtils.getName(command.getPath()));
            resourceCommand.setFormat(format.getFormat());
            resourceCommand.setOwnerKey(command.getOwnerKey());
            resourceCommand.setPublisherKey(command.getPublisherKey());
            resourceCommand.setSize(resourcePath.toFile().length());

            try (final InputStream input = Files.newInputStream(resourcePath)) {
                draft = this.addFileResource(resourceCommand, input);
            } catch(final AssetDraftException ex) {
                throw ex;
            } catch(final Exception ex) {
                throw new AssetDraftException(
                    AssetMessageCode.API_COMMAND_RESOURCE_COPY,
                    String.format("Failed to copy resource file [%s]", command.getPath()), ex
                );
            }

            // If required, get lock
            if (command.isLocked()) {
                final Pair<EnumLockResult, RecordLockDto> lock = this.getLock(command.getOwnerKey(), draft.getKey(), true);
                draft.setLock(lock.getRight());
            }

            return draft;
        } catch (final CatalogueServiceException ex) {
            throw new AssetDraftException(AssetMessageCode.CATALOGUE_SERVICE, "Failed to create API draft from asset", ex);
        } catch (final AssetDraftException ex) {
            throw ex;
        } catch(final Exception ex) {
            throw new AssetDraftException(AssetMessageCode.ERROR, "Failed to create API draft from asset", ex);
        }
    }

    @Override
    @Transactional
    public AssetDraftDto updateDraft(CatalogueItemCommandDto command) throws AssetDraftException {
        Pair<EnumLockResult, RecordLockDto> lock = null;

        // Always lock record for update operations
        if (command.getDraftKey() != null) {
            lock = this.getLock(command.getOwnerKey(), command.getDraftKey(), true);
        }

        final AssetDraftDto draft = this.draftRepository.update(command);

        // For create operations, if a lock is required, get a lock after the
        // insertion of the new draft
        if ((lock == null || lock.getLeft() == EnumLockResult.NONE) && command.isLocked()) {
            lock = this.getLock(command.getOwnerKey(), draft.getKey(), true);
        }

        // For update operations, optionally release the lock
        if (command.getDraftKey() != null && !command.isLocked()) {
            this.releaseLock(command.getOwnerKey(), draft.getKey());
        } else if (lock != null) {
            // Set lock information to result
            draft.setLock(lock.getRight());
        }

        // Consolidate file resources
        this.consolidateResources(draft);

        return draft;
    }

    @Override
    @Transactional
    public AssetDraftDto createDraftFromAsset(DraftFromAssetCommandDto command) throws AssetDraftException {
        Assert.notNull(command, "Expected a non-null command");
        Assert.notNull(command.getPublisherKey(), "Expected a non-null publisher key");
        Assert.isTrue(!StringUtils.isBlank(command.getPid()), "Expected a non-empty pid");

        try {
            final CatalogueFeature feature = this.catalogueService.findOneFeature(command.getPid());

            // TODO: If the feature is not published, check history (add method to catalogue for fetching the latest asset version)
            /*
            if (feature == null) {
                this.catalogueService.findOneHistoryFeature(command.getPid());
            }
            */

            // Feature must exist
            if(feature == null) {
                throw new AssetDraftException(
                    AssetMessageCode.ASSET_NOT_FOUND,
                    String.format("Cannot find asset with PID [%s]", command.getPid())
                );
            }

            // Publisher must own the asset
            if (!command.getPublisherKey().equals(feature.getProperties().getPublisherId())) {
                throw new AssetDraftException(
                    AssetMessageCode.API_COMMAND_ASSET_ACCESS_DENIED,
                    String.format("Provider does not own asset with PID [%s]", command.getPid())
                );
            }

            // Create draft
            final CatalogueItemCommandDto draftCommand = new CatalogueItemCommandDto(feature);

            draftCommand.setAutomatedMetadata(null);
            draftCommand.setIngestionInfo(null);
            draftCommand.setOwnerKey(command.getOwnerKey());
            draftCommand.setParentId(command.getPid());
            draftCommand.setPublisherKey(command.getPublisherKey());
            draftCommand.setTitle(draftCommand.getTitle() + " [Draft]");

            final AssetDraftDto draft = this.updateDraft(draftCommand);

            // If a lock is required for the new record, create one
            if (command.isLocked()) {
                final Pair<EnumLockResult, RecordLockDto> lock = this.getLock(command.getOwnerKey(), draft.getKey(), true);
                draft.setLock(lock.getRight());
            }

            return draft;
        } catch (final AssetDraftException ex) {
            throw ex;
        } catch(final Exception ex) {
            throw new AssetDraftException(AssetMessageCode.ERROR, "Failed to create draft from asset existing asset", ex);
        }
    }

    @Override
    @Transactional
    public AssetDraftDto updateDraftMetadata(CatalogueItemMetadataCommandDto command) throws AssetDraftException {
        Pair<EnumLockResult, RecordLockDto> lock = null;

        try {
            final UUID ownerKey     = command.getOwnerKey();
            final UUID publisherKey = command.getPublisherKey();
            final UUID draftKey     = command.getDraftKey();

            // Check draft and owner
            final ProviderAssetDraftEntity draft = ownerKey.equals(publisherKey)
                ? this.draftRepository.findOneByPublisherAndKey(publisherKey, draftKey).orElse(null)
                : this.draftRepository.findOneByOwnerAndPublisherAndKey(ownerKey, publisherKey, draftKey).orElse(null);

            if (draft == null) {
                throw new AssetDraftException(AssetMessageCode.DRAFT_NOT_FOUND, "Draft not found");
            }

            // Lock record
            lock = this.getLock(ownerKey, draftKey, true);

            // Update metadata properties
            final AssetDraftDto result = this.draftRepository.update(command);

            // Update samples
            if (command.getSamples() != null && !command.getSamples().isNull()) {
                final ResourceDto resource = draft.getCommand().getResources().stream()
                    .filter(r -> r.getId().equals(command.getResourceKey().toString()))
                    .findFirst()
                    .orElse(null);

                if (resource == null) {
                    throw new AssetDraftException(AssetMessageCode.RESOURCE_NOT_FOUND, "Resource not found");
                }

                final String fileName = this.getMetadataPropertyFileName(
                    command.getResourceKey().toString(), "samples", EnumMetadataPropertyType.JSON
                );
                final Path   path     = this.draftFileManager.resolveMetadataPropertyPath(
                    command.getPublisherKey(), command.getDraftKey(), fileName
                );

                try {
                    final String content = this.objectMapper.writeValueAsString(command.getSamples());
                    FileUtils.writeStringToFile(path.toFile(), content, Charset.forName("UTF-8"));
                } catch (final Exception ex) {
                    throw new AssetDraftException(AssetMessageCode.IO_ERROR, "Failed to serialize and persist samples");
                }
            }

            // Set lock information
            if (lock != null && lock.getLeft() == EnumLockResult.EXISTS) {
                result.setLock(lock.getRight());
            }

            return result;
        } finally {
            // Release lock only if it was created in this transaction
            if (lock != null && lock.getLeft() == EnumLockResult.CREATED) {
                this.releaseLock(command.getOwnerKey(), command.getDraftKey());
            }
        }
    }

    @Override
    @Transactional
    public void deleteDraft(UUID ownerKey, UUID publisherKey, UUID draftKey) throws AssetDraftException {
        try {
            this.ensureDraftAndStatus(ownerKey, publisherKey, draftKey, EnumProviderAssetDraftStatus.DRAFT);

            this.getLock(ownerKey, draftKey, true);

            // Delete data in transaction before deleting files
            this.deleteDraftData(ownerKey, publisherKey, draftKey);

            // Delete all files for the selected draft
            this.draftFileManager.deleteAllFiles(publisherKey, draftKey);
        } finally {
            this.releaseLock(ownerKey, draftKey);
        }
    }

    private void deleteDraftData(UUID ownerKey, UUID publisherKey, UUID draftKey) throws AssetDraftException {
        this.ensureDraftAndStatus(ownerKey, publisherKey, draftKey, EnumProviderAssetDraftStatus.DRAFT);

        // Delete resource links in database
        this.assetResourceRepository.deleteAll(draftKey);

        // Delete draft
        this.draftRepository.delete(publisherKey, draftKey);
    }

    @Override
    @Transactional
    public void submitDraft(CatalogueItemCommandDto command) throws AssetDraftException {
        try {
            // If the key is not set, create and lock a new record; Otherwise,
            // lock the existing one
            if (command.getDraftKey() == null) {
                command.setLocked(true);

                final AssetDraftDto draft = this.updateDraft(command);

                command.setDraftKey(draft.getKey());
            } else {
                this.getLock(command.getOwnerKey(), command.getDraftKey(), true);
            }

            // A draft must exist with status DRAFT
            final AssetDraftDto draft = this.ensureDraftAndStatus(
                command.getOwnerKey(), command.getPublisherKey(), command.getDraftKey(), EnumProviderAssetDraftStatus.DRAFT
            );

            // No other draft (excluding this one) with the same parent identifier must exist
            if (!StringUtils.isBlank(draft.getParentId())) {
                final ProviderAssetDraftEntity existingDraft = this.draftRepository.findAllByParentId(draft.getParentId()).stream()
                    .filter(d -> !d.getKey().equals(draft.getKey()))
                    .findFirst()
                    .orElse(null);

                if (existingDraft != null) {
                    throw new AssetDraftException(AssetMessageCode.DRAFT_FOR_PARENT_EXISTS, "Draft for the selected asset already exists");
                }
            }

            // Check if workflow exists
            final EnumProviderAssetDraftStatus newStatus = EnumProviderAssetDraftStatus.SUBMITTED;
            ProcessInstanceDto                 instance  = this.bpmEngine.findInstance(command.getDraftKey());

            if (instance == null) {
                final Map<String, VariableValueDto> variables = BpmInstanceVariablesBuilder.builder()
                    .variableAsString(EnumProcessInstanceVariable.START_USER_KEY.getValue(), command.getOwnerKey().toString())
                    .variableAsString("draftKey", command.getDraftKey().toString())
                    .variableAsString("publisherKey", command.getPublisherKey().toString())
                    .variableAsString("type", command.getType().toString())
                    .variableAsBoolean("ingested", command.isIngested())
                    .variableAsString("status", newStatus.toString())
                    .build();

                instance = this.bpmEngine.startProcessDefinitionByKey(
                    WORKFLOW_SELL_ASSET, command.getDraftKey().toString(), variables, true
                );
            }

            this.draftRepository.update(command, newStatus, instance.getDefinitionId(), instance.getId());
        } catch (final AssetDraftException ex) {
            throw ex;
        } finally {
            // Attempt to release lock but ignore exceptions
            try {
                this.releaseLock(command.getOwnerKey(), command.getDraftKey());
            } catch (final Exception ex) {
                logger.warn(
                    "Failed to release lock [type={}, key={}, message={}]",
                    EnumRecordLock.DRAFT, command.getDraftKey(), ex.getMessage()
                );
            }

        }

    }

    @Override
    @Transactional
    public void updateStatus(AssetDraftSetStatusCommandDto command) throws AssetDraftException {
        // TODO: Validate status transition

        this.draftRepository.updateStatus(command.getPublisherKey(), command.getAssetKey(), command.getStatus());
    }

    @Override
    @Transactional
    public void acceptHelpDesk(UUID publisherKey, UUID draftKey) throws AssetDraftException {
        this.reviewHelpDesk(publisherKey, draftKey, true, null);
    }

    @Override
    @Transactional
    public void rejectHelpDesk(UUID publisherKey, UUID draftKey, String reason) throws AssetDraftException {
        this.reviewHelpDesk(publisherKey, draftKey, false, reason);
    }

    private void reviewHelpDesk(UUID publisherKey, UUID draftKey, boolean accepted, String reason) throws AssetDraftException {
        try {
            // Update draft
            final EnumProviderAssetDraftStatus newStatus = accepted
                ? this.draftRepository.acceptHelpDesk(publisherKey, draftKey)
                : this.draftRepository.rejectHelpDesk(publisherKey, draftKey, reason);

            // Find workflow instance
            final TaskDto task = this.bpmEngine.findTaskById(draftKey.toString(), TASK_REVIEW).orElse(null);

            if (task != null) {
                // Complete task
                final Map<String, VariableValueDto> variables = BpmInstanceVariablesBuilder.builder()
                    .variableAsBoolean("helpdeskAccept", accepted)
                    .variableAsString("helpdeskRejectReason", reason)
                    .variableAsString("status", newStatus.toString())
                    .build();

                this.bpmEngine.completeTask(task.getId(), variables);
            }
        } catch (final FeignException fex) {
            logger.error("Operation has failed", fex);

            throw new AssetDraftException(AssetMessageCode.BPM_SERVICE, "Operation on BPM server failed", fex);
        }
    }

    @Override
    @Transactional
    public void acceptProvider(AssetDraftReviewCommandDto command) throws AssetDraftException {
        // Update status BEFORE updating workflow instance to avoid race
        // condition
        final EnumProviderAssetDraftStatus newStatus = this.reviewProviderSetStatus(
            command.getOwnerKey(), command.getPublisherKey(), command.getDraftKey(), true, null
        );

        // Send message to workflow instance
        this.reviewProviderSendMessage(command.getDraftKey(), newStatus, true, null);
    }

    @Override
    @Transactional
    public void rejectProvider(AssetDraftReviewCommandDto command) throws AssetDraftException {
        // Update status BEFORE updating workflow instance to avoid race
        // condition
        final EnumProviderAssetDraftStatus newStatus = this.reviewProviderSetStatus(
            command.getOwnerKey(), command.getPublisherKey(), command.getDraftKey(), false, command.getReason()
        );

        // Send message to workflow instance
        this.reviewProviderSendMessage(command.getDraftKey(), newStatus, false, command.getReason());
    }

    private EnumProviderAssetDraftStatus reviewProviderSetStatus(
        UUID ownerKey, UUID publisherKey, UUID draftKey, boolean accepted, String reason
    ) throws AssetDraftException {
        try {
            // A draft must exist with status in [PENDING_PROVIDER_REVIEW,
            // POST_PROCESSING, PROVIDER_REJECTED]
            final AssetDraftDto draft = this.findOneDraft(ownerKey, publisherKey, draftKey, false);

            if (draft == null) {
                throw new AssetDraftException(AssetMessageCode.DRAFT_NOT_FOUND);
            }

            // Get lock
            this.getLock(ownerKey, draftKey, true);

            if (draft.getStatus() != EnumProviderAssetDraftStatus.PENDING_PROVIDER_REVIEW &&
                draft.getStatus() != EnumProviderAssetDraftStatus.POST_PROCESSING &&
                draft.getStatus() != EnumProviderAssetDraftStatus.PROVIDER_REJECTED
            ) {
                throw new AssetDraftException(AssetMessageCode.INVALID_STATE, String.format(
                    "Expected status in [PENDING_PROVIDER_REVIEW, POST_PROCESSING, PROVIDER_REJECTED]. Found [%s]", draft.getStatus()
                ));
            }

            if (accepted) {
                return this.draftRepository.acceptProvider(publisherKey, draftKey);
            } else {
                return this.draftRepository.rejectProvider(publisherKey, draftKey, reason);
            }
        } finally {
            // Release lock since the processing workflow will resume
            this.releaseLock(ownerKey, draftKey);
        }
    }

    private void reviewProviderSendMessage(
        UUID draftKey, EnumProviderAssetDraftStatus status, boolean accepted, String reason
    ) throws AssetDraftException {
        try {
            final Map<String, VariableValueDto> variables = BpmInstanceVariablesBuilder.builder()
                .variableAsBoolean("providerAccept", accepted)
                .variableAsString("providerRejectReason", reason)
                .variableAsString("status", status.toString())
                .build();

            this.bpmEngine.correlateMessage(draftKey.toString(), MESSAGE_PROVIDER_REVIEW, variables);
        } catch (final FeignException fex) {
            logger.error("Operation has failed", fex);

            throw new AssetDraftException(AssetMessageCode.BPM_SERVICE, "Operation on BPM server failed", fex);
        }
    }

    @Override
    @Transactional
    public void publishDraft(UUID ownerKey, UUID publisherKey, UUID draftKey) throws AssetDraftException {
        try {
            // Validate draft state
            final AssetDraftDto draft = this.findOneDraft(ownerKey, publisherKey, draftKey, false);

            if (draft == null) {
                throw new AssetDraftException(AssetMessageCode.DRAFT_NOT_FOUND);
            }

            if (draft.getStatus() != EnumProviderAssetDraftStatus.POST_PROCESSING) {
                throw new AssetDraftException(
                    AssetMessageCode.INVALID_STATE,
                    String.format("Expected status to be [POST_PROCESSING]. Found [%s]", draft.getStatus())
                );
            }

            // Get PID service user id for publisher
            final AccountEntity publisher            = this.accountRepository.findOneByKey(publisherKey).orElse(null);
            final Integer       ownerId              = publisher.getProfile().getProvider().getPidServiceUserId();
            final String        assetType            = draft.getCommand().getType().toString();
            final String        assetTypeDescription = draft.getCommand().getType().getDescription();
            final String        assetDescription     = draft.getCommand().getTitle();

            // Register asset type
            pidService.registerAssetType(assetType, assetTypeDescription);

            // Register asset
            final String pid = pidService.registerAsset(
                draftKey.toString(), ownerId, assetType, assetDescription
            );

            // Create feature
            final CatalogueFeature feature = this.convertDraftToFeature(pid, publisherKey, draft);

            // Link draft file-system to asset file-system by creating a
            // symbolic link
            this.draftFileManager.linkDraftFilesToAsset(publisherKey, draftKey, pid);

            // If there is a parent identifier, unpublish parent asset
            if (!StringUtils.isBlank(draft.getParentId())) {
                this.catalogueService.unpublish(publisherKey, draft.getParentId());
            }

            // Publish asset
            this.catalogueService.publish(feature);

            // Link resource files to the new PID value
            this.assetResourceRepository.linkDraftResourcesToAsset(draftKey, pid);
            this.assetAdditionalResourceRepository.linkDraftResourcesToAsset(draftKey, pid);

            // Update draft status
            this.draftRepository.publish(publisherKey, draftKey, pid);
        } catch (final CatalogueServiceException ex) {
            throw new AssetDraftException(AssetMessageCode.CATALOGUE_SERVICE, "Failed to publish asset", ex);
        } catch (final AssetDraftException ex) {
            throw ex;
        } catch (final Exception ex) {
            logger.error("Operation has failed", ex);

            throw new AssetDraftException(AssetMessageCode.ERROR, "Failed to publish asset", ex);
        }
    }

    private CatalogueFeature convertDraftToFeature(String pid, UUID publisherKey, AssetDraftDto draft) {
        final CatalogueFeature feature = draft.getCommand().toFeature();

        feature.setId(pid);
        feature.getProperties().setPublisherId(publisherKey);

        // Initialize geometry if not already set for tabular assets
        if (draft.getType() == EnumAssetType.TABULAR && feature.getGeometry() == null) {
            final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
            final Geometry        geom            = geometryFactory.createPolygon(new Coordinate[] {
                new Coordinate(-180.0,-90.0),
                new Coordinate(180.0,-90.0),
                new Coordinate(180.0,90.0),
                new Coordinate(-180.0,90.0),
                new Coordinate(-180.0,-90.0)
            });

            feature.setGeometry(geom);
        }

        // Set contract
        final ProviderTemplateContractHistoryDto contract = this.contractRepository.findOneObjectByKey(
            publisherKey, draft.getCommand().getContractTemplateKey()
        );

        Assert.notNull(contract, "Expected a non-null provider template contract");

        feature.getProperties().setContractTemplateId(contract.getId());
        feature.getProperties().setContractTemplateVersion(contract.getVersion());

        // Redirect draft metadata property links to asset ones before
        // publishing the asset to the catalogue
        this.updateMetadataPropertyLinks(
            pid, draft.getCommand().getResources(), feature.getProperties().getAutomatedMetadata(), draft.getStatus()
        );

        return feature;
    }

    @Override
    @Transactional
    public void updateMetadata(
        UUID publisherKey, UUID draftKey, String resourceKey, JsonNode value
    ) throws FileSystemException, AssetDraftException {
        try {
            // The provider must have access to the selected draft and also the
            // draft must be already accepted by the HelpDesk. Since metadata is
            // updated by the publish workflow, we assume that owner key is equal to
            // publisher key.
            final AssetDraftDto draft = this.ensureDraftAndStatus(publisherKey, publisherKey, draftKey, EnumProviderAssetDraftStatus.SUBMITTED);

            // Get existing metadata. Create JsonNode object if no metadata
            // already exists
            if (draft.getCommand().getAutomatedMetadata() == null) {
                draft.getCommand().setAutomatedMetadata(objectMapper.createArrayNode());
            }
            final ArrayNode metadata = (ArrayNode) draft.getCommand().getAutomatedMetadata();

            // Store all metadata in asset repository
            final String content = objectMapper.writeValueAsString(value);

            this.draftFileManager.saveMetadataAsText(publisherKey, draftKey, resourceKey + ".automated-metadata.json", content);

            // Filter properties before updating metadata in catalogue service
            final FileResourceDto                   resource   = (FileResourceDto) draft.getResourceByKey(resourceKey);
            final EnumAssetType                     assetType  = resource.getCategory();
            final List<AssetMetadataPropertyEntity> properties = this.assetMetadataPropertyRepository.findAllByAssetType(assetType);

            for(final AssetMetadataPropertyEntity p: properties) {
                final String   propertyName = p.getName();
                final JsonNode propertyNode = value.get(propertyName);

                // Ignore undefined or null nodes
                if (propertyNode == null || propertyNode.isNull()) {
                    continue;
                }

                final String fileName = this.getMetadataPropertyFileName(resourceKey, propertyName, p.getType());

                switch (p.getType()) {
                    case PNG :
                        this.draftFileManager.saveMetadataPropertyAsImage(publisherKey, draftKey, fileName, propertyNode.asText());
                        break;
                    case JSON :
                        this.draftFileManager.saveMetadataPropertyAsJson(publisherKey, draftKey, fileName, objectMapper.writeValueAsString(propertyNode));
                        break;
                }

                final String uri = String.format(
                    "/action/drafts/%s/resources/%s/metadata/%s",
                    draftKey, resourceKey, propertyName
                );

                ((ObjectNode) value).put(propertyName, uri);
            }

            // Store filtered metadata in asset repository
            final String filteredContent = objectMapper.writeValueAsString(value);

            this.draftFileManager.saveMetadataAsText(publisherKey, draftKey, resourceKey + ".automated-metadata-minified.json", filteredContent);

            // Set resource key in metadata
            ((ObjectNode) value).put("key", resourceKey.toString());

            // Update metadata
            metadata.add(value);

            // Set geometry, if not already set, from metadata
            Geometry geometry = null;
            if (draft.getCommand().getGeometry() == null) {
                final JsonNode mbrNode = value.get(METADATA_PROPERTY_MBR);

                // Ignore undefined or null nodes
                if (mbrNode != null && !mbrNode.isNull()) {
                    final WKTReader mbrReader = new WKTReader();
                    geometry = mbrReader.read(mbrNode.asText());
                }
            }

            this.draftRepository.updateMetadataAndGeometry(publisherKey, draftKey, metadata, geometry);
        } catch (final AssetDraftException ex) {
            throw ex;
        } catch (final JsonProcessingException ex) {
            throw new AssetDraftException(
                AssetMessageCode.METADATA_SERIALIZATION,
                String.format("Failed to serialize automated metadata for asset [%s]", draftKey), ex
            );
        } catch (final FeignException fex) {
            logger.error(String.format("Operation has failed for asset. [draftKey=%s]", draftKey), fex);

            throw new AssetDraftException(AssetMessageCode.CATALOGUE_SERVICE, "Failed to update metadata", fex);
        } catch (final Exception ex) {
            logger.error("Failed to update metadata", ex);

            throw new AssetDraftException(AssetMessageCode.ERROR, "Failed to publish asset", ex);
        }
    }

    @Override
    @Transactional
    public void updateResourceIngestionData(
        UUID publisherKey, UUID draftKey, String resourceKey, ServerIngestResultResponseDto data
    ) throws AssetDraftException {
        try {
            // The provider must have access to the selected draft and also the
            // draft must be already accepted by the provider. Since ingestion
            // data is updated by the workflow, we assume that the owner key is
            // equal to the publisher key
            this.ensureDraftAndStatus(publisherKey, publisherKey, draftKey, null);

            this.draftRepository.updateResourceIngestionData(
                publisherKey, draftKey, resourceKey, ResourceIngestionDataDto.from(resourceKey, data)
            );
        } catch (final AssetDraftException ex) {
            throw ex;
        } catch (final FeignException fex) {
            logger.error(String.format("Operation has failed for asset. [draftKey=%s]", draftKey), fex);

            throw new AssetDraftException(AssetMessageCode.CATALOGUE_SERVICE, "Failed to update metadata", fex);
        } catch (final Exception ex) {
            logger.error("Failed to update metadata", ex);

            throw new AssetDraftException(AssetMessageCode.ERROR, "Failed to publish asset", ex);
        }
    }

    @Override
    @Transactional
    public void updateResourceIngestionData(
        UUID publisherKey, UUID draftKey, String resourceKey, ServerIngestPublishResponseDto data
    ) throws AssetDraftException {
        try {
            // The provider must have access to the selected draft and also the
            // draft must be already accepted by the provider. Since ingestion
            // data is updated by the workflow, we assume that the owner key is
            // equal to the publisher key
            this.ensureDraftAndStatus(publisherKey, publisherKey, draftKey, null);

            this.draftRepository.updateResourceIngestionData(publisherKey, draftKey, resourceKey, data);
        } catch (final AssetDraftException ex) {
            throw ex;
        } catch (final FeignException fex) {
            logger.error(String.format("Operation has failed for asset. [draftKey=%s]", draftKey), fex);

            throw new AssetDraftException(AssetMessageCode.CATALOGUE_SERVICE, "Failed to update metadata", fex);
        } catch (final Exception ex) {
            logger.error("Failed to update metadata", ex);

            throw new AssetDraftException(AssetMessageCode.ERROR, "Failed to publish asset", ex);
        }
    }

    @Override
    @Transactional
    public AssetDraftDto addFileResource(
        UserFileResourceCommandDto command
    ) throws FileSystemException, AssetRepositoryException, AssetDraftException {
        // The provider must have access to the selected draft and also the
        // draft must be editable
        AssetDraftDto draft = this.ensureDraftAndStatus(
            command.getOwnerKey(), command.getPublisherKey(), command.getDraftKey(), EnumProviderAssetDraftStatus.DRAFT
        );

        // Lock record before update
        final Pair<EnumLockResult, RecordLockDto> lock = this.getLock(command.getOwnerKey(), command.getDraftKey(), true);

        // Resolve resource file
        final FilePathCommand fileCommand = FilePathCommand.builder()
            .path(command.getPath())
            .userId(command.getUserId())
            .build();

        final Path path = this.userFileManager.resolveFilePath(fileCommand);

        // Add resource
        final FileResourceCommandDto resourceCommand = new FileResourceCommandDto();

        resourceCommand.setCategory(draft.getType());
        resourceCommand.setCrs(command.getCrs());
        resourceCommand.setDraftKey(draft.getKey());
        resourceCommand.setEncoding(command.getEncoding());
        resourceCommand.setFileName(command.getFileName());
        resourceCommand.setFormat(command.getFormat());
        resourceCommand.setOwnerKey(command.getOwnerKey());
        resourceCommand.setPublisherKey(command.getPublisherKey());
        resourceCommand.setSize(command.getSize());

        try (final InputStream input = Files.newInputStream(path)) {
            draft = this.addFileResource(resourceCommand, input);
        } catch(final AssetDraftException ex) {
            throw ex;
        } catch(final Exception ex) {
            throw new AssetDraftException(
                AssetMessageCode.API_COMMAND_RESOURCE_COPY,
                String.format("Failed to copy resource file [%s]", command.getPath()), ex
            );
        }

        // Release lock if it was created only for the specific operation
        if (lock != null && lock.getLeft() == EnumLockResult.CREATED) {
            this.releaseLock(command.getOwnerKey(), command.getDraftKey());
        } else if (lock != null) {
            draft.setLock(lock.getRight());
        }

        return draft;
    }

    @Override
    @Transactional
    public AssetDraftDto addFileResource(
        FileResourceCommandDto command, InputStream input
    ) throws FileSystemException, AssetRepositoryException, AssetDraftException {
        // The provider must have access to the selected draft and also the
        // draft must be editable
        final AssetDraftDto draft = this.ensureDraftAndStatus(
            command.getOwnerKey(), command.getPublisherKey(), command.getDraftKey(), EnumProviderAssetDraftStatus.DRAFT
        );

        // Lock record before update
        final Pair<EnumLockResult, RecordLockDto> lock = this.getLock(command.getOwnerKey(), command.getDraftKey(), true);

        // Set category
        if (command.getCategory() == null) {
            command.setCategory(draft.getType());
        }

        // Update database link
        final FileResourceDto resource = assetResourceRepository.update(command);

        // Update asset file repository
        this.draftFileManager.uploadResource(command, input);

        // Update draft with new file resource
        draft.getCommand().setDraftKey(command.getDraftKey());
        draft.getCommand().setOwnerKey(command.getOwnerKey());
        draft.getCommand().setPublisherKey(command.getPublisherKey());
        draft.getCommand().addFileResource(resource);

        final AssetDraftDto result = this.draftRepository.update(draft.getCommand());

        // Release lock if it was created only for the specific operation
        if (lock != null && lock.getLeft() == EnumLockResult.CREATED) {
            this.releaseLock(command.getOwnerKey(), command.getDraftKey());
        } else if (lock != null) {
            draft.setLock(lock.getRight());
        }

        return result;
    }

    @Override
    @Transactional
    public AssetDraftDto addServiceResource(
        UUID publisherKey, UUID draftKey, ServiceResourceDto resource
    ) throws AssetRepositoryException, AssetDraftException {
        return this.draftRepository.addServiceResource(publisherKey, draftKey, resource);
    }

    @Override
    @Transactional
    public AssetDraftDto addAdditionalResource(
        AssetFileAdditionalResourceCommandDto command, InputStream input
    ) throws FileSystemException, AssetRepositoryException, AssetDraftException {
        // The provider must have access to the selected draft and also the
        // draft must be editable
        final AssetDraftDto draft = this.ensureDraftAndStatus(
            command.getOwnerKey(), command.getPublisherKey(), command.getDraftKey(), EnumProviderAssetDraftStatus.DRAFT
        );

        // Lock record before update
        final Pair<EnumLockResult, RecordLockDto> lock = this.getLock(command.getOwnerKey(), command.getDraftKey(), true);

        // Update database link
        final AssetFileAdditionalResourceDto resource = assetAdditionalResourceRepository.update(command);

        // Update asset file repository
        this.draftFileManager.uploadAdditionalResource(command, input);

        // Update draft with new file resource
        draft.getCommand().setDraftKey(command.getDraftKey());
        draft.getCommand().setOwnerKey(command.getOwnerKey());
        draft.getCommand().setPublisherKey(command.getPublisherKey());
        draft.getCommand().addAdditionalResource(resource);

        final AssetDraftDto result = this.draftRepository.update(draft.getCommand());

        // Release lock if it was created only for the specific operation
        if (lock != null && lock.getLeft() == EnumLockResult.CREATED) {
            this.releaseLock(command.getOwnerKey(), command.getDraftKey());
        } else if (lock != null) {
            result.setLock(lock.getRight());
        }

        return result;
    }

    @Override
    public Path resolveAssetAdditionalResource(
        String pid, String resourceKey
    ) throws FileSystemException, AssetRepositoryException {
        final AssetAdditionalResourceEntity resource = this.assetAdditionalResourceRepository
            .findOneByAssetPidAndResourceKey(pid, resourceKey)
            .orElse(null);

        final Path path = this.assetFileManager.resolveAdditionalResourcePath(pid, resource.getFileName());

        if (!path.toFile().exists()) {
            throw new FileSystemException(FileSystemMessageCode.FILE_IS_MISSING, "File not found");
        }

        return path;
    }

    @Override
    @Transactional
    public Path resolveDraftAdditionalResource(
        UUID ownerKey, UUID publisherKey, UUID draftKey, String resourceKey
    ) throws FileSystemException, AssetRepositoryException {
        // The provider must have access to the selected draft
        this.ensureDraftAndStatus(ownerKey, publisherKey, draftKey, null);

        final AssetAdditionalResourceEntity resource = this.assetAdditionalResourceRepository
            .findOneByDraftKeyAndResourceKey(draftKey, resourceKey)
            .orElse(null);

        final Path path = this.draftFileManager.resolveAdditionalResourcePath(publisherKey, draftKey, resource.getFileName());

        if (!path.toFile().exists()) {
            throw new FileSystemException(FileSystemMessageCode.FILE_IS_MISSING, "File not found");
        }

        return path;
    }

    @Override
    public MetadataProperty resolveAssetMetadataProperty(
        String pid, String resourceKey, String propertyName
    ) throws FileSystemException, AssetRepositoryException {
        final AssetResourceEntity resource = this.assetResourceRepository
            .findOneByAssetPidAndResourceKey(pid, resourceKey)
            .orElse(null);

        final AssetMetadataPropertyEntity property = this.assetMetadataPropertyRepository
            .findOneByAssetTypeAndName(resource.getCategory(), propertyName)
            .orElse(null);

        final String fileName = this.getMetadataPropertyFileName(resource.getKey(), propertyName, property.getType());
        final Path   path     = this.assetFileManager.resolveMetadataPropertyPath(pid, fileName);

        if (!path.toFile().exists()) {
            throw new FileSystemException(FileSystemMessageCode.FILE_IS_MISSING, "File not found");
        }

        return MetadataProperty.of(property.getType(), path);
    }

    @Override
    @Transactional
    public MetadataProperty resolveDraftMetadataProperty(
        UUID ownerKey, UUID publisherKey, UUID draftKey, String resourceKey, String propertyName
    ) throws FileSystemException, AssetRepositoryException {
        // The provider must have access to the selected draft
        this.ensureDraftAndStatus(ownerKey, publisherKey, draftKey, null);

        final AssetResourceEntity resource = this.assetResourceRepository
            .findOneByDraftKeyAndResourceKey(draftKey, resourceKey)
            .orElse(null);

        final AssetMetadataPropertyEntity property = this.assetMetadataPropertyRepository
            .findOneByAssetTypeAndName(resource.getCategory(), propertyName)
            .orElse(null);

        final String fileName = this.getMetadataPropertyFileName(resource.getKey(), propertyName, property.getType());
        final Path   path     = this.draftFileManager.resolveMetadataPropertyPath(publisherKey, draftKey, fileName);

        if (!path.toFile().exists()) {
            throw new FileSystemException(FileSystemMessageCode.FILE_IS_MISSING, "File not found");
        }

        return MetadataProperty.of(property.getType(), path);
    }

    private AssetDraftDto ensureDraftAndStatus(
        UUID ownerKey, UUID publisherKey, UUID assetKey, EnumProviderAssetDraftStatus status
    ) throws AssetDraftException {
        final AssetDraftDto draft = this.findOneDraft(ownerKey, publisherKey, assetKey, false);

        if (draft == null) {
            throw new AssetDraftException(AssetMessageCode.DRAFT_NOT_FOUND);
        }

        if (status != null && draft.getStatus() != status) {
            throw new AssetDraftException(
                AssetMessageCode.INVALID_STATE,
                String.format("Expected status is [%s]. Found [%s]", status, draft.getStatus())
            );
        }

        return draft;
    }

    private void consolidateResources(AssetDraftDto draft) {
        final CatalogueItemCommandDto          command             = draft.getCommand();
        final UUID                             ownerKey            = command.getOwnerKey();
        final UUID                             publisherKey        = command.getPublisherKey();
        final UUID                             draftKey            = command.getDraftKey();
        final List<ResourceDto>                resources           = command.getResources();
        final List<AssetAdditionalResourceDto> additionalResources = command.getAdditionalResources();

        // Delete all resources that are not present in the draft record
        final List<String> rids = resources.stream()
            .filter(r -> r.getType() == EnumResourceType.FILE)
            .map(r -> r.getId())
            .collect(Collectors.toList());

        final List<FileResourceDto> registeredResources = this.assetResourceRepository.findAllResourcesByDraftKey(draft.getKey()).stream()
            .map(AssetResourceEntity::toDto)
            .collect(Collectors.toList());

        registeredResources.stream().filter(r -> !rids.contains(r.getId())).forEach(r -> {
            // Delete resource record in transaction before deleting file
            final FileResourceDto resource = this.deleteResourceRecord(ownerKey, publisherKey, draftKey, r.getId());

            // Update asset file repository
            this.draftFileManager.deleteResource(publisherKey, draftKey, resource.getFileName());
        });

        // Delete all additional fire resources that are not present in the
        // draft record
        final List<String> arids = additionalResources.stream().filter(r -> r.getType() == EnumAssetAdditionalResource.FILE)
            .map(r -> (AssetFileAdditionalResourceDto) r).map(r -> r.getId())
            .collect(Collectors.toList());

        final List<AssetFileAdditionalResourceDto> registeredAdditionalResources = this.assetAdditionalResourceRepository
            .findAllResourcesByDraftKey(draft.getKey()).stream().map(AssetAdditionalResourceEntity::toDto)
            .collect(Collectors.toList());

        registeredAdditionalResources.stream().filter(r -> !arids.contains(r.getId())).forEach(r -> {
            // Delete resource record in transaction before deleting file
            final AssetFileAdditionalResourceDto resource = this.deleteAdditionalResourceRecord(
                ownerKey, publisherKey, draftKey, r.getId()
            );

            // Update asset file repository
            this.draftFileManager.deleteAdditionalResource(publisherKey, draftKey, resource.getFileName());
        });
    }

    private FileResourceDto deleteResourceRecord(UUID ownerKey, UUID publisherKey, UUID draftKey, String resourceKey) {
        // The provider must have access to the selected draft and also the
        // draft must be editable
        this.ensureDraftAndStatus(ownerKey, publisherKey, draftKey, EnumProviderAssetDraftStatus.DRAFT);

        return assetResourceRepository.delete(draftKey, resourceKey);
    }

    private AssetFileAdditionalResourceDto deleteAdditionalResourceRecord(UUID ownerKey, UUID publisherKey, UUID draftKey, String resourceKey) {
        // The provider must have access to the selected draft and also the
        // draft must be editable
        this.ensureDraftAndStatus(ownerKey, publisherKey, draftKey, EnumProviderAssetDraftStatus.DRAFT);

        return assetAdditionalResourceRepository.delete(draftKey, resourceKey);
    }

    @Override
    public void updateMetadataPropertyLinks(
        String id, List<ResourceDto> resources, JsonNode metadata, EnumProviderAssetDraftStatus status
    ) throws AssetDraftException {
        String urlTemplate = "";

        switch (status) {
            case POST_PROCESSING :
                urlTemplate = "/action/assets/%s/resources/%s/metadata/%s";
                break;
            case PENDING_HELPDESK_REVIEW :
                urlTemplate = "/action/helpdesk-drafts/%s/resources/%s/metadata/%s";
                break;
            default :
                throw new AssetDraftException(AssetMessageCode.INVALID_STATE, String.format(
                    "Expected status in [POST_PROCESSING, PENDING_PROVIDER_REVIEW]. Found [%s]", status
                ));
        }

        for (final ResourceDto r : resources) {
            if (r.getType() != EnumResourceType.FILE) {
                continue;
            }
            final FileResourceDto                   fr         = (FileResourceDto) r;
            final EnumAssetType                     assetType  = fr.getCategory();
            final List<AssetMetadataPropertyEntity> properties = this.assetMetadataPropertyRepository.findAllByAssetType(assetType);
            ObjectNode                              current    = null;

            for (int i = 0; i < metadata.size(); i++) {
                if (metadata.get(i).get("key").asText().equals(r.getId().toString())) {
                    current = (ObjectNode) metadata.get(i);
                    break;
                }
            }

            if (current == null || current.isNull()) {
                continue;
            }

            for(final AssetMetadataPropertyEntity p: properties) {
                final String   propertyName = p.getName();
                final JsonNode propertyNode = current.get(propertyName);

                // Ignore undefined or null nodes
                if (propertyNode == null || propertyNode.isNull()) {
                    continue;
                }

                final String uri = String.format(urlTemplate, id, r.getId(), propertyName);

                current.put(propertyName, uri);
            }
        }
    }

    @Override
    public void unpublishAsset(UnpublishAssetCommand command) throws CatalogueServiceException {
        final CatalogueItemDetailsDto item = this.catalogueService.findOne(
            null, command.getPid(), command.getPublisherKey(), false
        );
        if (item == null) {
            throw new CatalogueServiceException(CatalogueServiceMessageCode.ITEM_NOT_FOUND);
        }

        final String businessKey = String.format("%s:%s:unpublish", command.getPublisherKey(), command.getPid());

        // Check if workflow exists
        final ProcessInstanceDto instance = this.bpmEngine.findInstance(businessKey);

        if (instance == null) {
            final Map<String, VariableValueDto> variables = BpmInstanceVariablesBuilder.builder()
                .variableAsString(EnumProcessInstanceVariable.START_USER_KEY.getValue(), command.getUserKey().toString())
                .variableAsString("assetId", command.getPid().toString())
                .variableAsString("assetName", item.getTitle())
                .variableAsString("assetVersion", item.getVersion())
                .variableAsString("publisherKey", command.getPublisherKey().toString())
                .build();

            this.bpmEngine.startProcessDefinitionByKey(
                WORKFLOW_UNPUBLISH_ASSET, businessKey, variables, true
            );
        }
    }

    @Override
    public PageResultDto<ProviderAccountSubscriptionDto> findAllSubscriptions(
        UUID publisherKey, int pageIndex, int pageSize,
        EnumProviderSubSortField orderBy, EnumSortingOrder order
    ) {
        final Direction   direction   = order == EnumSortingOrder.DESC ? Direction.DESC : Direction.ASC;
        final PageRequest pageRequest = PageRequest.of(pageIndex, pageSize, Sort.by(direction, orderBy.getValue()));

        final Page<ProviderAccountSubscriptionDto> page = this.subscriptionRepository.findAllObjectsByProvider(publisherKey, pageRequest);

        return PageResultDto.of(pageIndex, pageSize, page.getContent(), page.getTotalElements());
    }

    private String getMetadataPropertyFileName(String resourceKey, String propertyName, EnumMetadataPropertyType propertyType) {
        return StringUtils.joinWith(".", resourceKey, "property", propertyName, propertyType.getExtension());
    }

    private Pair<EnumLockResult, RecordLockDto> getLock(UUID userKey, UUID draftKey, boolean required) throws AssetDraftException {
        final RecordLockDto lock = this.recordLockRepository.findOneForDraftAsObject(draftKey).orElse(null);

        if (required && lock != null && !lock.getOwnerKey().equals(userKey)) {
            throw new AssetDraftException(
                AssetMessageCode.LOCK_EXISTS,
                String.format("Record is already locked by user [%s]", lock.getOwnerEmail())
            );
        }
        if (required && lock == null) {
            final Integer       recordId = this.draftRepository.getIdFromKey(draftKey);
            final RecordLockDto newLock  = this.recordLockRepository.create(EnumRecordLock.DRAFT, recordId, userKey);

            return Pair.of(EnumLockResult.CREATED, newLock);
        }

        return Pair.of(lock == null ? EnumLockResult.NONE : EnumLockResult.EXISTS, lock);
    }

    @Override
    @Transactional
    public void releaseLock(UUID userKey, UUID draftKey) throws AssetDraftException {
        final Optional<RecordLockEntity> lock = this.recordLockRepository.findOneForDraft(draftKey);

        if (lock.isPresent()) {
            if (!lock.get().getOwner().getKey().equals(userKey)) {
                throw new AssetDraftException(
                    AssetMessageCode.LOCK_EXISTS,
                    String.format("Record belongs to user [%s]", lock.get().getOwner().getEmail())
                );
            }

            recordLockRepository.delete(lock.get());
        }
    }

    @Override
    @Cacheable(
        cacheNames = "draft-services",
        cacheManager = "defaultCacheManager",
        key = "'draft-' + #draftKey + '-' + #resourceKey"
    )
    public List<ResourceIngestionDataDto> getServices(UUID publisherKey, UUID draftKey) throws AssetDraftException {
        final AssetDraftDto draft = this.findOneDraft(publisherKey, draftKey, false);

        // Draft must exist
        if (draft == null) {
            throw new AssetDraftException(AssetMessageCode.DRAFT_NOT_FOUND, "Draft not found");
        }
        // Draft status cannot be in [DRAFT, SUBMITTED]. Ingestion data may be
        // missing or partially computed
        if(draft.getStatus() == EnumProviderAssetDraftStatus.DRAFT ||
           draft.getStatus() == EnumProviderAssetDraftStatus.SUBMITTED
        ) {
            throw new AssetDraftException(AssetMessageCode.INVALID_STATE, "Draft status cannot be in [DRAFT, SUBMITTED]");
        }

        final List<ResourceIngestionDataDto> services = draft.getCommand().getIngestionInfo();

        // Services must exist
        if (CollectionUtils.isEmpty(services)) {
            throw new AssetDraftException(AssetMessageCode.INVALID_STATE, "Ingestion data not found");
        }

        return services;
    }

}
