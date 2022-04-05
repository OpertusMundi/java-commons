package eu.opertusmundi.common.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.camunda.bpm.engine.rest.dto.VariableValueDto;
import org.camunda.bpm.engine.rest.dto.runtime.ProcessInstanceDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import eu.opertusmundi.common.domain.AssetAdditionalResourceEntity;
import eu.opertusmundi.common.domain.AssetResourceEntity;
import eu.opertusmundi.common.domain.FavoriteEntity;
import eu.opertusmundi.common.domain.MasterSectionHistoryEntity;
import eu.opertusmundi.common.domain.ProviderTemplateContractHistoryEntity;
import eu.opertusmundi.common.feign.client.CatalogueFeignClient;
import eu.opertusmundi.common.model.PageRequestDto;
import eu.opertusmundi.common.model.PageResultDto;
import eu.opertusmundi.common.model.RequestContext;
import eu.opertusmundi.common.model.account.ProviderDto;
import eu.opertusmundi.common.model.analytics.AssetViewRecord;
import eu.opertusmundi.common.model.analytics.EnumAssetViewSource;
import eu.opertusmundi.common.model.asset.ResourceDto;
import eu.opertusmundi.common.model.catalogue.CatalogueResult;
import eu.opertusmundi.common.model.catalogue.CatalogueServiceException;
import eu.opertusmundi.common.model.catalogue.CatalogueServiceMessageCode;
import eu.opertusmundi.common.model.catalogue.client.CatalogueAssetQuery;
import eu.opertusmundi.common.model.catalogue.client.CatalogueDraftQuery;
import eu.opertusmundi.common.model.catalogue.client.CatalogueHarvestCommandDto;
import eu.opertusmundi.common.model.catalogue.client.CatalogueItemDetailsDto;
import eu.opertusmundi.common.model.catalogue.client.CatalogueItemDraftDto;
import eu.opertusmundi.common.model.catalogue.client.CatalogueItemDto;
import eu.opertusmundi.common.model.catalogue.client.EnumAssetType;
import eu.opertusmundi.common.model.catalogue.client.EnumCatalogueType;
import eu.opertusmundi.common.model.catalogue.elastic.ElasticAssetQuery;
import eu.opertusmundi.common.model.catalogue.elastic.ElasticAssetQueryResult;
import eu.opertusmundi.common.model.catalogue.elastic.ElasticServiceException;
import eu.opertusmundi.common.model.catalogue.server.CatalogueCollection;
import eu.opertusmundi.common.model.catalogue.server.CatalogueFeature;
import eu.opertusmundi.common.model.catalogue.server.CatalogueResponse;
import eu.opertusmundi.common.model.contract.ContractDto;
import eu.opertusmundi.common.model.contract.ContractTermDto;
import eu.opertusmundi.common.model.pricing.BasePricingModelCommandDto;
import eu.opertusmundi.common.model.pricing.EffectivePricingModelDto;
import eu.opertusmundi.common.model.workflow.EnumProcessInstanceVariable;
import eu.opertusmundi.common.model.workflow.EnumWorkflow;
import eu.opertusmundi.common.repository.AccountRecentSearchRepository;
import eu.opertusmundi.common.repository.AssetAdditionalResourceRepository;
import eu.opertusmundi.common.repository.AssetResourceRepository;
import eu.opertusmundi.common.repository.FavoriteRepository;
import eu.opertusmundi.common.repository.ProviderRepository;
import eu.opertusmundi.common.repository.contract.ProviderTemplateContractHistoryRepository;
import eu.opertusmundi.common.service.integration.DataProviderManager;
import eu.opertusmundi.common.util.BpmEngineUtils;
import eu.opertusmundi.common.util.BpmInstanceVariablesBuilder;
import feign.FeignException;

@Service
public class DefaultCatalogueService implements CatalogueService {

    private static final Logger logger = LoggerFactory.getLogger(DefaultCatalogueService.class);

    private static final Logger assetViewLogger = LoggerFactory.getLogger("ASSET_VIEWS");

    private static final String DRAFT_PUBLISHED_STATUS = "published";

    @Value("${opertusmundi.contract.icons}")
    private String iconFolder;

    @Autowired
    private Tracer tracer;

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private FavoriteRepository favoriteRepository;

    @Autowired
    private ProviderRepository providerRepository;

    @Autowired
    private AccountRecentSearchRepository recentSearchRepository;

