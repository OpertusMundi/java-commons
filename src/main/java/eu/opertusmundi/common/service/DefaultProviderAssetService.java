package eu.opertusmundi.common.service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.camunda.bpm.engine.rest.dto.VariableValueDto;
import org.camunda.bpm.engine.rest.dto.message.CorrelationMessageDto;
import org.camunda.bpm.engine.rest.dto.runtime.ProcessInstanceDto;
import org.camunda.bpm.engine.rest.dto.runtime.StartProcessInstanceDto;
import org.camunda.bpm.engine.rest.dto.task.CompleteTaskDto;
import org.camunda.bpm.engine.rest.dto.task.TaskDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.opertusmundi.common.domain.ProviderAssetDraftEntity;
import eu.opertusmundi.common.feign.client.BpmServerFeignClient;
import eu.opertusmundi.common.feign.client.CatalogueFeignClient;
import eu.opertusmundi.common.model.PageResultDto;
import eu.opertusmundi.common.model.asset.AssetDraftDto;
import eu.opertusmundi.common.model.asset.AssetDraftReviewCommandDto;
import eu.opertusmundi.common.model.asset.AssetDraftSetStatusCommandDto;
import eu.opertusmundi.common.model.asset.AssetMessageCode;
import eu.opertusmundi.common.model.asset.EnumProviderAssetDraftStatus;
import eu.opertusmundi.common.model.catalogue.client.CatalogueItemCommandDto;
import eu.opertusmundi.common.model.catalogue.server.CatalogueFeature;
import eu.opertusmundi.common.repository.ProviderAssetDraftRepository;
import feign.FeignException;

@Service
public class DefaultProviderAssetService implements ProviderAssetService {

    private static final Logger logger = LoggerFactory.getLogger(DefaultProviderAssetService.class);

    private static final String DRAFT_PUBLISHED_STATUS = "published";

    private static final String WORKFLOW_SELL_ASSET = "workflow-provider-publish-asset";

    private static final String TASK_REVIEW = "task-review";

    private static final String MESSAGE_PROVIDER_REVIEW = "provider-publish-asset-user-acceptance-message";

    private final List<String> sortableFields = Arrays.asList(
        "status",
        "createdOn",
        "modifiedOn",
        "title",
        "version",
        "account.profile.provider.name"
    );

    @Autowired
    private ProviderAssetDraftRepository providerAssetDraftRepository;

    @Autowired
    private ObjectProvider<CatalogueFeignClient> catalogueClient;

    @Autowired
    private ObjectProvider<BpmServerFeignClient> bpmClient;

    @Override
    public PageResultDto<AssetDraftDto> findAllDraft(
        UUID publisherKey, Set<EnumProviderAssetDraftStatus> status, int pageIndex, int pageSize, String orderBy, String order
    ) {
        final Direction direction = order.equalsIgnoreCase("desc") ? Direction.DESC : Direction.ASC;
        if (!this.sortableFields.contains(orderBy)) {
            logger.warn("Sort field {} is not supported", orderBy);
            orderBy = "createdOn";
        }

        final PageRequest   pageRequest = PageRequest.of(pageIndex, pageSize, Sort.by(direction, orderBy));
        Page<AssetDraftDto> page;

        if (status != null && !status.isEmpty() && publisherKey != null) {
            page = this.providerAssetDraftRepository
                .findAllByPublisherAndStatus(publisherKey, status, pageRequest)
                .map(ProviderAssetDraftEntity::toDto);
        } else if (publisherKey != null) {
            page = this.providerAssetDraftRepository
                .findAllByPublisher(publisherKey, pageRequest)
                .map(ProviderAssetDraftEntity::toDto);
        } else if (status != null && !status.isEmpty()) {
            page = this.providerAssetDraftRepository
                .findAllByStatus(status, pageRequest)
                .map(ProviderAssetDraftEntity::toDto);
        } else {
            page = this.providerAssetDraftRepository
                .findAll(pageRequest)
                .map(ProviderAssetDraftEntity::toDto);
        }

        final long count = page.getTotalElements();
        final List<AssetDraftDto> records = page.getContent();

        return PageResultDto.of(pageIndex, pageSize, records, count);
    }

    @Override
    public AssetDraftDto findOneDraft(UUID publisherKey, UUID assetKey) {
        final ProviderAssetDraftEntity e = this.providerAssetDraftRepository.findOneByPublisherAndKey(publisherKey, assetKey).orElse(null);

        return e != null ? e.toDto() : null;
    }

    @Override
    @Transactional
    public AssetDraftDto updateDraft(CatalogueItemCommandDto command) throws AssetDraftException {
        final AssetDraftDto result = this.providerAssetDraftRepository.update(command);

        return result;
    }

    @Override
    @Transactional
    public void deleteDraft(UUID publisherKey, UUID draftKey) throws AssetDraftException {
        this.ensureDraftAndStatus(publisherKey, draftKey, EnumProviderAssetDraftStatus.DRAFT);

        this.providerAssetDraftRepository.delete(publisherKey, draftKey);
    }

