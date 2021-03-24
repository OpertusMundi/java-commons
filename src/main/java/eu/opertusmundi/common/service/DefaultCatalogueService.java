package eu.opertusmundi.common.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.camunda.bpm.engine.rest.dto.VariableValueDto;
import org.camunda.bpm.engine.rest.dto.runtime.ProcessInstanceDto;
import org.camunda.bpm.engine.rest.dto.runtime.StartProcessInstanceDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import brave.Span;
import brave.Tracer;
import eu.opertusmundi.common.domain.AccountEntity;
import eu.opertusmundi.common.domain.AssetAdditionalResourceEntity;
import eu.opertusmundi.common.domain.AssetResourceEntity;
import eu.opertusmundi.common.feign.client.BpmServerFeignClient;
import eu.opertusmundi.common.feign.client.CatalogueFeignClient;
import eu.opertusmundi.common.model.PageRequestDto;
import eu.opertusmundi.common.model.PageResultDto;
import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.asset.AssetFileAdditionalResourceDto;
import eu.opertusmundi.common.model.asset.EnumAssetAdditionalResource;
import eu.opertusmundi.common.model.catalogue.CatalogueResult;
import eu.opertusmundi.common.model.catalogue.CatalogueServiceException;
import eu.opertusmundi.common.model.catalogue.CatalogueServiceMessageCode;
import eu.opertusmundi.common.model.catalogue.client.CatalogueAssetQuery;
import eu.opertusmundi.common.model.catalogue.client.CatalogueClientSetStatusCommandDto;
import eu.opertusmundi.common.model.catalogue.client.CatalogueDraftQuery;
import eu.opertusmundi.common.model.catalogue.client.CatalogueHarvestCommandDto;
import eu.opertusmundi.common.model.catalogue.client.CatalogueHarvestImportCommandDto;
import eu.opertusmundi.common.model.catalogue.client.CatalogueItemCommandDto;
import eu.opertusmundi.common.model.catalogue.client.CatalogueItemDetailsDto;
import eu.opertusmundi.common.model.catalogue.client.CatalogueItemDraftDto;
import eu.opertusmundi.common.model.catalogue.client.CatalogueItemDto;
import eu.opertusmundi.common.model.catalogue.client.EnumCatalogueType;
import eu.opertusmundi.common.model.catalogue.server.CatalogueCollection;
import eu.opertusmundi.common.model.catalogue.server.CatalogueFeature;
import eu.opertusmundi.common.model.catalogue.server.CatalogueResponse;
import eu.opertusmundi.common.model.dto.PublisherDto;
import eu.opertusmundi.common.model.pricing.BasePricingModelCommandDto;
import eu.opertusmundi.common.model.pricing.EffectivePricingModelDto;
import eu.opertusmundi.common.repository.AssetAdditionalResourceRepository;
import eu.opertusmundi.common.repository.AssetResourceRepository;
import eu.opertusmundi.common.repository.ProviderRepository;
import feign.FeignException;

@Service
public class DefaultCatalogueService implements CatalogueService {

    private static final Logger logger = LoggerFactory.getLogger(DefaultCatalogueService.class);
    
    private static final String WORKFLOW_CATALOGUE_HARVEST = "workflow-catalogue-harvest";

    @Autowired
    private Tracer tracer;

    @Autowired
    private ProviderRepository providerRepository;
    
    @Autowired
    private AssetResourceRepository assetResourceRepository;
    
    @Autowired
    private AssetAdditionalResourceRepository assetAdditionalResourceRepository;

    @Autowired
    private ObjectProvider<BpmServerFeignClient> bpmClient;

    @Autowired
    private ObjectProvider<CatalogueFeignClient> catalogueClient;
    
    @Autowired
    private QuotationService quotationService;

