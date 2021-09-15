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
import org.camunda.bpm.engine.rest.dto.VariableValueDto;
import org.camunda.bpm.engine.rest.dto.runtime.ProcessInstanceDto;
import org.camunda.bpm.engine.rest.dto.task.TaskDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

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
import eu.opertusmundi.common.model.EnumSortingOrder;
import eu.opertusmundi.common.model.PageResultDto;
import eu.opertusmundi.common.model.asset.AssetAdditionalResourceDto;
import eu.opertusmundi.common.model.asset.AssetDraftDto;
import eu.opertusmundi.common.model.asset.AssetDraftReviewCommandDto;
import eu.opertusmundi.common.model.asset.AssetDraftSetStatusCommandDto;
import eu.opertusmundi.common.model.asset.AssetFileAdditionalResourceCommandDto;
import eu.opertusmundi.common.model.asset.AssetFileAdditionalResourceDto;
import eu.opertusmundi.common.model.asset.AssetMessageCode;
import eu.opertusmundi.common.model.asset.AssetRepositoryException;
import eu.opertusmundi.common.model.asset.EnumAssetAdditionalResource;
import eu.opertusmundi.common.model.asset.EnumAssetSourceType;
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
import eu.opertusmundi.common.model.catalogue.CatalogueServiceException;
import eu.opertusmundi.common.model.catalogue.CatalogueServiceMessageCode;
import eu.opertusmundi.common.model.catalogue.client.CatalogueHarvestImportCommandDto;
import eu.opertusmundi.common.model.catalogue.client.CatalogueItemCommandDto;
import eu.opertusmundi.common.model.catalogue.client.CatalogueItemDetailsDto;
import eu.opertusmundi.common.model.catalogue.client.CatalogueItemSamplesCommandDto;
import eu.opertusmundi.common.model.catalogue.client.CatalogueItemVisibilityCommandDto;
import eu.opertusmundi.common.model.catalogue.client.DraftApiCommandDto;
import eu.opertusmundi.common.model.catalogue.client.DraftApiFromAssetCommandDto;
import eu.opertusmundi.common.model.catalogue.client.DraftApiFromFileCommandDto;
import eu.opertusmundi.common.model.catalogue.client.DraftFromAssetCommandDto;
import eu.opertusmundi.common.model.catalogue.client.EnumDeliveryMethod;
import eu.opertusmundi.common.model.catalogue.client.EnumSpatialDataServiceType;
import eu.opertusmundi.common.model.catalogue.client.EnumType;
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

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AssetFileTypeRepository assetFileTypeRepository;

    @Autowired
    private AssetMetadataPropertyRepository assetMetadataPropertyRepository;

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
        UUID publisherKey,
        Set<EnumProviderAssetDraftStatus> status, Set<EnumType> type, Set<EnumSpatialDataServiceType> serviceType,
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

        final Page<AssetDraftDto> page = this.draftRepository.findAllByPublisherAndStatus(
            publisherKey, status, type, serviceType, pageRequest
        ).map(ProviderAssetDraftEntity::toDto);

        final long                count   = page.getTotalElements();
        final List<AssetDraftDto> records = page.getContent();

        return PageResultDto.of(pageIndex, pageSize, records, count);
    }

    @Override
    public AssetDraftDto findOneDraft(UUID publisherKey, UUID draftKey) {
        final ProviderAssetDraftEntity e = this.draftRepository.findOneByPublisherAndKey(publisherKey, draftKey).orElse(null);

        final AssetDraftDto draft = e != null ? e.toDto() : null;

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
        }

        throw new AssetDraftException(
            AssetMessageCode.API_COMMAND_NOT_SUPPORTED,
            String.format("API command type [%s] is not supported", command.getType())
        );
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

    AssetDraftDto createApiDraftFromAsset(DraftApiFromAssetCommandDto command) throws AssetDraftException {
        try {
            final CatalogueFeature feature = this.catalogueService.findOneFeature(command.getPid());

            // Feature must exist
            if(feature == null) {
                throw new AssetDraftException(
                    AssetMessageCode.API_COMMAND_ASSET_NOT_FOUND,
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
            draftCommand.setDeliveryMethod(EnumDeliveryMethod.DIGITAL_PLATFORM);
            draftCommand.setIngested(true);
            draftCommand.setIngestionInfo(null);
            draftCommand.setParentId(command.getPid());
            draftCommand.setPublisherKey(command.getPublisherKey());
            draftCommand.setSpatialDataServiceType(EnumSpatialDataServiceType.fromString(command.getServiceType()));
            draftCommand.setTitle(command.getTitle());
            draftCommand.setType(EnumType.SERVICE);
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

            return draft;
        } catch (final CatalogueServiceException ex) {
            throw new AssetDraftException(AssetMessageCode.CATALOGUE_SERVICE, "Failed to create API draft from asset", ex);
        } catch (final AssetDraftException ex) {
            throw ex;
        } catch(final Exception ex) {
            throw new AssetDraftException(AssetMessageCode.ERROR, "Failed to create API draft from asset", ex);
        }
    }

    AssetDraftDto createApiDraftFromFile(DraftApiFromFileCommandDto command) throws AssetDraftException {
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
            draftCommand.setPublisherKey(command.getPublisherKey());
            draftCommand.setSpatialDataServiceType(EnumSpatialDataServiceType.fromString(command.getServiceType()));
            draftCommand.setTitle(command.getTitle());
            draftCommand.setType(EnumType.SERVICE);
            draftCommand.setVersion(command.getVersion());

            AssetDraftDto draft = this.updateDraft(draftCommand);

            // Get file format and category
            final AssetFileTypeEntity format = this.assetFileTypeRepository
                .findOneByFormat(command.getFormat()).get();

            // Add resource
            final FileResourceCommandDto resourceCommand = new FileResourceCommandDto();

            resourceCommand.setCategory(format.getCategory());
            resourceCommand.setDraftKey(draft.getKey());
            resourceCommand.setFileName(FilenameUtils.getName(command.getPath()));
            resourceCommand.setFormat(format.getFormat());
            resourceCommand.setPublisherKey(command.getPublisherKey());
            resourceCommand.setSize(resourcePath.toFile().length());

            try (final InputStream input = Files.newInputStream(resourcePath)) {
                draft = this.addFileResource(resourceCommand, input);
            } catch(final Exception ex) {
                throw new AssetDraftException(
                    AssetMessageCode.API_COMMAND_RESOURCE_COPY,
                    String.format("Failed to copy resource file [%s]", command.getPath()), ex
                );
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
        final AssetDraftDto draft = this.draftRepository.update(command);

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
            draftCommand.setParentId(command.getPid());
            draftCommand.setPublisherKey(command.getPublisherKey());
            draftCommand.setTitle(draftCommand.getTitle() + " [Draft]");

            final AssetDraftDto draft = this.updateDraft(draftCommand);

            return draft;
        } catch (final AssetDraftException ex) {
            throw ex;
        } catch(final Exception ex) {
            throw new AssetDraftException(AssetMessageCode.ERROR, "Failed to create draft from asset existing asset", ex);
        }
    }

    @Override
    @Transactional
    public AssetDraftDto updateDraftMetadataVisibility(CatalogueItemVisibilityCommandDto command) throws AssetDraftException {
        final AssetDraftDto draft = this.draftRepository.update(command);

        return draft;
    }

    @Override
    @Transactional
    public AssetDraftDto updateDraftMetadataSamples(CatalogueItemSamplesCommandDto command) throws AssetDraftException {
        // Check draft and owner
        final ProviderAssetDraftEntity draft = this.draftRepository.findOneByPublisherAndKey(
            command.getProviderKey(), command.getDraftKey()
        ).orElse(null);

        if (draft == null) {
            throw new AssetDraftException(AssetMessageCode.DRAFT_NOT_FOUND, "Draft not found");
        }
        // Check resource
        final ResourceDto resource = draft.getCommand().getResources().stream()
            .filter(r -> r.getId().equals(command.getResourceKey()))
            .findFirst()
            .orElse(null);

        if (resource == null) {
            throw new AssetDraftException(AssetMessageCode.RESOURCE_NOT_FOUND, "Resource not found");
        }

        final String fileName = this.getMetadataPropertyFileName(
            command.getResourceKey(), "samples", EnumMetadataPropertyType.JSON
        );
        final Path   path     = this.draftFileManager.resolveMetadataPropertyPath(
            command.getProviderKey(), command.getDraftKey(), fileName
        );

        try {
            final String content = this.objectMapper.writeValueAsString(command.getData());
            FileUtils.writeStringToFile(path.toFile(), content, Charset.forName("UTF-8"));
        } catch (final Exception ex) {
            throw new AssetDraftException(AssetMessageCode.IO_ERROR, "Failed to serialize and persist samples");
        }

        return draft.toDto();
    }

    @Override
    public void deleteDraft(UUID publisherKey, UUID draftKey) throws AssetDraftException {
        this.ensureDraftAndStatus(publisherKey, draftKey, EnumProviderAssetDraftStatus.DRAFT);

        // Delete data in transaction before deleting files
        this.deleteDraftData(publisherKey, draftKey);

        // Delete all files for the selected draft
        this.draftFileManager.deleteAllFiles(publisherKey, draftKey);
    }

    @Transactional
    private void deleteDraftData(UUID publisherKey, UUID draftKey) throws AssetDraftException {
        this.ensureDraftAndStatus(publisherKey, draftKey, EnumProviderAssetDraftStatus.DRAFT);

        // Delete resource links in database
        this.assetResourceRepository.deleteAll(draftKey);

        // Delete draft
        this.draftRepository.delete(publisherKey, draftKey);
    }

    @Override
    @Transactional
    public void submitDraft(CatalogueItemCommandDto command) throws AssetDraftException {
        try {
            // Create draft if key is not set
            if (command.getAssetKey() == null) {
                final AssetDraftDto draft = this.updateDraft(command);

                command.setAssetKey(draft.getKey());
            }

            // A draft must exist with status DRAFT
            final AssetDraftDto draft = this.ensureDraftAndStatus(
                command.getPublisherKey(), command.getAssetKey(), EnumProviderAssetDraftStatus.DRAFT
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
            ProcessInstanceDto                 instance  = this.bpmEngine.findInstance(command.getAssetKey());

            if (instance == null) {
                final Map<String, VariableValueDto> variables = BpmInstanceVariablesBuilder.builder()
                    .variableAsString(EnumProcessInstanceVariable.START_USER_KEY.getValue(), command.getPublisherKey().toString())
                    .variableAsString("draftKey", command.getAssetKey().toString())
                    .variableAsString("publisherKey", command.getPublisherKey().toString())
                    .variableAsString("type", command.getType().toString())
                    .variableAsBoolean("ingested", command.isIngested())
                    .variableAsString("status", newStatus.toString())
                    .build();

                instance = this.bpmEngine.startProcessDefinitionByKey(
                    WORKFLOW_SELL_ASSET, command.getAssetKey().toString(), variables, true
                );
            }

            this.draftRepository.update(command, newStatus, instance.getDefinitionId(), instance.getId());
        } catch (final AssetDraftException ex) {
            throw ex;
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

    @Transactional
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
    public void acceptProvider(AssetDraftReviewCommandDto command) throws AssetDraftException {
        // Update status BEFORE updating workflow instance to avoid race
        // condition
        final EnumProviderAssetDraftStatus newStatus = this.reviewProviderSetStatus(
            command.getPublisherKey(), command.getAssetKey(), true, null
        );

        // Send message to workflow instance
        this.reviewProviderSendMessage(command.getAssetKey(), newStatus, true, null);
    }

    @Override
    public void rejectProvider(AssetDraftReviewCommandDto command) throws AssetDraftException {
        // Update status BEFORE updating workflow instance to avoid race
        // condition
        final EnumProviderAssetDraftStatus newStatus = this.reviewProviderSetStatus(
            command.getPublisherKey(), command.getAssetKey(), false, command.getReason()
        );

        // Send message to workflow instance
        this.reviewProviderSendMessage(command.getAssetKey(), newStatus, false, command.getReason());
    }

    @Transactional
    private EnumProviderAssetDraftStatus reviewProviderSetStatus(
        UUID publisherKey, UUID draftKey, boolean accepted, String reason
    ) throws AssetDraftException {
        // A draft must exist with status in [PENDING_PROVIDER_REVIEW,
        // POST_PROCESSING, PROVIDER_REJECTED]
        final AssetDraftDto draft = this.findOneDraft(publisherKey, draftKey);

        if (draft == null) {
            throw new AssetDraftException(AssetMessageCode.DRAFT_NOT_FOUND);
        }

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
    public void publishDraft(UUID publisherKey, UUID draftKey) throws AssetDraftException {
        try {
            // Validate draft state
            final AssetDraftDto draft = this.findOneDraft(publisherKey, draftKey);

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
        UUID publisherKey, UUID draftKey, UUID resourceKey, JsonNode value
    ) throws FileSystemException, AssetDraftException {
        try {
            // The provider must have access to the selected draft and also the
            // draft must be already accepted by the HelpDesk
            final AssetDraftDto draft = this.ensureDraftAndStatus(publisherKey, draftKey, EnumProviderAssetDraftStatus.SUBMITTED);

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
            final EnumAssetSourceType               source     = mapFormatToSourceType(resource.getFormat());
            final List<AssetMetadataPropertyEntity> properties = this.assetMetadataPropertyRepository.findAllByAssetType(source);

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

            this.draftRepository.updateMetadata(publisherKey, draftKey, metadata);
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
    public void updateResourceIngestionData(
        UUID publisherKey, UUID draftKey, UUID resourceKey, ServerIngestResultResponseDto data
    ) throws AssetDraftException {
        try {
            // The provider must have access to the selected draft and also the
            // draft must be already accepted by the provider
            this.ensureDraftAndStatus(publisherKey, draftKey, null);

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
    public void updateResourceIngestionData(
        UUID publisherKey, UUID draftKey, UUID resourceKey, ServerIngestPublishResponseDto data
    ) throws AssetDraftException {
        try {
            // The provider must have access to the selected draft and also the
            // draft must be already accepted by the provider
            this.ensureDraftAndStatus(publisherKey, draftKey, null);

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
        FileResourceCommandDto command, InputStream input
    ) throws FileSystemException, AssetRepositoryException, AssetDraftException {
        // The provider must have access to the selected draft and also the
        // draft must be editable
        final AssetDraftDto draft = this.ensureDraftAndStatus(
            command.getPublisherKey(), command.getDraftKey(), EnumProviderAssetDraftStatus.DRAFT
        );

        // Set category
        command.setCategory(this.mapFormatToSourceType(command.getFormat()));

        // Update database link
        final FileResourceDto resource = assetResourceRepository.update(command);

        // Update asset file repository
        this.draftFileManager.uploadResource(command, input);

        // Update draft with new file resource
        draft.getCommand().setAssetKey(command.getDraftKey());
        draft.getCommand().setPublisherKey(command.getPublisherKey());
        draft.getCommand().addFileResource(resource);

        return this.draftRepository.update(draft.getCommand());
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
            command.getPublisherKey(), command.getDraftKey(), EnumProviderAssetDraftStatus.DRAFT
        );

        // Update database link
        final AssetFileAdditionalResourceDto resource = assetAdditionalResourceRepository.update(command);

        // Update asset file repository
        this.draftFileManager.uploadAdditionalResource(command, input);

        // Update draft with new file resource
        draft.getCommand().setAssetKey(command.getDraftKey());
        draft.getCommand().setPublisherKey(command.getPublisherKey());
        draft.getCommand().addAdditionalResource(resource);

        return this.draftRepository.update(draft.getCommand());
    }

    @Override
    public Path resolveAssetAdditionalResource(
        String pid, UUID resourceKey
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
    public Path resolveDraftAdditionalResource(
        UUID publisherKey, UUID draftKey, UUID resourceKey
    ) throws FileSystemException, AssetRepositoryException {
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
        String pid, UUID resourceKey, String propertyName
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
    public MetadataProperty resolveDraftMetadataProperty(
        UUID publisherKey, UUID draftKey, UUID resourceKey, String propertyName
    ) throws FileSystemException, AssetRepositoryException {
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

    public EnumAssetSourceType mapFormatToSourceType(String format) throws AssetDraftException {
        final Optional<AssetFileTypeEntity> fileType = this.assetFileTypeRepository.findOneByFormat(format);

        if (fileType.isPresent()) {
            return fileType.get().getCategory();
        }

        throw new AssetDraftException(AssetMessageCode.FORMAT_NOT_SUPPORTED,
                String.format("Format [%s] cannot be mapped to data profiler source type", format));
    }

    @Transactional
    private AssetDraftDto ensureDraftAndStatus(
        UUID publisherKey, UUID assetKey, EnumProviderAssetDraftStatus status
    ) throws AssetDraftException {
        final AssetDraftDto draft = this.findOneDraft(publisherKey, assetKey);

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

    @Transactional
    private void consolidateResources(AssetDraftDto draft) {
        final CatalogueItemCommandDto          command             = draft.getCommand();
        final UUID                             publisherKey        = command.getPublisherKey();
        final UUID                             draftKey            = command.getAssetKey();
        final List<ResourceDto>                resources           = command.getResources();
        final List<AssetAdditionalResourceDto> additionalResources = command.getAdditionalResources();

        // Delete all resources that are not present in the draft record
        final List<UUID> rids = resources.stream()
            .filter(r -> r.getType() == EnumResourceType.FILE)
            .map(r -> r.getId())
            .collect(Collectors.toList());

        final List<FileResourceDto> registeredResources = this.assetResourceRepository.findAllResourcesByDraftKey(draft.getKey()).stream()
            .map(AssetResourceEntity::toDto)
            .collect(Collectors.toList());

        registeredResources.stream().filter(r -> !rids.contains(r.getId())).forEach(r -> {
            // Delete resource record in transaction before deleting file
            final FileResourceDto resource = this.deleteResourceRecord(publisherKey, draftKey, r.getId());

            // Update asset file repository
            this.draftFileManager.deleteResource(publisherKey, draftKey, resource.getFileName());
        });

        // Delete all additional fire resources that are not present in the
        // draft record
        final List<UUID> arids = additionalResources.stream().filter(r -> r.getType() == EnumAssetAdditionalResource.FILE)
            .map(r -> (AssetFileAdditionalResourceDto) r).map(r -> r.getId())
            .collect(Collectors.toList());

        final List<AssetFileAdditionalResourceDto> registeredAdditionalResources = this.assetAdditionalResourceRepository
            .findAllResourcesByDraftKey(draft.getKey()).stream().map(AssetAdditionalResourceEntity::toDto)
            .collect(Collectors.toList());

        registeredAdditionalResources.stream().filter(r -> !arids.contains(r.getId())).forEach(r -> {
            // Delete resource record in transaction before deleting file
            final AssetFileAdditionalResourceDto resource = this.deleteAdditionalResourceRecord(publisherKey, draftKey, r.getId());

            // Update asset file repository
            this.draftFileManager.deleteAdditionalResource(publisherKey, draftKey, resource.getFileName());
        });
    }

    @Transactional
    private FileResourceDto deleteResourceRecord(UUID publisherKey, UUID draftKey, UUID resourceKey) {
        // The provider must have access to the selected draft and also the
        // draft must be editable
        this.ensureDraftAndStatus(publisherKey, draftKey, EnumProviderAssetDraftStatus.DRAFT);

        return assetResourceRepository.delete(draftKey, resourceKey);
    }

    @Transactional
    private AssetFileAdditionalResourceDto deleteAdditionalResourceRecord(UUID publisherKey, UUID draftKey, UUID resourceKey) {
        // The provider must have access to the selected draft and also the
        // draft must be editable
        this.ensureDraftAndStatus(publisherKey, draftKey, EnumProviderAssetDraftStatus.DRAFT);

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
            final EnumAssetSourceType               source     = mapFormatToSourceType(fr.getFormat());
            final List<AssetMetadataPropertyEntity> properties = this.assetMetadataPropertyRepository.findAllByAssetType(source);
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


    private String getMetadataPropertyFileName(UUID resourceKey, String propertyName, EnumMetadataPropertyType propertyType) {
        return StringUtils.joinWith(".", resourceKey, "property", propertyName, propertyType.getExtension());
    }

}