    @Override
    @Transactional
    public void submitDraft(CatalogueItemCommandDto command) throws AssetDraftException {
        try {
            // A draft must exist with status DRAFT
            this.ensureDraftAndStatus(command.getPublisherKey(), command.getAssetKey(), EnumProviderAssetDraftStatus.DRAFT);

            // Check if workflow exists
            ProcessInstanceDto instance = this.findInstance(command.getAssetKey());

            if (instance == null) {
                final StartProcessInstanceDto options = new StartProcessInstanceDto();

                final Map<String, VariableValueDto> variables = new HashMap<String, VariableValueDto>();

                // Set variables
                this.setStringVariable(variables, "userId", command.getUserId());
                this.setStringVariable(variables, "draftKey", command.getAssetKey());
                this.setStringVariable(variables, "publisherKey", command.getPublisherKey());
                this.setBooleanVariable(variables, "ingested", command.isIngested());
                this.setStringVariable(variables, "source", command.getSource());

                options.setBusinessKey(command.getAssetKey().toString());
                options.setVariables(variables);
                options.setWithVariablesInReturn(true);

                instance = this.bpmClient.getObject().startProcessByKey(WORKFLOW_SELL_ASSET, options);
            }

            this.providerAssetDraftRepository.update(command, EnumProviderAssetDraftStatus.SUBMITTED, instance.getDefinitionId(),
                    instance.getId());
        } catch (final AssetDraftException ex) {
            throw ex;
        }
    }

    @Override
    @Transactional
    public void updateStatus(AssetDraftSetStatusCommandDto command) throws AssetDraftException {
        // TODO: Validate status transition

        this.providerAssetDraftRepository.updateStatus(command.getPublisherKey(), command.getAssetKey(), command.getStatus());
    }

    @Override
    @Transactional
    public void acceptHelpDesk(UUID publisherKey, UUID draftKey) throws AssetDraftException {
        this.reviewHelpDesk(publisherKey, draftKey, false, null);
    }

    @Override
    @Transactional
    public void rejectHelpDesk(UUID publisherKey, UUID draftKey, String reason) throws AssetDraftException {
        this.reviewHelpDesk(publisherKey, draftKey, true, reason);
    }

    @Transactional
    private void reviewHelpDesk(UUID publisherKey, UUID draftKey, boolean rejected, String reason) throws AssetDraftException {
        try {
            // Find workflow instance
            final List<TaskDto> tasks = this.bpmClient.getObject().findInstanceTaskById(draftKey.toString(), TASK_REVIEW);

            if (tasks.size() == 1) {
                // Complete task
                final CompleteTaskDto               options   = new CompleteTaskDto();
                final Map<String, VariableValueDto> variables = new HashMap<String, VariableValueDto>();

                this.setBooleanVariable(variables, "helpdeskAccept", !rejected);
                this.setStringVariable(variables, "helpdeskRejectReason", reason);

                options.setVariables(variables);

                this.bpmClient.getObject().completeTask(tasks.get(0).getId(), options);
            }

            // Update draft
            if (rejected) {
                this.providerAssetDraftRepository.reject(publisherKey, draftKey, reason, EnumProviderAssetDraftStatus.HELPDESK_REJECTED);
            } else {
                this.providerAssetDraftRepository.updateStatus(publisherKey, draftKey,
                        EnumProviderAssetDraftStatus.PENDING_PROVIDER_REVIEW);
            }
        } catch (final FeignException fex) {
            logger.error("[Feign Client][Provider Asset] Operation has failed", fex);

            throw new AssetDraftException(AssetMessageCode.BPM_SERVICE, "Operation on BPM server failed", fex);
        }
    }

    @Override
    @Transactional
    public void acceptProvider(AssetDraftReviewCommandDto command) throws AssetDraftException {
        this.reviewProvider(command.getPublisherKey(), command.getAssetKey(), false, null);
    }

    @Override
    @Transactional
    public void rejectProvider(AssetDraftReviewCommandDto command) throws AssetDraftException {
        this.reviewProvider(command.getPublisherKey(), command.getAssetKey(), true, command.getReason());
    }