    @Override
    public CatalogueResult<CatalogueItemDto> findAll(CatalogueAssetQuery request) throws CatalogueServiceException {
        Assert.notNull(request, "Expected a non-null request");
        
        try {
            // Catalogue service data page index is 1-based
            final ResponseEntity<CatalogueResponse<CatalogueCollection>> e = this.catalogueClient.getObject().findAll(
                request.getQuery(), request.getPublisherKey(), request.getPage() + 1, request.getSize()
            );

            final CatalogueResponse<CatalogueCollection> catalogueResponse = e.getBody();

            if (!catalogueResponse.isSuccess()) {
                throw CatalogueServiceException.fromService(catalogueResponse.getMessage());
            }

            // Process response
            final CatalogueResult<CatalogueItemDto> response = this.createSearchResult(
                catalogueResponse, (item) -> new CatalogueItemDto(item), request.getPage(), request.getSize(), true
            );

            return response;
        } catch (final FeignException fex) {
            // Convert 404 errors to empty results
            if (fex.status() == HttpStatus.NOT_FOUND.value()) {
                return CatalogueResult.empty(request.toPageRequest());
            }

            logger.error("[Feign Client][Catalogue] Operation has failed", fex);

            throw new CatalogueServiceException(CatalogueServiceMessageCode.CATALOGUE_SERVICE, fex.getMessage());
        } catch (final Exception ex) {
            logger.error("[Catalogue] Operation has failed", ex);

            throw CatalogueServiceException.wrap(ex);
        }
    }
    

    @Override
    public List<CatalogueItemDto> findAllById(String[] id) throws CatalogueServiceException {
        Assert.notEmpty(id, "Expected a non-empty array of identifers");
        
        try {
            // Catalogue service data page index is 1-based
            final ResponseEntity<CatalogueResponse<List<CatalogueFeature>>> e = this.catalogueClient.getObject().findAllById(id);

            final CatalogueResponse<List<CatalogueFeature>> catalogueResponse = e.getBody();

            if (!catalogueResponse.isSuccess()) {
                throw CatalogueServiceException.fromService(catalogueResponse.getMessage());
            }
            
            if (catalogueResponse.getResult().size() != id.length) {
                throw new CatalogueServiceException(
                    CatalogueServiceMessageCode.ITEM_NOT_FOUND, 
                    String.format("Expected [%d] items. Found [%s]", id.length, catalogueResponse.getResult().size())
                );
            }

            return catalogueResponse.getResult().stream().map(CatalogueItemDto::new).collect(Collectors.toList());
        } catch (final CatalogueServiceException ex) {
            throw ex;
        } catch (final FeignException fex) {
            logger.error("[Feign Client][Catalogue] Operation has failed", fex);

            throw new CatalogueServiceException(CatalogueServiceMessageCode.CATALOGUE_SERVICE, fex.getMessage());
        } catch (final Exception ex) {
            logger.error("[Catalogue] Operation has failed", ex);

            throw CatalogueServiceException.wrap(ex);
        }
    }
    
    
    public CatalogueResult<CatalogueItemDraftDto> findAllDraft(CatalogueDraftQuery request) throws CatalogueServiceException {
        Assert.notNull(request, "Expected a non-null request");
        
        try {
            // Catalogue service data page index is 1-based
            final ResponseEntity<CatalogueResponse<CatalogueCollection>> e = this.catalogueClient.getObject().findAllDraft(
                request.getPublisherKey(), request.getStatus().getValue(), request.getPage() + 1, request.getSize()
            );

            final CatalogueResponse<CatalogueCollection> catalogueResponse = e.getBody();

            if (!catalogueResponse.isSuccess()) {
                throw CatalogueServiceException.fromService(catalogueResponse.getMessage());
            }

            // Process response
            final CatalogueResult<CatalogueItemDraftDto> response = this.createSearchResult(
                catalogueResponse, (item) -> new CatalogueItemDraftDto(item), request.getPage(), request.getSize(), true
            );

            return response;
        } catch (final FeignException fex) {
            // Convert 404 errors to empty results
            if (fex.status() == HttpStatus.NOT_FOUND.value()) {
                return CatalogueResult.empty(request.toPageRequest());
            }

            logger.error("[Feign Client][Catalogue] Operation has failed", fex);

            throw new CatalogueServiceException(CatalogueServiceMessageCode.CATALOGUE_SERVICE, fex.getMessage());
        } catch (final Exception ex) {
            logger.error("[Catalogue] Operation has failed", ex);

            throw CatalogueServiceException.wrap(ex);
        }
    }

