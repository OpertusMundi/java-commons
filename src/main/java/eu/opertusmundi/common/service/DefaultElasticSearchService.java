package eu.opertusmundi.common.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.geo.ShapeRelation;
import org.elasticsearch.common.geo.builders.EnvelopeBuilder;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.geometry.Rectangle;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.ScoreSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.locationtech.jts.geom.Coordinate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ResourceLoader;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.opertusmundi.common.config.ElasticConfiguration;
import eu.opertusmundi.common.model.catalogue.client.EnumTopicCategory;
import eu.opertusmundi.common.model.catalogue.client.EnumType;
import eu.opertusmundi.common.model.catalogue.elastic.CreateIndexCommand;
import eu.opertusmundi.common.model.catalogue.elastic.ElasticAssetQuery;
import eu.opertusmundi.common.model.catalogue.elastic.ElasticAssetQueryResult;
import eu.opertusmundi.common.model.catalogue.elastic.ElasticServiceException;
import eu.opertusmundi.common.model.catalogue.elastic.EnumElasticSearchSortField;
import eu.opertusmundi.common.model.catalogue.server.CatalogueFeature;
import eu.opertusmundi.common.model.dto.EnumSortingOrder;

@ConditionalOnProperty(name = "opertusmundi.elastic.enabled")
@Service
public class DefaultElasticSearchService implements ElasticSearchService {

    private static final Logger logger     = LogManager.getLogger(DefaultElasticSearchService.class);

    private RestHighLevelClient client     = null;

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    private ElasticConfiguration configuration;

    @Autowired
    private ObjectMapper objectMapper;

	@PostConstruct
	private void init() {
        this.log("Starting client initialization");

		try {
		    final HttpHost[] hosts = Arrays.asList(configuration.getHosts()).stream()
	            .map(c -> new HttpHost(c.getHostName(), c.getPort(), c.getScheme()))
	            .toArray(HttpHost[]::new);

		    final RestClientBuilder builder = RestClient.builder(hosts);

            client = new RestHighLevelClient(builder);

            this.log("Client initialization completed");
        } catch (final Exception ex) {
            logger.error("Failed to initialize client", ex);
        }
    }

    @PreDestroy
    private void shutdown() {
        this.close();
    }

    @Override
    @Retryable(include = {ElasticServiceException.class}, maxAttempts = 4, backoff = @Backoff(delay = 2000, maxDelay = 20000))
    public void initializeIndices() {
        configuration.getIndices().stream().forEach(def -> {
            if (!this.checkIndex(def.getName())) {
                this.createIndex(def);
            }
        });
    }

    @Override
    public void close() {
        try {
            client.close();
            client =null;
        } catch (final IOException ex) {
            logger.error("Failed to close elastic search client", ex);
        }
    }