    @Transactional
    private void reviewProvider(UUID publisherKey, UUID draftKey, boolean rejected, String reason) throws AssetDraftException {
        try {
            // A draft must exist with status in [PENDING_PROVIDER_REVIEW,
            // POST_PROCESSING, PROVIDER_REJECTED]
            final AssetDraftDto draft = this.findOneDraft(publisherKey, draftKey);

            if (draft == null) {
                throw new AssetDraftException(AssetMessageCode.DRAFT_NOT_FOUND);
            }

            if (draft.getStatus() != EnumProviderAssetDraftStatus.PENDING_PROVIDER_REVIEW
                    && draft.getStatus() != EnumProviderAssetDraftStatus.POST_PROCESSING
                    && draft.getStatus() != EnumProviderAssetDraftStatus.PROVIDER_REJECTED) {
                throw new AssetDraftException(AssetMessageCode.INVALID_STATE, String.format(
                        "Expected status in [PENDING_PROVIDER_REVIEW, POST_PROCESSING, PROVIDER_REJECTED]. Found [%s]", draft.getStatus()));
            }

            // Send message to workflow instance
            final CorrelationMessageDto         message   = new CorrelationMessageDto();
            final Map<String, VariableValueDto> variables = new HashMap<String, VariableValueDto>();

            this.setBooleanVariable(variables, "providerAccept", !rejected);
            this.setStringVariable(variables, "providerRejectReason", reason);

            message.setMessageName(MESSAGE_PROVIDER_REVIEW);
            message.setBusinessKey(draftKey.toString());
            message.setProcessVariables(variables);
            message.setVariablesInResultEnabled(true);
            message.setResultEnabled(true);

            this.bpmClient.getObject().correlateMessage(message);

            // Update status
            this.providerAssetDraftRepository.updateStatus(publisherKey, draftKey,
                    rejected ? EnumProviderAssetDraftStatus.PROVIDER_REJECTED : EnumProviderAssetDraftStatus.POST_PROCESSING);
        } catch (final FeignException fex) {
            logger.error("[Feign Client][Provider Asset] Operation has failed", fex);

            throw new AssetDraftException(AssetMessageCode.BPM_SERVICE, "Operation on BPM server failed", fex);
        }
    }

    @Override
    @Transactional
    public void publishDraft(UUID publisherKey, UUID draftKey) throws AssetDraftException {
        try {
            // TODO : id must be created by the PID service

            final ProviderAssetDraftEntity draft = this.providerAssetDraftRepository.findOneByPublisherAndKey(publisherKey, draftKey)
                    .orElse(null);

            if (draft == null) {
                throw new AssetDraftException(AssetMessageCode.DRAFT_NOT_FOUND);
            }

            if (draft.getStatus() != EnumProviderAssetDraftStatus.POST_PROCESSING) {
                throw new AssetDraftException(AssetMessageCode.INVALID_STATE,
                        String.format("Expected status to be [POST_PROCESSING]. Found [%s]", draft.getStatus()));
            }

            // Create feature
            final CatalogueFeature feature = draft.getCommand().toFeature();

            feature.setId(draftKey.toString());
            feature.getProperties().setPublisherId(publisherKey);

            draft.getCommand().getPricingModels().forEach(p -> feature.getProperties().getPricingModels().add(p));

            // TODO: Check if asset already exists (draft key can be used as the
            // idempotent key)
            // Insert new asset
            this.catalogueClient.getObject().createDraft(feature);

            // Publish asset
            this.catalogueClient.getObject().setDraftStatus(draftKey, DRAFT_PUBLISHED_STATUS);

            // Update draft status
            this.providerAssetDraftRepository.updateStatus(publisherKey, draftKey, EnumProviderAssetDraftStatus.PUBLISHED);
        } catch (final AssetDraftException ex) {
            throw ex;
        } catch (final FeignException fex) {
            logger.error("[Feign Client][Catalogue] Operation has failed", fex);

            throw new AssetDraftException(AssetMessageCode.CATALOGUE_SERVICE, "Failed to publish asset", fex);
        } catch (final Exception ex) {
            logger.error("[Catalogue] Operation has failed", ex);

            throw new AssetDraftException(AssetMessageCode.ERROR, "Failed to publish asset", ex);
        }
    }

    @Transactional
    private void ensureDraftAndStatus(UUID publisherKey, UUID assetKey, EnumProviderAssetDraftStatus status) {
        final AssetDraftDto draft = this.findOneDraft(publisherKey, assetKey);

        if (draft == null) {
            throw new AssetDraftException(AssetMessageCode.DRAFT_NOT_FOUND);
        }

        if (draft.getStatus() != status) {
            throw new AssetDraftException(AssetMessageCode.INVALID_STATE,
                    String.format("Expected status is [%s]. Found [%s]", status, draft.getStatus()));
        }
    }

    private ProcessInstanceDto findInstance(UUID businessKey) {
        try {
            final List<ProcessInstanceDto> instances = this.bpmClient.getObject().getInstance(businessKey.toString());

            return instances.stream().findFirst().orElse(null);
        } catch (final FeignException fex) {
            logger.error("[Feign Client][Provider Asset] Operation has failed", fex);

            // Handle 404 errors as valid responses
            if (fex.status() == HttpStatus.NOT_FOUND.value()) {
                return null;
            }

            throw new AssetDraftException(AssetMessageCode.BPM_SERVICE, "Operation on BPM server failed", fex);
        }
    }

    private void setBooleanVariable(Map<String, VariableValueDto> variables, String name, Object value) {
        this.setVariable(variables, "Boolean", name, value);
    }

    private void setStringVariable(Map<String, VariableValueDto> variables, String name, Object value) {
        this.setVariable(variables, "String", name, value);
    }

    private void setVariable(Map<String, VariableValueDto> variables, String type, String name, Object value) {
        final VariableValueDto v = new VariableValueDto();

        v.setValue(value);
        v.setType(type);

        variables.put(name, v);
    }

}
