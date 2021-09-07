package eu.opertusmundi.common.service;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.ElasticsearchStatusException;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.ingest.DeletePipelineRequest;
import org.elasticsearch.action.ingest.GetPipelineRequest;
import org.elasticsearch.action.ingest.GetPipelineResponse;
import org.elasticsearch.action.ingest.PutPipelineRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.client.transform.DeleteTransformRequest;
import org.elasticsearch.client.transform.GetTransformStatsRequest;
import org.elasticsearch.client.transform.GetTransformStatsResponse;
import org.elasticsearch.client.transform.PutTransformRequest;
import org.elasticsearch.client.transform.StartTransformRequest;
import org.elasticsearch.client.transform.StartTransformResponse;
import org.elasticsearch.client.transform.StopTransformRequest;
import org.elasticsearch.client.transform.StopTransformResponse;
import org.elasticsearch.client.transform.transforms.DestConfig;
import org.elasticsearch.client.transform.transforms.QueryConfig;
import org.elasticsearch.client.transform.transforms.SourceConfig;
import org.elasticsearch.client.transform.transforms.SyncConfig;
import org.elasticsearch.client.transform.transforms.TimeSyncConfig;
import org.elasticsearch.client.transform.transforms.TransformConfig;
import org.elasticsearch.client.transform.transforms.TransformStats;
import org.elasticsearch.client.transform.transforms.TransformStats.State;
import org.elasticsearch.client.transform.transforms.pivot.AggregationConfig;
import org.elasticsearch.client.transform.transforms.pivot.GroupConfig;
import org.elasticsearch.client.transform.transforms.pivot.PivotConfig;
import org.elasticsearch.client.transform.transforms.pivot.TermsGroupSource;
import org.elasticsearch.common.bytes.BytesArray;
import org.elasticsearch.common.geo.ShapeRelation;
import org.elasticsearch.common.geo.builders.EnvelopeBuilder;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.geometry.Rectangle;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchAllQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.AggregatorFactories;
import org.elasticsearch.search.aggregations.bucket.composite.CompositeAggregation;
import org.elasticsearch.search.aggregations.bucket.composite.CompositeAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.composite.CompositeValuesSourceBuilder;
import org.elasticsearch.search.aggregations.bucket.composite.TermsValuesSourceBuilder;
import org.elasticsearch.search.aggregations.metrics.ParsedSum;
import org.elasticsearch.search.aggregations.metrics.SumAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.ScoreSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.locationtech.jts.geom.Coordinate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ResourceLoader;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.opertusmundi.common.config.ElasticConfiguration;
import eu.opertusmundi.common.model.EnumSortingOrder;
import eu.opertusmundi.common.model.analytics.AssetViewQuery;
import eu.opertusmundi.common.model.analytics.BaseQuery;
import eu.opertusmundi.common.model.analytics.DataPoint;
import eu.opertusmundi.common.model.analytics.DataSeries;
import eu.opertusmundi.common.model.analytics.EnumAssetViewSource;
import eu.opertusmundi.common.model.analytics.ProfileRecord;
import eu.opertusmundi.common.model.catalogue.client.EnumTopicCategory;
import eu.opertusmundi.common.model.catalogue.client.EnumType;
import eu.opertusmundi.common.model.catalogue.elastic.CreateIndexCommand;
import eu.opertusmundi.common.model.catalogue.elastic.ElasticAssetQuery;
import eu.opertusmundi.common.model.catalogue.elastic.ElasticAssetQueryResult;
import eu.opertusmundi.common.model.catalogue.elastic.ElasticServiceException;
import eu.opertusmundi.common.model.catalogue.elastic.EnumElasticSearchDatasetSize;
import eu.opertusmundi.common.model.catalogue.elastic.EnumElasticSearchSortField;
import eu.opertusmundi.common.model.catalogue.elastic.PipelineDefinition;
import eu.opertusmundi.common.model.catalogue.elastic.TransformDefinition;
import eu.opertusmundi.common.model.catalogue.server.CatalogueFeature;
import io.jsonwebtoken.lang.Assert;

@ConditionalOnProperty(name = "opertusmundi.elastic.enabled")
@Service
public class DefaultElasticSearchService implements ElasticSearchService {

    private static final Logger logger = LogManager.getLogger(DefaultElasticSearchService.class);

    @Value("${opertusmundi.elastic.create-on-startup:false}")
    private boolean createOnStartup;

    /**
     * Maximum number of buckets returned by aggregation queries
     *
     * @see https://www.elastic.co/guide/en/elasticsearch/reference/current/search-aggregations-bucket.html
     */
    @Value("${opertusmundi.elastic.max-bucket-count:1000}")
    private int maxBucketCount;

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    private ElasticConfiguration configuration;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RestHighLevelClient client;

    @PostConstruct
    private void init() {
        this.initialize(createOnStartup);
    }

    @PreDestroy
    private void shutdown() {
        this.close();
    }

    /**
     * Initializes all registered object definitions
     *
     * @param createOnStartup Create missing objects on startup
     * @throws ElasticServiceException
     */
    @Retryable(include = {ElasticServiceException.class}, maxAttempts = 4, backoff = @Backoff(delay = 2000, maxDelay = 20000))
    protected void initialize(boolean createOnStartup) throws ElasticServiceException {
        // Check and (optionally) create indices
        configuration.getIndices().stream().forEach(def -> {
            if (def != null && def.isValid() && !this.checkIndex(def.getName())) {
                if (createOnStartup) {
                    this.createIndex(def);
                } else {
                    throw new ElasticServiceException(String.format("Index not found [name=%s]", def.getName()));
                }
            }
        });

        // Check and (optionally) create pipelines
        final PipelineDefinition pipelineDef = configuration.getAutoTimestampPipeline();

        if (pipelineDef != null && !this.checkPipeline(pipelineDef)) {
            if (createOnStartup) {
                this.createPipeline(configuration.getAutoTimestampPipeline());
            } else {
                throw new ElasticServiceException(String.format("Pipeline not found [name=%s]", pipelineDef.getName()));
            }
        }

        // Check and (optionally) create transforms
        final TransformDefinition transformDef = configuration.getAssetViewAggregateTransform();

        if (transformDef != null && transformDef.isValid()) {
            final TransformStats stats = this.getTransformStats(transformDef.getName());

            if (stats == null) {
                if(createOnStartup) {
                    this.createAssetViewAggregationTransform(
                        transformDef.getName(), transformDef.getSourceIndex(), transformDef.getDestIndex()
                    );

                    this.startTransform(transformDef.getName());
                } else {
                    throw new ElasticServiceException(String.format("Transform not found [name=%s]", transformDef.getName()));
                }
            } else if (stats.getState() != State.STARTED) {
                if(createOnStartup) {
                    this.startTransform(transformDef.getName());
                } else {
                    throw new ElasticServiceException(String.format(
                        "Transform is not started [name=%s, state=%s, expected=%s]",
                        transformDef.getName(), stats.getState(), State.STARTED
                    ));
                }
            }
        }
    }