    @Autowired
    private StatisticsService statisticsService;

    @Autowired
    private AssetResourceRepository assetResourceRepository;

    @Autowired
    private AssetAdditionalResourceRepository assetAdditionalResourceRepository;

    @Autowired
    private ProviderTemplateContractHistoryRepository providerContractRepository;

    @Autowired
    private BpmEngineUtils bpmEngine;

    @Autowired
    private ObjectProvider<CatalogueFeignClient> catalogueClient;

    @Autowired(required = false)
    private ElasticSearchService elasticSearchService;

    @Autowired
    private QuotationService quotationService;

    @Autowired
    private DataProviderManager dataProviderManager;

    @Override
    public CatalogueResult<CatalogueItemDto> findAll(RequestContext ctx, CatalogueAssetQuery request) throws CatalogueServiceException {
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
            final CatalogueResult<CatalogueItemDto> response = this.featureCollectionToCatalogueResult(
                request.getPage(), request.getSize(),
                catalogueResponse.getResult().getItems(), catalogueResponse.getResult().getTotal(),
                (item) -> new CatalogueItemDto(item),
                true
            );

            // Log asset views
            this.logViews(ctx, response.getResult().getItems(), request.getQuery(), EnumAssetViewSource.SEARCH);

            return response;
        } catch (final FeignException fex) {
            // Convert 404 errors to empty results
            if (fex.status() == HttpStatus.NOT_FOUND.value()) {
                return CatalogueResult.empty(request.toPageRequest());
            }

            logger.error("Operation has failed", fex);

            throw new CatalogueServiceException(CatalogueServiceMessageCode.CATALOGUE_SERVICE, fex.getMessage(), fex);
        } catch (final Exception ex) {
            logger.error("Operation has failed", ex);

            throw CatalogueServiceException.wrap(ex);
        }
    }

    @Override
    public CatalogueResult<CatalogueItemDto> findAllRelated(RequestContext ctx, String id) throws CatalogueServiceException {
        Assert.isTrue(!StringUtils.isBlank(id), "Expected a non-null identifier");

        try {
            // Catalogue service data page index is 1-based
            final ResponseEntity<CatalogueResponse<List<CatalogueFeature>>> e = this.catalogueClient.getObject().findAllRelated(id);

            final CatalogueResponse<List<CatalogueFeature>> catalogueResponse = e.getBody();

            if (!catalogueResponse.isSuccess()) {
                throw CatalogueServiceException.fromService(catalogueResponse.getMessage());
            }

            // Process response
            final List<CatalogueFeature> features = catalogueResponse.getResult().stream()
                .filter(f -> !f.getId().equals(id))
                .collect(Collectors.toList());

            final CatalogueResult<CatalogueItemDto> response = this.featureCollectionToCatalogueResult(
                0, features.size(),
                features, features.size(),
                (item) -> new CatalogueItemDto(item),
                true
            );

            // Log asset views
            this.logViews(ctx, response.getResult().getItems(), null, EnumAssetViewSource.REFERENCE);

            return response;
        } catch (final FeignException fex) {
            // Convert 404 errors to empty results
            if (fex.status() == HttpStatus.NOT_FOUND.value()) {
                return CatalogueResult.empty(PageRequestDto.defaultValue());
            }

            logger.error("Operation has failed", fex);

            throw new CatalogueServiceException(CatalogueServiceMessageCode.CATALOGUE_SERVICE, fex.getMessage(), fex);
        } catch (final Exception ex) {
            logger.error("Operation has failed", ex);

            throw CatalogueServiceException.wrap(ex);
        }
    }

    @Override
    public CatalogueResult<CatalogueItemDto> findAllAdvanced(
        RequestContext ctx, ElasticAssetQuery request
    ) throws CatalogueServiceException {
        Assert.notNull(request, "Expected a non-null request");

        try {
            if (this.elasticSearchService == null) {
                return this.featureCollectionToCatalogueResult(
                    request.getPage().orElse(0), request.getSize().orElse(10),
                    Collections.emptyList(), 0,
                    (item) -> new CatalogueItemDto(item),
                    true
                );
            }

            final ElasticAssetQueryResult result = elasticSearchService.searchAssets(request);

            // Process response
            final CatalogueResult<CatalogueItemDto> response = this.featureCollectionToCatalogueResult(
                request.getPage().orElse(0), request.getSize().orElse(10),
                result.getAssets(), result.getTotal(),
                (item) -> new CatalogueItemDto(item),
                true
            );

            // Log asset views
            this.logViews(ctx, response.getResult().getItems(), request.getText(), EnumAssetViewSource.SEARCH);

            // Log user search term
            if (ctx != null && ctx.getAccount() != null && !StringUtils.isBlank(request.getText())) {
                this.recentSearchRepository.add(ctx.getAccount().getId(), request.getText());
            }

            return response;
        } catch (final Exception ex) {
            logger.error("Operation has failed", ex);

            throw CatalogueServiceException.wrap(ex);
        }
    }

    @Override
    public List<CatalogueItemDetailsDto> findAllById(String[] id) throws CatalogueServiceException {
        Assert.notEmpty(id, "Expected a non-empty array of identifiers");

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

            return catalogueResponse.getResult().stream()
                .map(feature -> this.featureToItem(null, feature, feature.getProperties().getPublisherId(), false))
                .collect(Collectors.toList());
        } catch (final CatalogueServiceException ex) {
            throw ex;
        } catch (final FeignException fex) {
            logger.error("[Feign Client][Catalogue] Operation has failed", fex);

            throw new CatalogueServiceException(CatalogueServiceMessageCode.CATALOGUE_SERVICE, fex.getMessage(), fex);
        } catch (final Exception ex) {
            logger.error("Operation has failed", ex);

            throw CatalogueServiceException.wrap(ex);
        }
    }

    @Override
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
            final CatalogueResult<CatalogueItemDraftDto> response = this.featureCollectionToCatalogueResult(
                request.getPage(), request.getSize(),
                catalogueResponse.getResult().getItems(), catalogueResponse.getResult().getTotal(),
                (item) -> new CatalogueItemDraftDto(item),
                true
            );

            return response;
        } catch (final FeignException fex) {
            // Convert 404 errors to empty results
            if (fex.status() == HttpStatus.NOT_FOUND.value()) {
                return CatalogueResult.empty(request.toPageRequest());
            }

            logger.error("Operation has failed", fex);

            throw new CatalogueServiceException(CatalogueServiceMessageCode.CATALOGUE_SERVICE, fex.getMessage(), fex);
        } catch (final Exception ex) {
            logger.error("Operation has failed", ex);

            throw CatalogueServiceException.wrap(ex);
        }
    }

    @Override
    public CatalogueItemDetailsDto findOne(
        RequestContext ctx, String id, UUID publisherKey, boolean includeAutomatedMetadata
    ) throws CatalogueServiceException {
        try {
            final ResponseEntity<CatalogueResponse<CatalogueFeature>> e = this.catalogueClient.getObject().findOneById(id);

            final CatalogueResponse<CatalogueFeature> catalogueResponse = e.getBody();

            if (!catalogueResponse.isSuccess()) {
                throw CatalogueServiceException.fromService(catalogueResponse.getMessage());
            }

            // Convert feature to catalogue item
            final CatalogueFeature        feature = catalogueResponse.getResult();
            final CatalogueItemDetailsDto item    = this.featureToItem(ctx, feature, publisherKey, includeAutomatedMetadata);

            return item;
        } catch (final FeignException fex) {
            // Convert 404 errors to empty results
            if (fex.status() == HttpStatus.NOT_FOUND.value()) {
                return null;
            }

            logger.error("Operation has failed", fex);

            throw new CatalogueServiceException(CatalogueServiceMessageCode.CATALOGUE_SERVICE, fex.getMessage(), fex);
        } catch (final Exception ex) {
            logger.error("Operation has failed", ex);

            throw CatalogueServiceException.wrap(ex);
        }
    }

    @Override
    public CatalogueItemDetailsDto findOne(
        RequestContext ctx, String id, String version, UUID publisherKey, boolean includeAutomatedMetadata
    ) throws CatalogueServiceException {
        try {
            final ResponseEntity<CatalogueResponse<CatalogueFeature>> e = this.catalogueClient.getObject().findOneByIdAndVersion(id, version);

            final CatalogueResponse<CatalogueFeature> catalogueResponse = e.getBody();

            if (!catalogueResponse.isSuccess()) {
                throw CatalogueServiceException.fromService(catalogueResponse.getMessage());
            }

            // Convert feature to catalogue item
            final CatalogueFeature        feature = catalogueResponse.getResult();
            final CatalogueItemDetailsDto item    = this.featureToItem(ctx, feature, publisherKey, includeAutomatedMetadata);

            return item;
        } catch (final FeignException fex) {
            // Convert 404 errors to empty results
            if (fex.status() == HttpStatus.NOT_FOUND.value()) {
                return null;
            }

            logger.error("Operation has failed", fex);

            throw new CatalogueServiceException(CatalogueServiceMessageCode.CATALOGUE_SERVICE, fex.getMessage(), fex);
        } catch (final Exception ex) {
            logger.error("Operation has failed", ex);

            throw CatalogueServiceException.wrap(ex);
        }
    }

    private CatalogueItemDetailsDto featureToItem(
        RequestContext ctx, CatalogueFeature feature, UUID publisherKey, boolean includeAutomatedMetadata
    ) {
        final CatalogueItemDetailsDto item = new CatalogueItemDetailsDto(feature);

        // Filter automated metadata
        if (!includeAutomatedMetadata) {
            item.setAutomatedMetadata(null);
            item.setVisibility(null);
        } else if (item.getVisibility() != null && !item.getAutomatedMetadata().isNull()) {
            final ArrayNode metadataArray = (ArrayNode) item.getAutomatedMetadata();
            for (int i = 0; i < metadataArray.size(); i++) {
                final ObjectNode metadata = (ObjectNode) metadataArray.get(i);
                for (final String prop : item.getVisibility()) {
                    metadata.putNull(prop);
                }
            }
        }

        // Filter ingestion information
        if (!item.getPublisherId().equals(publisherKey)) {
            item.setIngestionInfo(null);
        }

        // Inject publisher details
        final ProviderDto publisher = this.providerRepository.findOneByKey(item.getPublisherId()).getProvider().toProviderDto(true);
        item.setPublisher(publisher);

        // Inject contract details
        this.setContract(item, feature);

        // Consolidate data from asset repository
        final List<AssetResourceEntity> resources = this.assetResourceRepository
            .findAllResourcesByAssetPid(item.getId());

        resources.stream()
            .forEach(r -> {
                final ResourceDto resource = item.getResources().stream()
                    .filter(r1 -> r1.getId().equals(r.getKey()))
                    .findFirst()
                    .orElse(null);

                if (resource != null) {
                    // TODO: Check that resource file exists ...
                }
            });

        final List<AssetAdditionalResourceEntity> additionalResources = this.assetAdditionalResourceRepository
            .findAllResourcesByAssetPid(item.getId());

        additionalResources.stream()
            .forEach(r -> {
                final ResourceDto resource = item.getResources().stream()
                    .filter(r1 -> r1.getId().equals(r.getKey()))
                    .findFirst()
                    .orElse(null);

                if (resource != null) {
                    // TODO: Check that resource file exists ...
                }
            });

        // Compute effective pricing models
        this.refreshPricingModels(item);

        // Set favorite flag for asset and provider
        final Integer userId = ctx == null || ctx.getAccount() == null ? null : ctx.getAccount().getId();

        if (userId != null) {
            final FavoriteEntity assetFavorite    = this.favoriteRepository.findOneAsset(userId, item.getId()).orElse(null);
            final FavoriteEntity providerFavorite = this.favoriteRepository.findOneProvider(userId, item.getPublisherId()).orElse(null);
            if (assetFavorite != null) {
                item.setFavorite(assetFavorite.getKey());
            }

            if (providerFavorite != null) {
                item.getPublisher().setFavorite(providerFavorite.getKey());
            }
        }

        // Log asset views
        this.logView(ctx,  item, null, EnumAssetViewSource.VIEW);

        return item;
    }

    @Override
    public CatalogueFeature findOneFeature(String id) throws CatalogueServiceException {
        try {
            final ResponseEntity<CatalogueResponse<CatalogueFeature>> e = this.catalogueClient.getObject().findOneById(id);
            final CatalogueResponse<CatalogueFeature> catalogueResponse = e.getBody();

            if (!catalogueResponse.isSuccess()) {
                throw new CatalogueServiceException(
                    CatalogueServiceMessageCode.CATALOGUE_SERVICE,
                    catalogueResponse.getMessage().toString()
                );
            }

            return catalogueResponse.getResult();
        } catch (final FeignException fex) {
            // Convert 404 errors to empty results
            if (fex.status() == HttpStatus.NOT_FOUND.value()) {
                return null;
            }

            logger.error("Operation has failed", fex);

            throw new CatalogueServiceException(CatalogueServiceMessageCode.CATALOGUE_SERVICE, fex.getMessage(), fex);
        } catch (final Exception ex) {
            logger.error("Operation has failed", ex);

            throw CatalogueServiceException.wrap(ex);
        }
    }

    @Override
    public CatalogueFeature findOneHistoryFeature(String id, String version) throws CatalogueServiceException {
        try {
            final ResponseEntity<CatalogueResponse<CatalogueFeature>> e = this.catalogueClient.getObject().findOneByIdAndVersion(id, version);

            final CatalogueResponse<CatalogueFeature> catalogueResponse = e.getBody();

            if (!catalogueResponse.isSuccess()) {
                throw new CatalogueServiceException(
                    CatalogueServiceMessageCode.CATALOGUE_SERVICE,
                    catalogueResponse.getMessage().toString()
                );
            }

            return catalogueResponse.getResult();
        } catch (final FeignException fex) {
            // Convert 404 errors to empty results
            if (fex.status() == HttpStatus.NOT_FOUND.value()) {
                return null;
            }

            logger.error("Operation has failed", fex);

            throw new CatalogueServiceException(CatalogueServiceMessageCode.CATALOGUE_SERVICE, fex.getMessage(), fex);
        } catch (final Exception ex) {
            logger.error("Operation has failed", ex);

            throw CatalogueServiceException.wrap(ex);
        }
    }

    @Override
    public CatalogueFeature findOneHarvested(String id) {
        try {
            final ResponseEntity<CatalogueResponse<CatalogueFeature>> e = this.catalogueClient.getObject().findOneHarvestedItemById(id);
            final CatalogueResponse<CatalogueFeature> catalogueResponse = e.getBody();

            if (!catalogueResponse.isSuccess()) {
                throw new CatalogueServiceException(
                    CatalogueServiceMessageCode.CATALOGUE_SERVICE,
                    catalogueResponse.getMessage().toString()
                );
            }

            return catalogueResponse.getResult();
        } catch (final FeignException fex) {
            // Convert 404 errors to empty results
            if (fex.status() == HttpStatus.NOT_FOUND.value()) {
                return null;
            }

            logger.error("Operation has failed", fex);

            throw new CatalogueServiceException(CatalogueServiceMessageCode.CATALOGUE_SERVICE, fex.getMessage(), fex);
        } catch (final Exception ex) {
            logger.error("Operation has failed", ex);

            throw CatalogueServiceException.wrap(ex);
        }
    }

    @Override
    public void harvestCatalogue(CatalogueHarvestCommandDto command) throws CatalogueServiceException {
        try {
            // Check if workflow exists
            final String businessKey = String.format("%s-%s", command.getUserKey(), command.getUrl());

            final ProcessInstanceDto instance = this.bpmEngine.findInstance(businessKey);

            if (instance == null) {
                // Set defaults
                if (command.getType() == null) {
                    command.setType(EnumCatalogueType.CSW);
                }
                // Set variables
                final Map<String, VariableValueDto> variables = BpmInstanceVariablesBuilder.builder()
                    .variableAsString(EnumProcessInstanceVariable.START_USER_KEY.getValue(), command.getUserKey().toString())
                    .variableAsString("userKey", command.getUserKey().toString())
                    .variableAsString("catalogueUrl", command.getUrl())
                    .variableAsString("catalogueType", command.getType().toString())
                    .build();

                this.bpmEngine.startProcessDefinitionByKey(EnumWorkflow.CATALOGUE_HARVEST, businessKey, variables, true);
            }
        } catch (final FeignException fex) {
            logger.error("Operation has failed", fex);

            throw CatalogueServiceException.wrap(fex);
        } catch (final Exception ex) {
            logger.error("Operation has failed", ex);

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
            final CatalogueResult<CatalogueItemDto> response = this.featureCollectionToCatalogueResult(
                pageIndex, pageSize,
                catalogueResponse.getResult().getItems(), catalogueResponse.getResult().getTotal(),
                (item) -> new CatalogueItemDto(item),
                false
            );

            return response;
        } catch (final FeignException fex) {
            // Convert 404 errors to empty results
            if (fex.status() == HttpStatus.NOT_FOUND.value()) {
                return CatalogueResult.empty(PageRequestDto.of(pageIndex, pageSize));
            }

            logger.error("Operation has failed", fex);

            throw new CatalogueServiceException(CatalogueServiceMessageCode.CATALOGUE_SERVICE, fex.getMessage(), fex);
        } catch (final Exception ex) {
            logger.error("Operation has failed", ex);

            throw CatalogueServiceException.wrap(ex);
        }
    }

    @Override
    public void unpublish(UUID publisherKey, String pid) throws CatalogueServiceException {
        try {
            // Remove all references from favorites
            this.favoriteRepository.deleteAllByAssetId(pid);

            // Remove asset from Elasticsearch
            if (this.elasticSearchService != null) {
                final CatalogueFeature feature = this.elasticSearchService.findAsset(pid);

                if (feature != null) {
                    if (!feature.getProperties().getPublisherId().equals(publisherKey)) {
                        throw new CatalogueServiceException(CatalogueServiceMessageCode.PUBLISHER_ASSET_OWNERSHIP);
                    }

                    this.elasticSearchService.removeAsset(pid);
                }
            }

            // Deactivate related statistics
            this.statisticsService.updateStatisticsUnpublishAsset(pid);

            // Remove asset from the catalogue
            final CatalogueItemDetailsDto item = this.findOne(null, pid, publisherKey, false);
            if (item != null) {
                this.catalogueClient.getObject().deletePublished(pid);
            }
        } catch (final CatalogueServiceException ex) {
            throw ex;
        } catch (final Exception ex) {
            logger.error("Operation has failed", ex);

            throw CatalogueServiceException.wrap(ex);
        }
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
    private <T extends CatalogueItemDto> CatalogueResult<T> featureCollectionToCatalogueResult(
        int pageIndex, int pageSize,
        List<CatalogueFeature> features, long total,
        Function<CatalogueFeature, T> converter,
        boolean includeProviders
    ) {
        // Convert features to items
        final List<T> items = features.stream()
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
            total
        );

        // Get all publishers in the result
        List<ProviderDto> publishers = null;

        if (includeProviders) {
            final Span span = this.tracer.nextSpan().name("database-publisher").start();

            try {
                final UUID[] publisherKeys = items.stream().map(i -> i.getPublisherId()).distinct().toArray(UUID[]::new);

                publishers = this.providerRepository.findAllByKey(publisherKeys).stream()
                    .map(a -> a.getProvider().toProviderDto(true))
                    .filter(p -> p != null)
                    .collect(Collectors.toList());

                if (publisherKeys.length != publishers.size()) {
                    throw new CatalogueServiceException(
                        CatalogueServiceMessageCode.PUBLISHER_NOT_FOUND,
                        "All publishers must exist"
                    );
                }
            } finally {
                span.end();
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
        // Inject pricing models from external data providers
        dataProviderManager.updatePricingModels(item);

        final List<BasePricingModelCommandDto> models = item.getPricingModels();

        if (models.isEmpty()) {
            return;
        }

        final List<EffectivePricingModelDto> quotations = quotationService.createQuotation(item);

        item.setEffectivePricingModels(quotations);
    }

    @Override
    public void publish(CatalogueFeature feature) throws CatalogueServiceException {
        // Draft may already be created
        CatalogueFeature existingDraft = null;
        try {
            ResponseEntity<CatalogueResponse<CatalogueFeature>> draftResponse;
            CatalogueResponse<CatalogueFeature>                 draftBody;

            draftResponse = this.catalogueClient.getObject().findOneDraftById(feature.getId());
            draftBody     = draftResponse.getBody();

            if (draftBody.isSuccess()) {
                existingDraft = draftBody.getResult();
            }
        } catch (final FeignException fex) {
            // 404 errors are valid responses
            if (fex.status() != HttpStatus.NOT_FOUND.value()) {
                throw fex;
            }
        }

        // Asset may already be published
        CatalogueFeature existingAsset = null;
        try {
            final ResponseEntity<CatalogueResponse<CatalogueFeature>> assetResponse;
            final CatalogueResponse<CatalogueFeature>                 assetBody;

            assetResponse = this.catalogueClient.getObject().findOneById(feature.getId());
            assetBody     = assetResponse.getBody();

            if (assetBody.isSuccess()) {
                existingAsset = assetBody.getResult();
            }
        } catch (final FeignException fex) {
            // 404 errors are valid responses
            if (fex.status() != HttpStatus.NOT_FOUND.value()) {
                throw fex;
            }
        }

        try {
            // Create a draft record first and then set its status to published.
            if (existingAsset == null) {
                if (existingDraft == null) {
                    this.catalogueClient.getObject().createDraft(feature);
                }

                this.catalogueClient.getObject().setDraftStatus(feature.getId(), DRAFT_PUBLISHED_STATUS);
            }

            // Query new published item from the catalogue. Catalogue may inject
            // additional information such as versions
            final CatalogueFeature publishedFeature = this.catalogueClient.getObject()
                .findOneById(feature.getId())
                .getBody()
                .getResult();

            final CatalogueItemDetailsDto publishedItem = this.featureToItem(
                null, publishedFeature, feature.getProperties().getPublisherId(), false
            );

            // Update statistics
            this.statisticsService.updateStatisticsPublishAsset(publishedItem);

            if (this.elasticSearchService != null) {
                // For tabular assets, reset geometry
                if (EnumAssetType.fromString(publishedFeature.getProperties().getType()) == EnumAssetType.TABULAR) {
                    publishedFeature.setGeometry(null);
                }
                this.elasticSearchService.addAsset(publishedFeature);
            }
        } catch (final ElasticServiceException ex) {
            logger.error("Failed to publish asset to elastic", ex);

            throw new CatalogueServiceException(CatalogueServiceMessageCode.ELASTIC_SERVICE, "Failed to publish asset to elastic", ex);
        } catch (final FeignException fex) {
            logger.error("Operation has failed", fex);

            throw new CatalogueServiceException(CatalogueServiceMessageCode.CATALOGUE_SERVICE, "Failed to publish asset", fex);
        }
    }

    private void logView(RequestContext ctx, CatalogueItemDetailsDto item, String query, EnumAssetViewSource source) {
        if (ctx == null || ctx.isIgnoreLogging()) {
            // Ignore request. A request initialized internally, may have a
            // null context e.g. a quotation request
            return;
        }
        final AssetViewRecord r = AssetViewRecord.from(ctx, item, query, source);

        try {
            assetViewLogger.info(objectMapper.writeValueAsString(r));
        } catch (final JsonProcessingException e) {
            logger.error("Failed to serialize object. [type={}]", AssetViewRecord.class);
        }
    }

    private void logViews(RequestContext ctx, List<CatalogueItemDto> items, String query, EnumAssetViewSource source) {
        if (ctx == null || ctx.isIgnoreLogging()) {
            return;
        }

        items.stream().forEach(item -> {
            final AssetViewRecord r = AssetViewRecord.from(ctx, item, query, source);

            try {
                assetViewLogger.info(objectMapper.writeValueAsString(r));
            } catch (final JsonProcessingException e) {
                logger.error("Failed to serialize object. [type={}]", AssetViewRecord.class);
            }
        });
    }

    private void setContract(CatalogueItemDetailsDto item, CatalogueFeature feature) {
        final ProviderTemplateContractHistoryEntity providerTemplate = this.providerContractRepository.findByIdAndVersion(
            feature.getProperties().getPublisherId(),
            feature.getProperties().getContractTemplateId(),
            feature.getProperties().getContractTemplateVersion()
        ).orElse(null);

        final ContractDto contract = providerTemplate.toSimpleDto();

        // Inject contract terms and conditions
        providerTemplate.getSections().stream()
            .filter(s -> s.getOption() != null)
            .map(s -> Pair.<Integer, MasterSectionHistoryEntity>of(s.getOption(), s.getMasterSection()))
            .map(p -> p.getRight().getOptions().get(p.getLeft()))
            .filter(s -> s.getIcon() != null)
            .map(s -> {
                final Path path = Paths.get(iconFolder, s.getIcon().getFile());
                try (final InputStream fileStream = resourceLoader.getResource(path.toString()).getInputStream()) {
                    final byte[] data = IOUtils.toByteArray(fileStream);
                    return ContractTermDto.of(s.getIcon(), s.getIcon().getCategory(), data, s.getShortDescription());
                } catch (final IOException ex) {
                    logger.warn(String.format("Failed to load resource [icon=%s, path=%s]", s.getIcon(), path), ex);
                }
                return ContractTermDto.of(s.getIcon(), s.getIcon().getCategory(), null, s.getShortDescription());
            })
            .forEach(contract.getTerms()::add);

        item.setContract(contract);
    }

}