    @Override
    public boolean checkIndex(String name) {
        boolean         exists  = false;
        final GetIndexRequest request = new GetIndexRequest(name);

        try {
            exists = client.indices().exists(request, RequestOptions.DEFAULT);
        } catch (final IOException ex) {
            logger.error("Failed to query index", ex);

            throw new ElasticServiceException("Failed to query index", ex);
        }
        return exists;
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
            final IndexRequest request       = new IndexRequest(this.configuration.getAssetIndex().getName());
            final String       content       = objectMapper.writeValueAsString(feature);

            request.id(feature.getId());
            request.source(content, XContentType.JSON);

            client.index(request, RequestOptions.DEFAULT);
        } catch (final Exception ex) {
            throw new ElasticServiceException("Failed to add asset to index", ex);
        }
    }

    @Override
    public ElasticAssetQueryResult searchAssets(ElasticAssetQuery assetQuery) throws ElasticServiceException {
        try {

            /* Get filters */
            final String       text        = assetQuery.getText();
            final List<String> type        = assetQuery.getType() == null
                ? null
                : assetQuery.getType().stream().map(EnumType::getValue).collect(Collectors.toList());
            final List<String> format      = assetQuery.getFormat();
            final List<String> crs         = assetQuery.getCrs();
            Integer            minPrice    = assetQuery.getMinPrice();
            Integer            maxPrice    = assetQuery.getMaxPrice();
            String             fromDate    = assetQuery.getFromDate();
            String             toDate      = assetQuery.getToDate();
            final List<String> topic       = assetQuery.getTopic() == null
                ? null
                : assetQuery.getTopic().stream().map(EnumTopicCategory::getValue).collect(Collectors.toList());
            final Integer      minScale    = assetQuery.getMinScale();
            final Integer      maxScale    = assetQuery.getMaxScale();
            final Coordinate   topLeft     = assetQuery.topLeftToCoordinate();
            final Coordinate   bottomRight = assetQuery.bottomRightToCoordinate();
            final List<String> attribute   = assetQuery.getAttribute();
            final List<String> license     = assetQuery.getLicense();
            final List<String> publisher   = assetQuery.getPublisher();
            final String       orderBy     = assetQuery.getOrderBy() == null
                ? EnumElasticSearchSortField.SCORE.getValue()
                : assetQuery.getOrderBy().getValue();
            final String       order       = assetQuery.getOrder() == null
                ? EnumSortingOrder.DESC.toString()
                : assetQuery.getOrder().toString();
            final Integer      from        = assetQuery.getFrom();
            final Integer      size        = assetQuery.getSize().orElse(10);

            final ElasticAssetQueryResult result = new ElasticAssetQueryResult();

    		/* Restrict the request to the asset index*/
    		final SearchRequest searchRequest = new SearchRequest(this.configuration.getAssetIndex().getName());
    		final SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
    		final BoolQueryBuilder query = QueryBuilders.boolQuery();

    		/* Check free text*/
            if (!StringUtils.isBlank(text)) {
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

    		/* Check asset type*/
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

            /* Check min, max price */
            if (minPrice == null) {
                minPrice = 0;
            }
            if (maxPrice == null) {
                maxPrice = 999999;
            }
            query.must(QueryBuilders.rangeQuery("properties.pricing_models.totalPriceExcludingTax").from(minPrice).to(maxPrice));

    		/* Check date range*/
            if (fromDate == null) {
                fromDate = "1900-01-01";
            }
            if (toDate == null) {
                toDate = "9999-12-31";
            }
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

    		/* Check topic*/
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

            /* Check coverage */

            /*
             * INTERSECTS – (default) returns all documents whose geo_shape field intersects the query geometry
             * DISJOINT – retrieves all documents whose geo_shape field has nothing in common with the query geometry
             * WITHIN – gets all documents whose geo_shape field is within the query geometry
             * CONTAINS – returns all documents whose geo_shape field contains the query geometry
             */

            if (topLeft != null && bottomRight != null) {
                final Rectangle geometry = new EnvelopeBuilder(topLeft, bottomRight).buildGeometry();

                query.must(QueryBuilders.geoShapeQuery("geometry", geometry).relation(ShapeRelation.INTERSECTS));
            }

            /* Check asset format */
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

            /* Check asset CRS */
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

            /* Check asset scale */
            if (minScale != null && maxScale != null) {
                query.must(QueryBuilders.boolQuery().must(QueryBuilders.rangeQuery("properties.scales.scale").from(minScale).to(maxScale)));
            } else if (minScale != null && maxScale == null) {
                query.must(QueryBuilders.boolQuery().must(QueryBuilders.rangeQuery("properties.scales.scale").from(minScale)));
            } else if (minScale == null && maxScale != null) {
                query.must(QueryBuilders.boolQuery().must(QueryBuilders.rangeQuery("properties.scales.scale").to(maxScale)));
            }

    		/* Check attributes*/
            List<QueryBuilder> attributeQueries = null;
            if (attribute != null && !attribute.isEmpty()) {
                attributeQueries = new ArrayList<QueryBuilder>();
                for (int i = 0; i < attribute.size(); i++) {
                    attributeQueries.add(
                        QueryBuilders.matchQuery("properties.automated_metadata.attributes", attribute.get(i)).fuzziness(Fuzziness.AUTO)
                    );
                }
                final BoolQueryBuilder tempBool = QueryBuilders.boolQuery();
                for (final QueryBuilder currentQuery : attributeQueries) {
                    tempBool.should(currentQuery);
                }
                query.must(tempBool);
            }

            /* Check license */
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

            /* Check publisher */
            List<QueryBuilder> publisherQueries = null;
            if (publisher != null && !publisher.isEmpty()) {
                publisherQueries = new ArrayList<QueryBuilder>();
                for (int i = 0; i < publisher.size(); i++) {
                    publisherQueries
                            .add(QueryBuilders.matchQuery("properties.publisher_name", publisher.get(i)).fuzziness(Fuzziness.AUTO));
                }
                final BoolQueryBuilder tempBool = QueryBuilders.boolQuery();
                for (final QueryBuilder currentQuery : publisherQueries) {
                    tempBool.should(currentQuery);
                }
                query.must(tempBool);
            }

            /* If order by is specified*/
            if (!StringUtils.isBlank(orderBy)) {
            	/* If order is not specifier or is specified as ASC, default value should be ASC*/
            	if (order == null || order.isEmpty() || order.equals("ASC")) {
                    searchSourceBuilder.query(query).sort(new FieldSortBuilder(orderBy).order(SortOrder.ASC)).sort(new ScoreSortBuilder().order(SortOrder.DESC));
            	/* Else DESC*/
                } else {
                    searchSourceBuilder.query(query).sort(new FieldSortBuilder(orderBy).order(SortOrder.DESC)).sort(new ScoreSortBuilder().order(SortOrder.DESC));
                }
            }
            /* Else use the default order by (ORDER BY _score DESC)*/
            else  {
            	searchSourceBuilder.query(query);
            }

            /* If page and size are specified */
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

    private void log(String message) {
        logger.info("ElasticService: {}", message);
    }

}