    @Override
    public void close() {
        try {
            client.close();
            client = null;
        } catch (final IOException ex) {
            logger.error("Failed to close elastic search client", ex);
        }
    }

    @Override
    public boolean checkIndex(String name) {
        try {
            final GetIndexRequest request = new GetIndexRequest(name);
            final boolean         exists  = client.indices().exists(request, RequestOptions.DEFAULT);

            return exists;
        } catch (final Exception ex) {
            throw new ElasticServiceException(String.format("Failed to query index [name=%s]", name), ex);
        }
    }

    @Override
    public boolean createIndex(String name, String settingsResource, String mappingsResource) throws ElasticServiceException {
        try (
            final InputStream settingsIs = resourceLoader.getResource(settingsResource).getInputStream();
            final InputStream mappingIs  = resourceLoader.getResource(mappingsResource).getInputStream();
        ) {
            final String settings = IOUtils.toString(settingsIs, StandardCharsets.UTF_8);
            final String mappings = IOUtils.toString(mappingIs, StandardCharsets.UTF_8);

            final JsonNode settingsNode = objectMapper.readTree(settings);
            final JsonNode mappingsNode = objectMapper.readTree(mappings);

            final CreateIndexCommand command = CreateIndexCommand.builder()
                .mappings(mappingsNode)
                .settings(settingsNode)
                .build();

            final CreateIndexRequest request = new CreateIndexRequest(name);
            request.source(objectMapper.writeValueAsString(command), XContentType.JSON);

            final CreateIndexResponse createIndexResponse = client.indices().create(request, RequestOptions.DEFAULT);

            return createIndexResponse.isAcknowledged();
        } catch (final Exception ex) {
            logger.error("Failed to create index", ex);

            throw new ElasticServiceException("Failed to create index", ex);
        }
    }

    @Override
    public boolean deleteIndex(String name) {
        try {
            final DeleteIndexRequest   request             = new DeleteIndexRequest(name);
            final AcknowledgedResponse deleteIndexResponse = client.indices().delete(request, RequestOptions.DEFAULT);

            return deleteIndexResponse.isAcknowledged();
        } catch (final ElasticsearchException ex) {
            if (ex.status() == RestStatus.NOT_FOUND) {
                throw new ElasticServiceException("Failed to delete index. Index was not found.", ex);
            }
            throw new ElasticServiceException("Failed to delete index", ex);
        } catch (final Exception ex) {
            throw new ElasticServiceException("Failed to delete index", ex);
        }
    }

    @Override
    public boolean checkPipeline(String name) throws ElasticServiceException {
        try {
            final GetPipelineRequest request = new GetPipelineRequest(name);

            final GetPipelineResponse response = client.ingest().getPipeline(request, RequestOptions.DEFAULT);

            return response.isFound();
        } catch (final Exception ex) {
            throw new ElasticServiceException(String.format("Failed to query pipeline [name=%s]", name), ex);
        }
    }

    @Override
    public boolean createPipeline(String name, String definitionResource) throws ElasticServiceException {
        try (
            final InputStream definitionIs = resourceLoader.getResource(definitionResource).getInputStream();
        ) {
            final String processor = IOUtils.toString(definitionIs, StandardCharsets.UTF_8);

            final PutPipelineRequest request  = new PutPipelineRequest(
                name, new BytesArray(processor.getBytes(StandardCharsets.UTF_8)), XContentType.JSON
            );

            final AcknowledgedResponse response = client.ingest().putPipeline(request, RequestOptions.DEFAULT);
            return response.isAcknowledged();
        } catch (final Exception ex) {
            logger.error(String.format(
                "Failed to create pipeline [name=%s, definition=%s]",
                name, definitionResource
            ), ex );

            throw new ElasticServiceException("Failed to create index", ex);
        }
    }

    @Override
    public boolean deletePipeline(String name) {
        final DeletePipelineRequest request = new DeletePipelineRequest(name);
        try {
            final AcknowledgedResponse response = client.ingest().deletePipeline(request, RequestOptions.DEFAULT); // TODO
            return response.isAcknowledged();
        } catch (final IOException ex) {
            throw new ElasticServiceException("Failed to delete pipeline", ex);
        }
    }

    private TransformStats getTransformStats(String name) throws ElasticServiceException {
        try {
            final GetTransformStatsRequest request = new GetTransformStatsRequest(name);
            request.setAllowNoMatch(true);

            final GetTransformStatsResponse response = client.transform().getTransformStats(request, RequestOptions.DEFAULT);

            final TransformStats stats = response.getTransformsStats().stream()
                .filter(s -> s.getId().equals(name))
                .findFirst()
                .orElse(null);

            return stats;
        } catch (final Exception ex) {
            throw new ElasticServiceException(String.format("Failed to query transform stats [name=%s]", name), ex);
        }
    }

    @Override
    public boolean checkTransform(String name) throws ElasticServiceException {
        try {
            final TransformStats stats = this.getTransformStats(name);

            if (stats == null) {
                return false;
            }

            return true;
        } catch (final ElasticServiceException ex) {
            throw ex;
        } catch (final Exception ex) {
            throw new ElasticServiceException(String.format("Failed to query transform [name=%s]", name), ex);
        }
    }

    @Override
    public boolean isTransformStarted(String name) throws ElasticServiceException {
        try {
            final TransformStats stats = this.getTransformStats(name);

            return (stats != null && stats.getState() == State.STARTED);
        } catch (final ElasticServiceException ex) {
            throw ex;
        } catch (final Exception ex) {
            throw new ElasticServiceException(String.format("Failed to query transform [name=%s]", name), ex);
        }
    }

