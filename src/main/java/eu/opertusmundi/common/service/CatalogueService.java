package eu.opertusmundi.common.service;

import java.util.List;
import java.util.UUID;

import org.springframework.lang.Nullable;

import eu.opertusmundi.common.model.RequestContext;
import eu.opertusmundi.common.model.catalogue.CatalogueResult;
import eu.opertusmundi.common.model.catalogue.CatalogueServiceException;
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
    CatalogueResult<CatalogueItemDto> findAllRelated(RequestContext ctx, String id) throws CatalogueServiceException;

    /**
     * Search for published items using Elasticsearch
     *
     * @param ctx
     * @param request
     * @return
     * @throws CatalogueServiceException
     */
    CatalogueResult<CatalogueItemDto> findAllAdvanced(RequestContext ctx, ElasticAssetQuery request) throws CatalogueServiceException;

    /**
     * Find all items with the specified identifiers
     *
     * @param id
     * @return
     * @throws CatalogueServiceException
     */
    List<CatalogueItemDto> findAllById(String[] id) throws CatalogueServiceException;

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
        RequestContext ctx, String id, @Nullable UUID publisherKey, boolean includeAutomatedMetadata
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
     * Delete published asset with the given PID
     *
     * @param pid
     * @throws CatalogueServiceException
     */
    void deleteAsset(String pid);

    /**
     * Publish feature to catalogue
     *
     * @param feature
     * @throws CatalogueServiceException
     */
    void publish(CatalogueFeature feature) throws CatalogueServiceException;

}
