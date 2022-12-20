package eu.opertusmundi.common.service;

import java.util.List;
import java.util.UUID;

import org.springframework.lang.Nullable;

import eu.opertusmundi.common.model.RequestContext;
import eu.opertusmundi.common.model.catalogue.CatalogueResult;
import eu.opertusmundi.common.model.catalogue.CatalogueServiceException;
import eu.opertusmundi.common.model.catalogue.IdVersionPair;
import eu.opertusmundi.common.model.catalogue.client.CatalogueAssetQuery;
import eu.opertusmundi.common.model.catalogue.client.CatalogueDraftQuery;
import eu.opertusmundi.common.model.catalogue.client.CatalogueHarvestCommandDto;
import eu.opertusmundi.common.model.catalogue.client.CatalogueItemDetailsDto;
import eu.opertusmundi.common.model.catalogue.client.CatalogueItemDraftDto;
import eu.opertusmundi.common.model.catalogue.client.CatalogueItemDto;
import eu.opertusmundi.common.model.catalogue.elastic.ElasticAssetQuery;
import eu.opertusmundi.common.model.catalogue.server.CatalogueFeature;

public interface CatalogueService {

    /**
     * Search for published features
     *
     * @param ctx
     * @param request
     * @return
     * @throws CatalogueServiceException
     */
    List<CatalogueFeature> findAllFeatures(CatalogueAssetQuery request) throws CatalogueServiceException;

    /**
     * Search for published items
     *
     * @param ctx
     * @param request
     * @return
     * @throws CatalogueServiceException
     */
    CatalogueResult<CatalogueItemDto> findAll(RequestContext ctx, CatalogueAssetQuery request) throws CatalogueServiceException;

    /**
     * Find all related assets
     *
     * @param ctx
     * @param id
     * @return
     * @throws CatalogueServiceException
     */
    CatalogueResult<CatalogueItemDto> findAllRelatedAssets(RequestContext ctx, String id) throws CatalogueServiceException;

    /**
     * Find all related collections of assets (bundles)
     *
     * @param ctx
     * @param id
     * @return
     * @throws CatalogueServiceException
     */
    CatalogueResult<CatalogueItemDto> findAllRelatedBundles(RequestContext ctx, String id) throws CatalogueServiceException;

    /**
     * Search for published items using Elasticsearch
     *
     * @param ctx
     * @param request
     * @return
     * @throws CatalogueServiceException
     */
    CatalogueResult<CatalogueItemDto> findAllElastic(RequestContext ctx, ElasticAssetQuery request) throws CatalogueServiceException;

    /**
     * Find all items with the specified identifiers in both published and
     * history catalogue tables
     *
     * @param id
     * @param throwOnMissing
     * @return
     * @throws CatalogueServiceException
     */
    List<CatalogueItemDetailsDto> findAllHistoryAndPublishedById(String[] id) throws CatalogueServiceException;

    /**
     * Find all items with the specified identifiers
     *
     * <p>
     * See {@link #findAllById(String[], boolean)}
     *
     * @param id
     * @return
     * @throws CatalogueServiceException
     */
    default List<CatalogueItemDetailsDto> findAllPublishedById(String[] id) throws CatalogueServiceException {
        return this.findAllPublishedById(id, true);
    }

    /**
     * Find all items with the specified identifiers
     *
     * @param id
     * @param throwOnMissing
     * @return
     * @throws CatalogueServiceException
     */
    List<CatalogueItemDetailsDto> findAllPublishedById(String[] id, boolean throwOnMissing) throws CatalogueServiceException;

    /**
     * Find all history items with the specified pairs of identifier and version
     * values
     *
     * <p>
     * See {@link #findAllHistoryByIdAndVersion(List, boolean)}
     *
     * @param pairs
     * @return
     * @throws CatalogueServiceException
     */
    default List<CatalogueItemDetailsDto> findAllHistoryByIdAndVersion(List<IdVersionPair> pairs) throws CatalogueServiceException {
        return this.findAllHistoryByIdAndVersion(pairs, true);
    }

    /**
     * Find all history items with the specified pairs of identifier and version
     * values
     *
     * @param pairs
     * @return
     * @throws CatalogueServiceException
     */
    List<CatalogueItemDetailsDto> findAllHistoryByIdAndVersion(List<IdVersionPair> pairs, boolean throwOnMissing) throws CatalogueServiceException;

    /**
     * Search for draft items
     *
     * @param request
     * @return
     * @throws CatalogueServiceException
     */
    CatalogueResult<CatalogueItemDraftDto> findAllDraft(CatalogueDraftQuery request) throws CatalogueServiceException;

    /**
     * Search for harvested items
     *
     * @param url
     * @param pageIndex
     * @param pageSize
     * @return
     * @throws CatalogueServiceException
     */
    CatalogueResult<CatalogueItemDto> findAllHarvested(String url, String query, int pageIndex, int pageSize) throws CatalogueServiceException;

    /**
     * Find one published item by its unique PID
     *
     * @param ctx
     * @param id
     * @param publisherKey
     * @param includeAutomatedMetadata
     * @return
     * @throws CatalogueServiceException
     */
    CatalogueItemDetailsDto findOne(
        @Nullable RequestContext ctx, String id, @Nullable UUID publisherKey, boolean includeAutomatedMetadata
    ) throws CatalogueServiceException;

    /**
     * Find one published item by its unique PID and version
     *
     * @param ctx
     * @param id
     * @param version
     * @param publisherKey
     * @param includeAutomatedMetadata
     * @return
     * @throws CatalogueServiceException
     */
    CatalogueItemDetailsDto findOne(
        RequestContext ctx, String id, String version, @Nullable UUID publisherKey, boolean includeAutomatedMetadata
    ) throws CatalogueServiceException;

    /**
     * Find one catalogue feature item by its unique PID
     *
     * @param id
     * @return
     * @throws CatalogueServiceException
     */
    CatalogueFeature findOneFeature(String id) throws CatalogueServiceException;

    /**
     * Find one catalogue feature by its unique PID from history
     *
     * @param id
     * @param version
     * @return
     * @throws CatalogueServiceException
     */
    CatalogueFeature findOneHistoryFeature(String id, String version) throws CatalogueServiceException;

    /**
     * Find a harvested resource by its unique identifier
     *
     * <p>
     * Note: Currently the catalogue URL is not used for indexing harvested
     * resources. As a result the identifier is unique only in the scope of the
     * harvested catalogue.
     * </p>
     *
     * @param id
     * @return
     * @throws CatalogueServiceException
     */
    CatalogueFeature findOneHarvested(String id) throws CatalogueServiceException;

    /**
     * Harvest catalogue
     *
     * @param command
     * @throws CatalogueServiceException
     */
    void harvestCatalogue(CatalogueHarvestCommandDto command) throws CatalogueServiceException;

    /**
     * Publish feature to catalogue
     *
     * @param feature
     * @throws CatalogueServiceException
     */
    void publish(CatalogueFeature feature) throws CatalogueServiceException;

    /**
     * Delete a published asset with the given PID
     *
     * @param publisherKey
     * @param pid
     * @return The removed catalogue item
     * @throws CatalogueServiceException
     */
    CatalogueItemDetailsDto unpublish(UUID publisherKey, String pid) throws CatalogueServiceException;
}
