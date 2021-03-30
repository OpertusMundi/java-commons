package eu.opertusmundi.common.service;

import java.util.List;
import java.util.UUID;

import org.springframework.lang.Nullable;

import eu.opertusmundi.common.model.catalogue.CatalogueResult;
import eu.opertusmundi.common.model.catalogue.CatalogueServiceException;
import eu.opertusmundi.common.model.catalogue.client.CatalogueAssetQuery;
import eu.opertusmundi.common.model.catalogue.client.CatalogueDraftQuery;
import eu.opertusmundi.common.model.catalogue.client.CatalogueHarvestCommandDto;
import eu.opertusmundi.common.model.catalogue.client.CatalogueItemDetailsDto;
import eu.opertusmundi.common.model.catalogue.client.CatalogueItemDraftDto;
import eu.opertusmundi.common.model.catalogue.client.CatalogueItemDto;

public interface CatalogueService {

    /**
     * Search for published items
     * 
     * @param request
     * @return
     * @throws CatalogueServiceException
     */
    CatalogueResult<CatalogueItemDto> findAll(CatalogueAssetQuery request) throws CatalogueServiceException;

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
     * @param id
     * @param userKey
     * @param includeAutomatedMetadata
     * @return
     * @throws CatalogueServiceException
     */
    CatalogueItemDetailsDto findOne(String id, @Nullable UUID userKey, boolean includeAutomatedMetadata) throws CatalogueServiceException;

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

}
