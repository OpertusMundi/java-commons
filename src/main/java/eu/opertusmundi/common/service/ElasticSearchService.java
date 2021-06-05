package eu.opertusmundi.common.service;

import java.math.BigDecimal;

import eu.opertusmundi.common.model.analytics.AssetViewQuery;
import eu.opertusmundi.common.model.analytics.DataSeries;
import eu.opertusmundi.common.model.analytics.ProfileRecord;
import eu.opertusmundi.common.model.catalogue.elastic.ElasticAssetQuery;
import eu.opertusmundi.common.model.catalogue.elastic.ElasticAssetQueryResult;
import eu.opertusmundi.common.model.catalogue.elastic.ElasticServiceException;
import eu.opertusmundi.common.model.catalogue.elastic.IndexDefinition;
import eu.opertusmundi.common.model.catalogue.elastic.PipelineDefinition;
import eu.opertusmundi.common.model.catalogue.server.CatalogueFeature;

public interface ElasticSearchService {

    /**
     * Closes elastic search client
     */
    void close();

    /**
     * Check if index exists
     *
     * @param name The name of the index to check
     * @return True if the index exists; Otherwise False
     */
    boolean checkIndex(String name);

    /**
     * Create an index
     *
     * @param definition Index definition object
     * @return If the response is acknowledged
     * @throws ElasticServiceException
     */
    default boolean createIndex(IndexDefinition definition) throws ElasticServiceException {
        return this.createIndex(definition.getName(), definition.getSettings(), definition.getMappings());
    }

    /**
     * Create an index
     *
     * @param name The name of the new index
     * @param settingsResource Resource with settings
     * @param mappingsResource Resource with mappings
     * @return If the response is acknowledged
     * @throws ElasticServiceException
     */
    boolean createIndex(String name, String settingsResource, String mappingsResource) throws ElasticServiceException;

    /**
     * Delete index with the specified {@code} name}
     * @param name The name of the index to delete
     * @return If the response is acknowledged
     * @throws ElasticServiceException
     */
    boolean deleteIndex(String name) throws ElasticServiceException;

    /**
     * Add or update profile
     *
     * @param profile
     * @throws ElasticServiceException
     */
    void addProfile(ProfileRecord profile) throws ElasticServiceException;

    /**
     * Add or update feature
     *
     * @param feature
     * @throws ElasticServiceException
     */
    void addAsset(CatalogueFeature feature) throws ElasticServiceException;

    /**
     * Add or update an asset
     *
     * @see https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-index_.html
     *
     * @param content The document content in JSON format
     * @throws ElasticServiceException
     */
    void addAsset(String content) throws ElasticServiceException;

    /**
     * Create a pipeline
     *
     * @param def The pipeline definition
     * @return If the response is acknowledged
     * @throws ElasticServiceException
     */
    default boolean createPipeline(PipelineDefinition def) throws ElasticServiceException {
        return this.createPipeline(def.getName(), def.getDefinition());
    }

    default boolean checkPipeline(PipelineDefinition def) throws ElasticServiceException {
        return this.checkPipeline(def.getName());
    }

    /**
     * Check if pipeline exists
     *
     * @param name The name of the pipeline
     * @return
     * @throws ElasticServiceException
     */
    boolean checkPipeline(String name) throws ElasticServiceException;

    /**
     * Create a pipeline
     *
     * @param name The name of the pipeline
     * @param definitionResource The resource with the pipeline definition
     * @return If the response is acknowledged
     * @throws ElasticServiceException
     */
    boolean createPipeline(String name, String definitionResource) throws ElasticServiceException;

    /**
     * Delete pipeline with the specified {@code} name
     * @param name The name of the pipeline to delete
     * @return If the response is acknowledged
     * @throws ElasticServiceException
     */
    boolean deletePipeline(String name) throws ElasticServiceException;

    /**
     * Check if transform exists
     *
     * @param name The name of the transform
     * @return
     * @throws ElasticServiceException
     */
    boolean checkTransform(String name) throws ElasticServiceException;

    /**
     * Check if transform is started
     *
     * @param name The name of the transform
     * @return
     * @throws ElasticServiceException
     */
    boolean isTransformStarted(String name) throws ElasticServiceException;

    /**
     * Delete transform with the specified {@code} name
     * @param name The name of the transform to delete
     * @param force When true deletes the transform regardless the state, if false deletes it if it is stopped
     * @return If the response is acknowledged
     * @throws ElasticServiceException
     */
    boolean deleteTransform(String name, boolean force) throws ElasticServiceException;

    /**
     * Start transform with the specified {@code} name
     * @param name The name of the transform to start
     * @return If the response is acknowledged
     * @throws ElasticServiceException
     */
    boolean startTransform(String name) throws ElasticServiceException;

    /**
     * Stop transform with the specified {@code} name
     * @param name The name of the transform to stop
     * @param allowNoMatch
     * @param waitForCheckpoint
     * @param waitForCompletion
     * @return If the response is acknowledged
     * @throws ElasticServiceException
     */
    boolean stopTransform(String name, boolean allowNoMatch, boolean waitForCheckpoint, boolean waitForCompletion) throws ElasticServiceException;

    /**
     * Search asset index
     *
     * @param query Asset query Object with the values of the front end filters
     * @return
     * @throws ElasticServiceException
     */
    ElasticAssetQueryResult searchAssets(ElasticAssetQuery query) throws ElasticServiceException;

    /**
     * Search asset view aggregate index
     *
     * @param query
     * @return
     */
    DataSeries<BigDecimal> searchAssetViews(AssetViewQuery query) throws ElasticServiceException;

}