    /**
     * Create a transform
     *
     * @param name The name of the new transform
     * @param sourceIndex Source index
     * @param destIndex Destination index
     * @return If the response is acknowledged
     * @throws ElasticServiceException
     */
   private boolean createAssetViewAggregationTransform(String name, String sourceIndex, String destIndex) throws ElasticServiceException {
        // Query from source to target index
        final QueryConfig queryConfig = new QueryConfig(new MatchAllQueryBuilder());

        // Source and destination indices
        final SourceConfig sourceConfig = SourceConfig.builder().setIndex(sourceIndex).setQueryConfig(queryConfig).build();
        final DestConfig   destConfig   = DestConfig.builder().setIndex(destIndex).build();

        // Group by fields
        final GroupConfig groupConfig = GroupConfig.builder()
            .groupBy("country", TermsGroupSource.builder().setField("country.keyword").build())
            .groupBy("day", TermsGroupSource.builder().setField("day").build())
            .groupBy("week", TermsGroupSource.builder().setField("week").build())
            .groupBy("month", TermsGroupSource.builder().setField("month").build())
            .groupBy("year", TermsGroupSource.builder().setField("year").build())
            .groupBy("publisherKey", TermsGroupSource.builder().setField("publisherKey.keyword").build())
            .groupBy("segment", TermsGroupSource.builder().setField("segment.keyword").build())
            .groupBy("id", TermsGroupSource.builder().setField("id.keyword").build())
            .build();

        // Aggregated fields - Counts
        final AggregatorFactories.Builder aggBuilder = new AggregatorFactories.Builder();
        aggBuilder
            .addAggregator(AggregationBuilders.filter("view_count", QueryBuilders.termQuery("source.keyword", "VIEW")))
            .addAggregator(AggregationBuilders.filter("search_count", QueryBuilders.termQuery("source.keyword", "SEARCH")))
            .addAggregator(AggregationBuilders.filter("reference_count", QueryBuilders.termQuery("source.keyword", "REFERENCE")));

        final AggregationConfig aggConfig   = new AggregationConfig(aggBuilder);
        final PivotConfig       pivotConfig = PivotConfig.builder().setGroups(groupConfig).setAggregationConfig(aggConfig).build();

        final SyncConfig syncConfig = new TimeSyncConfig("insertTimestamp", TimeValue.timeValueSeconds(60));

        final TransformConfig transformConfig = TransformConfig
            .builder()
            .setId(name)
            .setSource(sourceConfig)
            .setDest(destConfig)
            .setFrequency(TimeValue.timeValueSeconds(60))
            .setPivotConfig(pivotConfig)
            .setSyncConfig(syncConfig)
            .build();

        final PutTransformRequest request = new PutTransformRequest(transformConfig);
        request.setDeferValidation(false);

        try {
            final org.elasticsearch.client.core.AcknowledgedResponse response = client.transform().putTransform(request, RequestOptions.DEFAULT);
            return response.isAcknowledged();
        } catch (final ElasticsearchStatusException ex) {
            if (ex.status() != RestStatus.BAD_REQUEST) {
                throw new ElasticServiceException("Failed to create transform", ex);
            }
            return false;
        } catch (final IOException ex) {
            throw new ElasticServiceException("Failed to create transform", ex);
        }
    }

    @Override
    public boolean deleteTransform(String name, boolean force) {
        final DeleteTransformRequest request = new DeleteTransformRequest(name);
        request.setForce(force);
        try {
            final org.elasticsearch.client.core.AcknowledgedResponse response = client.transform().deleteTransform(request, RequestOptions.DEFAULT);
            return response.isAcknowledged();
        } catch (final ElasticsearchStatusException ex) {
            if (ex.status() != RestStatus.BAD_REQUEST) {
                throw new ElasticServiceException("Failed to delete transform", ex);
            }
            return false;
        } catch (final IOException ex) {
            throw new ElasticServiceException("Failed to delete transform", ex);
        }
    }

    @Override
    public boolean startTransform(String name) {
        final StartTransformRequest request = new StartTransformRequest(name);
        request.setTimeout(TimeValue.timeValueSeconds(2));
        try {
            final StartTransformResponse response = client.transform().startTransform(request, RequestOptions.DEFAULT);
            return response.isAcknowledged();
        } catch (final ElasticsearchStatusException ex) {
            if (ex.status() != RestStatus.CONFLICT) {
                throw new ElasticServiceException("Failed to start transform. Transform is already running", ex);
            }
            return false;
        } catch (final IOException ex) {
            throw new ElasticServiceException("Failed to start transform", ex);
        }
    }

    @Override
    public boolean stopTransform(String name, boolean allowNoMatch, boolean waitForCheckpoint, boolean waitForCompletion) {
        final StopTransformRequest request = new StopTransformRequest(name);
        request.setTimeout(TimeValue.timeValueSeconds(2));
        request.setAllowNoMatch(allowNoMatch);
        request.setWaitForCheckpoint(waitForCheckpoint);
        request.setWaitForCompletion(waitForCompletion);
        try {
            final StopTransformResponse response = client.transform().stopTransform(request, RequestOptions.DEFAULT); // TODO
            return response.isAcknowledged();
        } catch (final IOException ex) {
            throw new ElasticServiceException("Failed to stop transform", ex);
        }
    }

    @Override
    public void addProfile(ProfileRecord profile) throws ElasticServiceException {
        try {
            final IndexRequest request = new IndexRequest(this.configuration.getProfileIndex().getName());
            final String       content = objectMapper.writeValueAsString(profile);

            request.id(profile.getKey().toString());
            request.source(content, XContentType.JSON);

            client.index(request, RequestOptions.DEFAULT);
        } catch (final Exception ex) {
            throw new ElasticServiceException("Failed to add profile to index", ex);
        }
    }

    @Override
    public void addAsset(String content) throws ElasticServiceException {
        try {
            final IndexRequest request       = new IndexRequest(this.configuration.getAssetIndex().getName());
            final JsonNode     contentObject = objectMapper.readTree(content);

            request.id(contentObject.get("id").asText());
            request.source(content, XContentType.JSON);

            client.index(request, RequestOptions.DEFAULT);
        } catch (final Exception ex) {
            throw new ElasticServiceException("Failed to add asset to index", ex);
        }
    }

    @Override
    public void addAsset(CatalogueFeature feature) throws ElasticServiceException {
        try {
            final IndexRequest request = new IndexRequest(this.configuration.getAssetIndex().getName());
            final String       content = objectMapper.writeValueAsString(feature);

            request.id(feature.getId());
            request.source(content, XContentType.JSON);

            client.index(request, RequestOptions.DEFAULT);
        } catch (final Exception ex) {
            throw new ElasticServiceException("Failed to add asset to index", ex);
        }
    }
    
    public CatalogueFeature findAsset(String id) throws ElasticServiceException {
        try {
            final String     indexName = this.configuration.getAssetIndex().getName();
            final GetRequest request   = new GetRequest(indexName, id);

            final GetResponse response = client.get(request, RequestOptions.DEFAULT);
            if (response.isExists()) {
                final CatalogueFeature feature = objectMapper.convertValue(response.getSourceAsMap(), CatalogueFeature.class);

                return feature;
            }

            return null;
        } catch (final Exception ex) {
            throw new ElasticServiceException("Failed to query index ", ex);
        }
    }

    @Override
    public void removeAsset(String pid) throws ElasticServiceException {
        try {
            final String        indexName = this.configuration.getAssetIndex().getName();
            final DeleteRequest request   = new DeleteRequest(indexName);

            request.id(pid);

            final DeleteResponse response = client.delete(request, RequestOptions.DEFAULT);

            if (response.getResult() != DocWriteResponse.Result.DELETED) {
                logger.warn(
                    "Failed to remove asset from index [index={}, id={}, result={}]",
                    indexName, pid, response.getResult()
                );
            }
        } catch (final Exception ex) {
            throw new ElasticServiceException("Failed to remove asset from index", ex);
        }
    }