    @Override
    public CatalogueItemDetailsDto findOne(String id, UUID userKey, boolean includeAutomatedMetadata) throws CatalogueServiceException {
        try {
            final ResponseEntity<CatalogueResponse<CatalogueFeature>> e = this.catalogueClient.getObject().findOneById(id);

            final CatalogueResponse<CatalogueFeature> catalogueResponse = e.getBody();

            if(!catalogueResponse.isSuccess()) {
                throw CatalogueServiceException.fromService(catalogueResponse.getMessage());
            }

            // Convert feature to catalogue item
            final CatalogueItemDetailsDto item = new CatalogueItemDetailsDto(catalogueResponse.getResult());

            // Filter automated metadata
            if (!includeAutomatedMetadata) {
                item.setAutomatedMetadata(null);
            }
            // Filter ingestion information
            if (!item.getPublisherId().equals(userKey)) {
                item.setIngestionInfo(null);
            }

            // Inject publisher details
            final PublisherDto publisher = this.providerRepository.findOneByKey(item.getPublisherId()).toPublisherDto();

            item.setPublisher(publisher);
            
            // Consolidate data from asset repository
            List<AssetResourceEntity> resources = this.assetResourceRepository
                .findAllResourcesByAssetPid(item.getId());

            resources.stream()
                .map(AssetResourceEntity::toDto)
                .forEach(item.getResources()::add);
            
            List<AssetAdditionalResourceEntity> additionalResources = this.assetAdditionalResourceRepository
                .findAllResourcesByAssetPid(item.getId());
           
            item.getAdditionalResources().stream()
                .filter(r -> r.getType() == EnumAssetAdditionalResource.FILE)
                .forEach(r -> {
                    final AssetFileAdditionalResourceDto fileResource  = (AssetFileAdditionalResourceDto) r;
                    final AssetAdditionalResourceEntity resourceEntity = additionalResources.stream()
                        .filter(r1 -> r1.getKey().equals(fileResource.getId()))
                        .findFirst()
                        .orElse(null);

                        if (resourceEntity != null) {
                            fileResource.setModifiedOn(resourceEntity.getCreatedOn());
                            fileResource.setSize(resourceEntity.getSize());
                        }
                    });

            // Compute effective pricing models
            this.refreshPricingModels(item);

            return item;
        } catch (final FeignException fex) {
            // Convert 404 errors to empty results
            if (fex.status() == HttpStatus.NOT_FOUND.value()) {
                return null;
            }

            logger.error("[Feign Client][Catalogue] Operation has failed", fex);

            throw new CatalogueServiceException(CatalogueServiceMessageCode.CATALOGUE_SERVICE, fex.getMessage());
        } catch (final Exception ex) {
            logger.error("[Catalogue] Operation has failed", ex);

            throw CatalogueServiceException.wrap(ex);
        }
    }

    @Override
    public void harvestCatalogue(CatalogueHarvestCommandDto command) throws CatalogueServiceException {
        try {
            // Check if workflow exists
            final String businessKey = String.format("%s-%s", command.getUserKey(), command.getUrl());
            
            ProcessInstanceDto instance = this.findRunningInstance(businessKey);

            if (instance == null) {
                final StartProcessInstanceDto options = new StartProcessInstanceDto();

                final Map<String, VariableValueDto> variables = new HashMap<String, VariableValueDto>();

                // Set defaults
                if (command.getType() == null) {
                    command.setType(EnumCatalogueType.CSW);
                }
                // Set variables
                this.setStringVariable(variables, "userKey", command.getUserKey());
                this.setStringVariable(variables, "catalogueUrl", command.getUrl());
                this.setStringVariable(variables, "catalogueType", command.getType().toString());

                options.setBusinessKey(businessKey);
                options.setVariables(variables);
                options.setWithVariablesInReturn(true);

                this.bpmClient.getObject().startProcessByKey(WORKFLOW_CATALOGUE_HARVEST, options);
            }
        } catch (final FeignException fex) {
            logger.error("[Feign Client][BPM Server] Operation has failed", fex);

            throw CatalogueServiceException.wrap(fex);
        } catch (final Exception ex) {
            logger.error("[Catalogue] Operation has failed", ex);

            throw CatalogueServiceException.wrap(ex);
        }
    }

