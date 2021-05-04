package eu.opertusmundi.common.service;

import eu.opertusmundi.common.model.catalogue.elastic.ElasticAssetQuery;
import eu.opertusmundi.common.model.catalogue.elastic.ElasticAssetQueryResult;
import eu.opertusmundi.common.model.catalogue.elastic.ElasticServiceException;
import eu.opertusmundi.common.model.catalogue.elastic.IndexDefinition;
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
     * Initializes all registered index definitions
     *
     * @throws ElasticServiceException
     */
    void initializeIndices() throws ElasticServiceException;

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
     * Search asset index
     *
     * @param assetQuery Asset query Object with the values of the front end filters
     * @return
     * @throws ElasticServiceException
     */
    ElasticAssetQueryResult searchAssets(ElasticAssetQuery assetQuery) throws ElasticServiceException;

}