    @Override
    public ElasticAssetQueryResult searchAssets(ElasticAssetQuery assetQuery) throws ElasticServiceException {
        Assert.notNull(assetQuery, "Expected a non-null query");

        try {
            // Get filters
            final String       text        = assetQuery.getText();
            final List<String> type        = assetQuery.getType() == null
                ? null
                : assetQuery.getType().stream().map(EnumType::getValue).collect(Collectors.toList());
            final List<String> format      = assetQuery.getFormat();
            final List<String> crs         = assetQuery.getCrs();
            final Integer      minPrice    = assetQuery.getMinPrice();
            final Integer      maxPrice    = assetQuery.getMaxPrice();
            final String       fromDate    = assetQuery.getFromDate();
            final String       toDate      = assetQuery.getToDate();
            final List<String> topic       = assetQuery.getTopic() == null
                ? null
                : assetQuery.getTopic().stream().map(EnumTopicCategory::getValue).collect(Collectors.toList());
            final Integer      minScale    = assetQuery.getMinScale();
            final Integer      maxScale    = assetQuery.getMaxScale();
            final ShapeRelation spatialOperation 	= assetQuery.getSpatialOperation() == null
            	? ShapeRelation.INTERSECTS
            	: assetQuery.getSpatialOperation().toShapeRelation();
            final Coordinate   topLeft     			= assetQuery.topLeftToCoordinate();
            final Coordinate   bottomRight 			= assetQuery.bottomRightToCoordinate();
            final List<String> attribute   = assetQuery.getAttribute();
            final List<String> license     = assetQuery.getLicense();
            final List<String> publisher   = assetQuery.getPublisher();
            final List<String> 	languageList 							= assetQuery.getLanguage();
            final List<EnumElasticSearchDatasetSize> datasetSizeList	= assetQuery.getSizeOfDataset();
            final String       orderBy     = assetQuery.getOrderBy() == null
                ? EnumElasticSearchSortField.SCORE.getValue()
                : assetQuery.getOrderBy().getValue();
            final String       order       = assetQuery.getOrder() == null
                ? EnumSortingOrder.DESC.toString()
                : assetQuery.getOrder().toString();
            final Integer      from        = assetQuery.getFrom();
            final Integer      size        = assetQuery.getSize().orElse(10);

            final ElasticAssetQueryResult result = new ElasticAssetQueryResult();

            // Restrict the request to the asset index
            final SearchRequest       searchRequest       = new SearchRequest(this.configuration.getAssetIndex().getName());
            final SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            final BoolQueryBuilder    query               = QueryBuilders.boolQuery();

            // Check free text
            if (text != null && !text.isEmpty()) {
                query.must(QueryBuilders.boolQuery()
                    .should(QueryBuilders.matchQuery("properties.abstract", text).operator(Operator.AND).fuzziness(Fuzziness.AUTO))
                    .should(QueryBuilders.matchPhrasePrefixQuery("properties.abstract", text).slop(2))
                    .should(QueryBuilders.matchQuery("properties.title", text).operator(Operator.AND).fuzziness(Fuzziness.AUTO))
                    .should(QueryBuilders.matchPhrasePrefixQuery("properties.title", text).slop(2))
                    .should(QueryBuilders.matchQuery("properties.format", text).operator(Operator.AND).fuzziness(Fuzziness.AUTO))
                    .should(QueryBuilders.matchPhrasePrefixQuery("properties.format", text).slop(2))
                    .should(QueryBuilders.matchQuery("geometry.type", text).operator(Operator.AND).fuzziness(Fuzziness.AUTO))
                    .should(QueryBuilders.matchPhrasePrefixQuery("properties.type", text).slop(2))
                    .should(QueryBuilders.matchQuery("properties.keywords.keyword", text).operator(Operator.AND).fuzziness(Fuzziness.AUTO))
                    .should(QueryBuilders.matchPhrasePrefixQuery("properties.keywords.keyword", text).slop(2))
                    .should(QueryBuilders.matchQuery("properties.language", text).operator(Operator.AND).fuzziness(Fuzziness.AUTO))
                    .should(QueryBuilders.matchPhrasePrefixQuery("properties.language", text).slop(2))
                    .should(QueryBuilders.matchQuery("properties.license", text).operator(Operator.AND).fuzziness(Fuzziness.AUTO))
                    .should(QueryBuilders.matchPhrasePrefixQuery("properties.license", text).slop(2))
                    .should(QueryBuilders.matchQuery("properties.publisher_name", text).operator(Operator.AND).fuzziness(Fuzziness.AUTO))
                    .should(QueryBuilders.matchPhrasePrefixQuery("properties.publisher_name", text).slop(2))
                    .should(QueryBuilders.matchQuery("properties.suitable_for", text).operator(Operator.AND).fuzziness(Fuzziness.AUTO))
                    .should(QueryBuilders.matchPhrasePrefixQuery("properties.suitable_for", text).slop(2))
                    .should(QueryBuilders.matchQuery("properties.topic_category", text).operator(Operator.AND).fuzziness(Fuzziness.AUTO))
                    .should(QueryBuilders.matchPhrasePrefixQuery("properties.topic_category", text).slop(2))
                    .should(QueryBuilders.matchQuery("properties.publisher_name", text).operator(Operator.AND).fuzziness(Fuzziness.AUTO))
                    .should(QueryBuilders.matchPhrasePrefixQuery("properties.publisher_name", text).slop(2))
                    .should(QueryBuilders.matchQuery("properties.resources.fileName", text).operator(Operator.AND).fuzziness(Fuzziness.AUTO))
                    .should(QueryBuilders.matchPhrasePrefixQuery("properties.resources.fileName", text).slop(2))
                    .should(QueryBuilders.matchQuery("properties.resources.format", text).operator(Operator.AND).fuzziness(Fuzziness.AUTO))
                    .should(QueryBuilders.matchPhrasePrefixQuery("properties.format", text).slop(2))
                    .should(QueryBuilders.matchQuery("properties.resources.type", text).operator(Operator.AND).fuzziness(Fuzziness.AUTO))
                    .should(QueryBuilders.matchPhrasePrefixQuery("properties.resources.type", text).slop(2))
                    .should(QueryBuilders.matchQuery("properties.spatial_data_service_type", text).operator(Operator.AND).fuzziness(Fuzziness.AUTO))
                    .should(QueryBuilders.matchPhrasePrefixQuery("properties.spatial_data_service_type", text).slop(2))
                    .should(QueryBuilders.matchQuery("properties.pricing_models.type", text).operator(Operator.AND).fuzziness(Fuzziness.AUTO))
                    .should(QueryBuilders.matchPhrasePrefixQuery("properties.pricing_models.type", text).slop(2))
                );
            }

            // Check asset type
            List<QueryBuilder> typeQueries = null;
            if (type != null && !type.isEmpty()) {
                typeQueries = new ArrayList<QueryBuilder>();
                for (int i = 0; i < type.size(); i++) {
                    typeQueries.add(QueryBuilders.matchQuery("properties.type", type.get(i)));
                }
                final BoolQueryBuilder tempBool = QueryBuilders.boolQuery();
                for (final QueryBuilder currentQuery : typeQueries) {
                    tempBool.should(currentQuery);
                }
                query.must(tempBool);
            }

            // Check min, max price
            if (minPrice != null && maxPrice != null) {
                query.must(QueryBuilders.rangeQuery("properties.pricing_models.totalPriceExcludingTax").from(minPrice).to(maxPrice));
            } else if (minPrice != null && maxPrice == null) {
                query.must(QueryBuilders.rangeQuery("properties.pricing_models.totalPriceExcludingTax").from(minPrice));
            } else if (minPrice == null && maxPrice != null) {
                query.must(QueryBuilders.rangeQuery("properties.pricing_models.totalPriceExcludingTax").to(maxPrice));
            }

            // Check date range
            if (fromDate != null && toDate != null) {
                query.must(QueryBuilders.boolQuery()
                    .should(QueryBuilders.boolQuery()
                        .must(QueryBuilders.existsQuery("properties.date_start"))
                        .must(QueryBuilders.existsQuery("properties.date_end"))
                        .must(QueryBuilders.rangeQuery("properties.date_start").from(fromDate))
                        .must(QueryBuilders.rangeQuery("properties.date_end").to(toDate))
                    )
                    .should(QueryBuilders.boolQuery()
                        .mustNot(QueryBuilders.existsQuery("properties.date_start"))
                        .mustNot(QueryBuilders.existsQuery("properties.date_end"))
                        .must(QueryBuilders.existsQuery("properties.revision_date"))
                        .must(QueryBuilders.rangeQuery("properties.revision_date").from(fromDate).to(toDate))
                    )
                    .should(QueryBuilders.boolQuery()
                        .mustNot(QueryBuilders.existsQuery("properties.date_start"))
                        .mustNot(QueryBuilders.existsQuery("properties.date_end"))
                        .mustNot(QueryBuilders.existsQuery("properties.revision_date"))
                        .must(QueryBuilders.existsQuery("properties.creation_date"))
                        .must(QueryBuilders.rangeQuery("properties.creation_date").from(fromDate).to(toDate))
                    )
                );
            } else if (fromDate != null && toDate == null) {
                query.must(QueryBuilders.boolQuery()
                    .should(QueryBuilders.boolQuery()
                        .must(QueryBuilders.existsQuery("properties.date_start"))
                        .must(QueryBuilders.existsQuery("properties.date_end"))
                        .must(QueryBuilders.rangeQuery("properties.date_start").from(fromDate))
                    )
                    .should(QueryBuilders.boolQuery()
                        .mustNot(QueryBuilders.existsQuery("properties.date_start"))
                        .mustNot(QueryBuilders.existsQuery("properties.date_end"))
                        .must(QueryBuilders.existsQuery("properties.revision_date"))
                        .must(QueryBuilders.rangeQuery("properties.revision_date").from(fromDate))
                    )
                    .should(QueryBuilders.boolQuery()
                        .mustNot(QueryBuilders.existsQuery("properties.date_start"))
                        .mustNot(QueryBuilders.existsQuery("properties.date_end"))
                        .mustNot(QueryBuilders.existsQuery("properties.revision_date"))
                        .must(QueryBuilders.existsQuery("properties.creation_date"))
                        .must(QueryBuilders.rangeQuery("properties.creation_date").from(fromDate))
                    )
                );
            }
            else if (fromDate == null && toDate != null) {
                query.must(QueryBuilders.boolQuery()
                    .should(QueryBuilders.boolQuery()
                        .must(QueryBuilders.existsQuery("properties.date_start"))
                        .must(QueryBuilders.existsQuery("properties.date_end"))
                        .must(QueryBuilders.rangeQuery("properties.date_end").to(toDate))
                    )
                    .should(QueryBuilders.boolQuery()
                        .mustNot(QueryBuilders.existsQuery("properties.date_start"))
                        .mustNot(QueryBuilders.existsQuery("properties.date_end"))
                        .must(QueryBuilders.existsQuery("properties.revision_date"))
                        .must(QueryBuilders.rangeQuery("properties.revision_date").to(toDate))
                    )
                    .should(QueryBuilders.boolQuery()
                        .mustNot(QueryBuilders.existsQuery("properties.date_start"))
                        .mustNot(QueryBuilders.existsQuery("properties.date_end"))
                        .mustNot(QueryBuilders.existsQuery("properties.revision_date"))
                        .must(QueryBuilders.existsQuery("properties.creation_date"))
                        .must(QueryBuilders.rangeQuery("properties.creation_date").to(toDate))
                    )
                );
            }

            // Check topic
            List<QueryBuilder> topicQueries = null;
            if (topic != null && !topic.isEmpty()) {
                topicQueries = new ArrayList<QueryBuilder>();
                for (int i = 0; i < topic.size(); i++) {
                    topicQueries.add(QueryBuilders.matchQuery("properties.topic_category", topic.get(i)));
                }
                final BoolQueryBuilder tempBool = QueryBuilders.boolQuery();
                for (final QueryBuilder currentQuery : topicQueries) {
                    tempBool.should(currentQuery);
                }
                query.must(tempBool);
            }

            // Check coverage

            /*
             * INTERSECTS – (default) returns all documents whose geo_shape field intersects the query geometry
             * DISJOINT – retrieves all documents whose geo_shape field has nothing in common with the query geometry
             * WITHIN – gets all documents whose geo_shape field is within the query geometry
             * CONTAINS – returns all documents whose geo_shape field contains the query geometry
             */

            if (topLeft != null && bottomRight != null) {
                final Rectangle geometry = new EnvelopeBuilder(topLeft, bottomRight).buildGeometry();

                query.must(QueryBuilders.geoShapeQuery("properties.automated_metadata.mbr", geometry).relation(spatialOperation));
            }

            // Check asset format
            List<QueryBuilder> formatQueries = null;
            if (format != null && !format.isEmpty()) {
                formatQueries = new ArrayList<QueryBuilder>();
                for (int i = 0; i < format.size(); i++) {
                    formatQueries.add(QueryBuilders.matchQuery("properties.format", format.get(i)));
                }
                final BoolQueryBuilder tempBool = QueryBuilders.boolQuery();
                for (final QueryBuilder currentQuery : formatQueries) {
                    tempBool.should(currentQuery);
                }
                query.must(tempBool);
            }

            // Check asset CRS
            List<QueryBuilder> CRSQueries = null;
            if (crs != null && !crs.isEmpty()) {
                CRSQueries = new ArrayList<QueryBuilder>();
                for (int i = 0; i < crs.size(); i++) {
                    CRSQueries.add(QueryBuilders.matchQuery("properties.automated_metadata.crs", crs.get(i)));
                }
                final BoolQueryBuilder tempBool = QueryBuilders.boolQuery();
                for (final QueryBuilder currentQuery : CRSQueries) {
                    tempBool.should(currentQuery);
                }
                query.must(tempBool);
            }

            // Check asset scale
            if (minScale != null && maxScale != null) {
                query.must(QueryBuilders.boolQuery().must(QueryBuilders.rangeQuery("properties.scales.scale").from(minScale).to(maxScale)));
            } else if (minScale != null && maxScale == null) {
                query.must(QueryBuilders.boolQuery().must(QueryBuilders.rangeQuery("properties.scales.scale").from(minScale)));
            } else if (minScale == null && maxScale != null) {
                query.must(QueryBuilders.boolQuery().must(QueryBuilders.rangeQuery("properties.scales.scale").to(maxScale)));
            }

            // Check attributes
            List<QueryBuilder> attributeQueries = null;
            if (attribute != null && !attribute.isEmpty()) {
                attributeQueries = new ArrayList<QueryBuilder>();
                for (int i = 0; i < attribute.size(); i++) {
                    attributeQueries.add(QueryBuilders.matchQuery("properties.automated_metadata.attributes", attribute.get(i))
                            .fuzziness(Fuzziness.AUTO));
                }
                final BoolQueryBuilder tempBool = QueryBuilders.boolQuery();
                for (final QueryBuilder currentQuery : attributeQueries) {
                    tempBool.should(currentQuery);
                }
                query.must(tempBool);
            }

            // Check license
            List<QueryBuilder> licenseQueries = null;
            if (license != null && !license.isEmpty()) {
                licenseQueries = new ArrayList<QueryBuilder>();
                for (int i = 0; i < license.size(); i++) {
                    licenseQueries.add(QueryBuilders.matchQuery("properties.license", license.get(i)));
                }
                final BoolQueryBuilder tempBool = QueryBuilders.boolQuery();
                for (final QueryBuilder currentQuery : licenseQueries) {
                    tempBool.should(currentQuery);
                }
                query.must(tempBool);
            }

            // Check publisher
            List<QueryBuilder> publisherQueries = null;
            if (publisher != null && !publisher.isEmpty()) {
                publisherQueries = new ArrayList<QueryBuilder>();
                for (int i = 0; i < publisher.size(); i++) {
                    publisherQueries.add(QueryBuilders.matchQuery("properties.publisher_name", publisher.get(i)).fuzziness(Fuzziness.AUTO));
                }
                final BoolQueryBuilder tempBool = QueryBuilders.boolQuery();
                for (final QueryBuilder currentQuery : publisherQueries) {
                    tempBool.should(currentQuery);
                }
                query.must(tempBool);
            }

    		// Check language
            List<QueryBuilder> languageQueries = null;
            if (languageList != null && !languageList.isEmpty()) {
            	languageQueries = new ArrayList<QueryBuilder>();
                for (int i = 0; i < languageList.size(); i++) {
                	languageQueries.add(QueryBuilders.matchQuery("properties.language", languageList.get(i)));
                }
                final BoolQueryBuilder tempBool = QueryBuilders.boolQuery();
                for (final QueryBuilder currentQuery : languageQueries) {
                    tempBool.should(currentQuery);
                }
                query.must(tempBool);
            }

    		// Check dataset size
            List<QueryBuilder> datasetSizeQueries = null;
            if (datasetSizeList != null && !datasetSizeList.isEmpty()) {
            	datasetSizeQueries = new ArrayList<QueryBuilder>();
                for (int i = 0; i < datasetSizeList.size(); i++) {
                	if (datasetSizeList.get(i) == EnumElasticSearchDatasetSize.SMALL) {
                		datasetSizeQueries.add(QueryBuilders.boolQuery().must(QueryBuilders.rangeQuery("properties.automated_metadata.featureCount").lt(datasetSizeList.get(i).getMax())));
                	} else if (datasetSizeList.get(i) == EnumElasticSearchDatasetSize.MEDIUM) {
                		datasetSizeQueries.add(QueryBuilders.boolQuery().must(QueryBuilders.rangeQuery("properties.automated_metadata.featureCount").lte(datasetSizeList.get(i).getMax()).gte(datasetSizeList.get(i).getMin())));
                	} else if (datasetSizeList.get(i) == EnumElasticSearchDatasetSize.LARGE) {
                		datasetSizeQueries.add(QueryBuilders.boolQuery().must(QueryBuilders.rangeQuery("properties.automated_metadata.featureCount").gt(datasetSizeList.get(i).getMin())));
                	}
                }
                final BoolQueryBuilder tempBool = QueryBuilders.boolQuery();
                for (final QueryBuilder currentQuery : datasetSizeQueries) {
                    tempBool.should(currentQuery);
                }
                query.must(tempBool);
            }

            // If order by is specified
            if (!StringUtils.isBlank(orderBy)) {
                /*
                 * If order is not specifier or is specified as ASC, default
                 * value should be ASC
                 */
                if (order == null || order.isEmpty() || order.equals("ASC")) {
                    searchSourceBuilder.query(query).sort(new FieldSortBuilder(orderBy).order(SortOrder.ASC))
                            .sort(new ScoreSortBuilder().order(SortOrder.DESC));
                    // Else DESC
                } else {
                    searchSourceBuilder.query(query).sort(new FieldSortBuilder(orderBy).order(SortOrder.DESC))
                            .sort(new ScoreSortBuilder().order(SortOrder.DESC));
                }
            }
            // Else use the default order by (ORDER BY _score DESC)
            else {
                searchSourceBuilder.query(query);
            }

            // If page and size are specified
            if (from != null && size != null) {
            	searchSourceBuilder.from(from).size(size);
            }

            searchRequest.source(searchSourceBuilder);

            final SearchResponse            searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
            final SearchHits                hits           = searchResponse.getHits();
            final List<Map<String, Object>> objects        = new ArrayList<>();

            // NOTE: Ignore relation ...
            result.setTotal(hits.getTotalHits().value);

            for (final SearchHit hit : hits) {
                final Map<String, Object> sourceAsMap = hit.getSourceAsMap();
                objects.add(sourceAsMap);
            }

            // Convert list of objects to list of features
            final List<CatalogueFeature> features = objectMapper.convertValue(objects, new TypeReference<List<CatalogueFeature>>() { });
            result.setAssets(features);

            return result;
        } catch(final Exception ex) {
            throw new ElasticServiceException("Search operation has failed", ex);
        }
    }