    @Override
    public CatalogueResult<CatalogueItemDto> findAllHarvested(
        String url, String query, int pageIndex, int pageSize
    ) throws CatalogueServiceException {
        try {
            // Catalogue service data page index is 1-based
            final ResponseEntity<CatalogueResponse<CatalogueCollection>> e = this.catalogueClient.getObject().findAllHarvest(
                url, query, pageIndex + 1, pageSize
            );

            final CatalogueResponse<CatalogueCollection> catalogueResponse = e.getBody();

            if (!catalogueResponse.isSuccess()) {
                throw CatalogueServiceException.fromService(catalogueResponse.getMessage());
            }

            // Process response
            final CatalogueResult<CatalogueItemDto> response = this.createSearchResult(
                catalogueResponse, (item) -> new CatalogueItemDto(item), pageIndex, pageSize, false
            );

            return response;
        } catch (final FeignException fex) {
            // Convert 404 errors to empty results
            if (fex.status() == HttpStatus.NOT_FOUND.value()) {
                return CatalogueResult.empty(PageRequestDto.of(pageIndex, pageSize));
            }

            logger.error("[Feign Client][Catalogue] Operation has failed", fex);

            throw new CatalogueServiceException(CatalogueServiceMessageCode.CATALOGUE_SERVICE, fex.getMessage());
        } catch (final Exception ex) {
            logger.error("[Catalogue] Operation has failed", ex);

            throw CatalogueServiceException.wrap(ex);
        }
    }
    
    @Override
    public void importFromCatalogue(CatalogueHarvestImportCommandDto command) throws CatalogueServiceException {

    }
    
    @Override
    public void deleteAsset(String pid) {
        try {
            this.catalogueClient.getObject().deletePublished(pid);
        } catch (final Exception ex) {
            logger.error("[Catalogue] Operation has failed", ex);

            throw CatalogueServiceException.wrap(ex);
        }
    }

    /**
     * Set workflow instance variable of type String
     * 
     * @param variables
     * @param name
     * @param value
     */
    private void setStringVariable(Map<String, VariableValueDto> variables, String name, Object value) {
        this.setVariable(variables, "String", name, value);
    }

    /**
     * Set workflow instance variable
     * 
     * @param variables
     * @param type
     * @param name
     * @param value
     */
    private void setVariable(Map<String, VariableValueDto> variables, String type, String name, Object value) {
        final VariableValueDto v = new VariableValueDto();

        v.setValue(value);
        v.setType(type);

        variables.put(name, v);
    }

    /**
     * Find running workflow instance by business key
     * 
     * @param businessKey
     * @return
     */
    private ProcessInstanceDto findRunningInstance(String businessKey) {
        final List<ProcessInstanceDto> instances = this.bpmClient.getObject().getInstance(businessKey);

        return instances.stream()
            .filter(i -> !i.isEnded())
            .findFirst()
            .orElse(null);
    }

    /**
     * Convert a catalogue response to an API Gateway response
     *
     * @param catalogueResponse
     * @param converter
     * @param pageIndex
     * @param pageSize
     * @param includeProviders
     * @return
     */
    private <T extends CatalogueItemDto> CatalogueResult<T> createSearchResult(
        CatalogueResponse<CatalogueCollection> catalogueResponse,
        Function<CatalogueFeature, T> converter,
        int pageIndex, int pageSize,
        boolean includeProviders
    ) {
        // Convert features to items
        final CatalogueCollection features = catalogueResponse.getResult();

        final List<T> items = features.getItems().stream()
            .map(item -> {
                final T dto = converter.apply(item);

                // Compute effective pricing models
                this.refreshPricingModels(dto);
                
                // Filter properties
                dto.setAutomatedMetadata(null);
                dto.setIngestionInfo(null);

                return dto;
            })
            .collect(Collectors.toList());


        final PageResultDto<T> result = PageResultDto.of(
            pageIndex,
            pageSize,
            items,
            features.getTotal()
        );

        // Get all publishers in the result
        List<PublisherDto> publishers = null;

        if (includeProviders) {
            final Span span = this.tracer.nextSpan().name("database-publisher").start();
    
            try {
                final UUID[] publisherKeys = items.stream().map(i -> i.getPublisherId()).distinct().toArray(UUID[]::new);
    
                publishers = this.providerRepository.findAllByKey(publisherKeys).stream()
                    .map(AccountEntity::toPublisherDto)
                    .filter(p -> p != null)
                    .collect(Collectors.toList());
    
                if (publisherKeys.length != publishers.size()) {
                    throw new CatalogueServiceException(
                        CatalogueServiceMessageCode.PUBLISHER_NOT_FOUND,
                        "All publishers must exist"
                    );
                }
            } finally {
                span.finish();
            }
        }

        return new CatalogueResult<T>(result, publishers);
    }