    @Override
    public DataSeries<BigDecimal> searchAssetViews(AssetViewQuery query) {
        Assert.notNull(query, "Expected a non-null query");
        Assert.notNull(query.getSource(), "Expected a non-null source");

        try {
            final DataSeries<BigDecimal>                result                 = new DataSeries<>();
            final List<String>                          groupByFields          = new ArrayList<>();
            final BaseQuery.TemporalDimension           time                   = query.getTime();
            final BaseQuery.SpatialDimension            spatial                = query.getAreas();
            final BaseQuery.SegmentDimension            segments               = query.getSegments();
            final List<UUID>                            publishers             = query.getPublishers();
            final List<String>                          assets                 = query.getAssets();
            final EnumAssetViewSource                   source                 = query.getSource();
            TermsValuesSourceBuilder                    aggregationByYear      = null;
            TermsValuesSourceBuilder                    aggregationByMonth     = null;
            TermsValuesSourceBuilder                    aggregationByWeek      = null;
            TermsValuesSourceBuilder                    aggregationByDay       = null;
            TermsValuesSourceBuilder                    aggregationByCountry   = null;
            TermsValuesSourceBuilder                    aggregationBySegment   = null;
            TermsValuesSourceBuilder                    aggregationByPublisher = null;
            TermsValuesSourceBuilder                    aggregationByAssetID   = null;
            final BoolQueryBuilder                      filterQuery            = QueryBuilders.boolQuery();
            final List<CompositeValuesSourceBuilder<?>> sources                = new ArrayList<CompositeValuesSourceBuilder<?>>();
            final SearchSourceBuilder                   sourceBuilder          = new SearchSourceBuilder();

            if (time != null) {
                // Apply temporal dimension grouping
                result.setTimeUnit(time.getUnit());

                if (time.getUnit().ordinal() >= BaseQuery.EnumTemporalUnit.YEAR.ordinal()) {
                    groupByFields.add("year");
                    aggregationByYear = new TermsValuesSourceBuilder("year").field("year");
                    sources.add(aggregationByYear);
                }
                if (time.getUnit().ordinal() >= BaseQuery.EnumTemporalUnit.MONTH.ordinal()) {
                    groupByFields.add("month");
                    aggregationByMonth = new TermsValuesSourceBuilder("month").field("month");
                    sources.add(aggregationByMonth);
                }
                if (time.getUnit().ordinal() >= BaseQuery.EnumTemporalUnit.WEEK.ordinal()) {
                    groupByFields.add("week");
                    aggregationByWeek = new TermsValuesSourceBuilder("week").field("week");
                    sources.add(aggregationByWeek);
                }
                if (time.getUnit().ordinal() >= BaseQuery.EnumTemporalUnit.DAY.ordinal()) {
                    groupByFields.add("day");
                    aggregationByDay = new TermsValuesSourceBuilder("day").field("day");
                    sources.add(aggregationByDay);
                }

                int minYear  = 0, maxYear = 0;
                int minMonth = 0, maxMonth = 0;
                int minWeek  = 0, maxWeek = 0;
                int minDay   = 0, maxDay = 0;

                // Apply temporal dimension filtering
                if (time.getMin() != null && time.getMax() != null) {
                    minYear  = time.getMin().getYear();
                    minMonth = time.getMin().getMonthValue();
                    minWeek  = time.getMin().get(WeekFields.of(Locale.getDefault()).weekOfYear());
                    minDay   = time.getMin().getDayOfMonth();

                    maxYear  = time.getMax().getYear();
                    maxMonth = time.getMax().getMonthValue();
                    maxWeek  = time.getMax().get(WeekFields.of(Locale.getDefault()).weekOfYear());
                    maxDay   = time.getMax().getDayOfMonth();

                    if (time.getUnit().ordinal() >= BaseQuery.EnumTemporalUnit.YEAR.ordinal()) {
                        filterQuery.must(QueryBuilders.rangeQuery("year").gte(minYear).lte(maxYear));
                    }
                    if (time.getUnit().ordinal() >= BaseQuery.EnumTemporalUnit.MONTH.ordinal()) {
                        filterQuery.must(QueryBuilders.rangeQuery("month").gte(minMonth).lte(maxMonth));
                    }
                    if (time.getUnit().ordinal() >= BaseQuery.EnumTemporalUnit.WEEK.ordinal()) {
                        filterQuery.must(QueryBuilders.rangeQuery("week").gte(minWeek).lte(maxWeek));
                    }
                    if (time.getUnit().ordinal() >= BaseQuery.EnumTemporalUnit.DAY.ordinal()) {
                        filterQuery.must(QueryBuilders.rangeQuery("day").gte(minDay).lte(maxDay));
                    }
                }
            }

            if (spatial != null) {
                // Apply spatial grouping
                if (spatial.isEnabled()) {
                    groupByFields.add("country.keyword");
                    aggregationByCountry = new TermsValuesSourceBuilder("country").field("country.keyword");
                    sources.add(aggregationByCountry);
                }

                // Apply spatial filtering
                if (spatial.getCodes() != null && !spatial.getCodes().isEmpty()) {
                    final List<QueryBuilder> spatialQueries = new ArrayList<QueryBuilder>();
                    for (int i = 0; i < spatial.getCodes().size(); i++) {
                        spatialQueries.add(QueryBuilders.termQuery("country.keyword", spatial.getCodes().get(i)));
                    }
                    final BoolQueryBuilder tempBool = QueryBuilders.boolQuery();
                    for (final QueryBuilder currentQuery : spatialQueries) {
                        tempBool.should(currentQuery);
                    }
                    filterQuery.must(tempBool);
                }
            }

            if (segments != null) {
                // Apply segment grouping
                if (segments.isEnabled()) {
                    groupByFields.add("segment.keyword");
                    aggregationBySegment = new TermsValuesSourceBuilder("segment").field("segment.keyword");
                    sources.add(aggregationBySegment);
                }

                // Apply segment filtering
                if (segments.getSegments() != null && !segments.getSegments().isEmpty()) {
                    final List<QueryBuilder> segmentQueries = new ArrayList<QueryBuilder>();
                    for (int i = 0; i < segments.getSegments().size(); i++) {
                        segmentQueries.add(QueryBuilders.termQuery("segment.keyword", segments.getSegments().get(i)));
                    }
                    final BoolQueryBuilder tempBool = QueryBuilders.boolQuery();
                    for (final QueryBuilder currentQuery : segmentQueries) {
                        tempBool.should(currentQuery);
                    }
                    filterQuery.must(tempBool);
                }
            }

            if (publishers != null) {
                // Apply publisher grouping
                if (publishers.size() > 1) {
                    groupByFields.add("publisherKey.keyword");
                    aggregationByPublisher = new TermsValuesSourceBuilder("publisherKey").field("publisherKey.keyword");
                    sources.add(aggregationByPublisher);
                }

                // Apply publisher filtering
                final List<QueryBuilder> publisherQueries = new ArrayList<QueryBuilder>();
                for (int i = 0; i < publishers.size(); i++) {
                    publisherQueries.add(QueryBuilders.termQuery("publisherKey.keyword", publishers.get(i).toString()));
                }
                final BoolQueryBuilder tempBool = QueryBuilders.boolQuery();
                for (final QueryBuilder currentQuery : publisherQueries) {
                    tempBool.should(currentQuery);
                }
                filterQuery.must(tempBool);
            }

            if (assets != null) {
                // Apply asset grouping
                if (assets.size() > 1) {
                    groupByFields.add("id.keyword");
                    aggregationByAssetID = new TermsValuesSourceBuilder("id").field("id.keyword");
                    sources.add(aggregationByAssetID);
                }

                // Apply asset filtering
                final List<QueryBuilder> assetQueries = new ArrayList<QueryBuilder>();
                for (int i = 0; i < assets.size(); i++) {
                    assetQueries.add(QueryBuilders.termQuery("id.keyword", assets.get(i)));
                }
                final BoolQueryBuilder tempBool = QueryBuilders.boolQuery();
                for (final QueryBuilder currentQuery : assetQueries) {
                    tempBool.should(currentQuery);
                }
                filterQuery.must(tempBool);
            }

            SumAggregationBuilder       sumOfView                   = null;
            SumAggregationBuilder       sumOfSearch                 = null;
            SumAggregationBuilder       sumOfRef                    = null;
            CompositeAggregationBuilder compositeAggregationBuilder = null;
            if (source == EnumAssetViewSource.VIEW) {
                sumOfView                   = AggregationBuilders
                    .sum("view_count")
                    .field("view_count");
                compositeAggregationBuilder = new CompositeAggregationBuilder("groupby", sources)
                    .subAggregation(sumOfView);

            } else if (source == EnumAssetViewSource.SEARCH) {
                sumOfSearch                 = AggregationBuilders
                    .sum("search_count")
                    .field("search_count");
                compositeAggregationBuilder = new CompositeAggregationBuilder("groupby", sources)
                    .subAggregation(sumOfSearch);

            } else if (source == EnumAssetViewSource.REFERENCE) {
                sumOfRef                    = AggregationBuilders
                    .sum("reference_count")
                    .field("reference_count");
                compositeAggregationBuilder = new CompositeAggregationBuilder("groupby", sources)
                    .subAggregation(sumOfRef);

            } else {
                sumOfView                   = AggregationBuilders
                    .sum("view_count")
                    .field("view_count");
                sumOfSearch                 = AggregationBuilders
                    .sum("search_count")
                    .field("search_count");
                sumOfRef                    = AggregationBuilders
                    .sum("reference_count")
                    .field("reference_count");
                compositeAggregationBuilder = new CompositeAggregationBuilder("groupby", sources)
                    .subAggregation(sumOfView)
                    .subAggregation(sumOfSearch).subAggregation(sumOfRef);
            }
            compositeAggregationBuilder.size(maxBucketCount);
            sourceBuilder.aggregation(compositeAggregationBuilder);

            // Define index
            final SearchRequest       searchRequest       = new SearchRequest(this.configuration.getAssetViewAggregateIndex().getName());
            final SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            // Create query - aggregations and filters
            searchSourceBuilder.query(filterQuery).aggregation(compositeAggregationBuilder).size(0);

            // Sort results order by grouping fields
            for (int i = 0; i < groupByFields.size(); i++) {
                searchSourceBuilder.sort(new FieldSortBuilder(groupByFields.get(i)).order(SortOrder.ASC));
            }
            searchRequest.source(searchSourceBuilder);

            final SearchResponse       searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
            final CompositeAggregation agg            = searchResponse.getAggregations().get("groupby");

            // For each entry
            for (final CompositeAggregation.Bucket bucket : agg.getBuckets()) {
                result.getPoints().add(this.mapObjectToDataPoint(query, bucket));
            }

            return result;
        } catch (final Exception ex) {
            throw new ElasticServiceException("Search operation has failed", ex);
        }
    }

    private DataPoint<BigDecimal> mapObjectToDataPoint(AssetViewQuery query, CompositeAggregation.Bucket bucket) {
        final BaseQuery.TemporalDimension time       = query.getTime();
        final BaseQuery.SpatialDimension  spatial    = query.getAreas();
        final BaseQuery.SegmentDimension  segments   = query.getSegments();
        final List<UUID>                  publishers = query.getPublishers();
        final List<String>                assets     = query.getAssets();
        final EnumAssetViewSource         source     = query.getSource();

        final DataPoint<BigDecimal> p = new DataPoint<>();

        // The order we extract values must match the order we apply grouping
        // fields
        if (time != null) {
            p.setTime(new DataPoint.TimeInstant());

            if (time.getUnit().ordinal() >= BaseQuery.EnumTemporalUnit.YEAR.ordinal()) {
                p.getTime().setYear((Integer) bucket.getKey().get("year"));
            }
            if (time.getUnit().ordinal() >= BaseQuery.EnumTemporalUnit.MONTH.ordinal()) {
                p.getTime().setMonth((Integer) bucket.getKey().get("month"));
            }
            if (time.getUnit().ordinal() >= BaseQuery.EnumTemporalUnit.WEEK.ordinal()) {
                p.getTime().setWeek((Integer) bucket.getKey().get("week"));
            }
            if (time.getUnit().ordinal() >= BaseQuery.EnumTemporalUnit.DAY.ordinal()) {
                p.getTime().setDay((Integer) bucket.getKey().get("day"));
            }
        }

        if (spatial != null && spatial.isEnabled()) {
            p.setLocation(DataPoint.Location.of((String) bucket.getKey().get("country")));
        }

        if (segments != null && segments.isEnabled()) {
            p.setSegment((String) bucket.getKey().get("segment"));
        }

        if (publishers != null && publishers.size() > 1) {
            p.setPublisher(UUID.fromString((String) bucket.getKey().get("publisherKey")));
        }

        if (assets != null && assets.size() > 1) {
            p.setAsset((String) bucket.getKey().get("id"));
        }

        switch (source) {
            case VIEW :
                p.setValue(BigDecimal.valueOf(((ParsedSum) bucket.getAggregations().asMap().get("view_count")).getValue()));
                break;
            case SEARCH :
                p.setValue(BigDecimal.valueOf(((ParsedSum) bucket.getAggregations().asMap().get("search_count")).getValue()));
                break;
            case REFERENCE :
                p.setValue(BigDecimal.valueOf(((ParsedSum) bucket.getAggregations().asMap().get("reference_count")).getValue()));
                break;
        }

        return p;
    }

}