    /**
     * Compute pricing models effective values for a catalogue item
     *
     * @param item
     */
    private void refreshPricingModels(CatalogueItemDto item) {
        final List<BasePricingModelCommandDto> models = item.getPricingModels();

        if (models.isEmpty()) {
            return;
        }

        final List<EffectivePricingModelDto> quotations = quotationService.createQuotation(item);

        item.setEffectivePricingModels(quotations);
    }
    
    /**
     * TODO: Implement draft using catalogue
     */

    public RestResponse<Void> createDraftTemp(CatalogueItemCommandDto command) {
        try {
            // TODO : id must be created by the PID service

            // Inject provider (current user) key
            command.setPublisherKey(command.getPublisherKey());

            // Create feature
            final CatalogueFeature feature = command.toFeature();

            command.getPricingModels().stream().forEach(m-> {
                // Always override the key with a value generated at the server
                m.setKey(UUID.randomUUID());
            });

            // Insert new asset
            this.catalogueClient.getObject().createDraft(feature);

            return RestResponse.success();
        } catch (final FeignException fex) {
            logger.error("[Feign Client][Catalogue] Operation has failed", fex);
        } catch (final Exception ex) {
            logger.error("[Catalogue] Operation has failed", ex);
        }

        return RestResponse.failure();
    }
    

    public CatalogueItemDraftDto findOneDraftTemp(UUID draftKey) {
        try {
            final ResponseEntity<CatalogueResponse<CatalogueFeature>> e = this.catalogueClient.getObject()
                .findOneDraftById(draftKey.toString());

            final CatalogueResponse<CatalogueFeature> catalogueResponse = e.getBody();

            if (!catalogueResponse.isSuccess()) {
                throw CatalogueServiceException.fromService(catalogueResponse.getMessage());
            }

            // Convert feature to catalogue item
            final CatalogueItemDraftDto item = new CatalogueItemDraftDto(catalogueResponse.getResult());

            // Inject publisher details
            final PublisherDto publisher = this.providerRepository.findOneByKey(item.getPublisherId()).toPublisherDto();

            item.setPublisher(publisher);

            // Compute effective pricing models
            this.refreshPricingModels(item);

            return item;
        } catch (final FeignException fex) {
            // Convert 404 errors to empty results
            if (fex.status() == HttpStatus.NOT_FOUND.value()) {
                return null;
            }

            logger.error("[Feign Client][Catalogue] Operation has failed", fex);

            throw new CatalogueServiceException(CatalogueServiceMessageCode.CATALOGUE_SERVICE, fex.getMessage());
        } catch (final Exception ex) {
            logger.error("[Catalogue] Operation has failed", ex);

            throw CatalogueServiceException.wrap(ex);
        }
    }

    public void updateDraftTemp(UUID draftKey, CatalogueItemCommandDto command) {
        try {
            // Inject provider and asset identifiers
            command.setPublisherKey(command.getPublisherKey());
            command.setAssetKey(draftKey);

            // Create feature
            final CatalogueFeature feature = command.toFeature();

            // Update draft
            this.catalogueClient.getObject().updateDraft(draftKey.toString(), feature);
        } catch (final Exception ex) {
            logger.error("[Catalogue] Operation has failed", ex);

            throw CatalogueServiceException.wrap(ex);
        }
    }

    public void setDraftStatusTemp(UUID draftKey, CatalogueClientSetStatusCommandDto command) throws CatalogueServiceException {
        try {
            this.catalogueClient.getObject().setDraftStatus(draftKey.toString(), command.getStatus().getValue());
        } catch (final Exception ex) {
            logger.error("[Catalogue] Operation has failed", ex);

            throw CatalogueServiceException.wrap(ex);
        }
    }

    public void deleteDraftTemp(UUID draftKey) throws CatalogueServiceException {
        try {
            this.catalogueClient.getObject().deleteDraft(draftKey.toString());
        } catch (final Exception ex) {
            logger.error("[Catalogue] Operation has failed", ex);

            throw CatalogueServiceException.wrap(ex);
        }
    }
    
}
