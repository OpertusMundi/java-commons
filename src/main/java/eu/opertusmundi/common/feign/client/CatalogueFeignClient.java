package eu.opertusmundi.common.feign.client;

import java.util.List;
import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import eu.opertusmundi.common.feign.client.config.CatalogueFeignClientConfiguration;
import eu.opertusmundi.common.model.EnumSortingOrder;
import eu.opertusmundi.common.model.catalogue.EnumCatalogueSortField;
import eu.opertusmundi.common.model.catalogue.server.CatalogueCollection;
import eu.opertusmundi.common.model.catalogue.server.CatalogueFeature;
import eu.opertusmundi.common.model.catalogue.server.CatalogueResponse;
import eu.opertusmundi.common.model.catalogue.server.HistoryIdVersionQuery;

@FeignClient(
    name = "${opertusmundi.feign.catalogue.name}",
    url = "${opertusmundi.feign.catalogue.url}",
    configuration = CatalogueFeignClientConfiguration.class
)
public interface CatalogueFeignClient {

    /**
     * Search catalogue items
     *
     * @param query Search query
     * @param pageIndex The page index. Page index is 1-based
     * @param publisher Publisher unique identifier
     * @param pageSize The page size
     * @return An instance of {@link CatalogCollection}
     */
    @GetMapping(value = "/api/published/search", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<CatalogueResponse<CatalogueCollection>> findAll(
        @RequestParam("q") String query,
        @RequestParam(name = "publisher_id", required = false) String publisher,
        @RequestParam(name = "type", required = false) String type,
        @RequestParam("page") int pageIndex,
        @RequestParam("per_page") int pageSize,
        @RequestParam("orderBy") EnumCatalogueSortField orderBy,
        @RequestParam("order") EnumSortingOrder order
    );

    /**
     * Get a set of items by their identifiers
     *
     * @param id A list of item unique identifiers.
     * @return An instance of {@link CatalogueServerItem}
     */
    @GetMapping(value = "/api/published/get", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<CatalogueResponse<List<CatalogueFeature>>> findAllPublishedById(@RequestParam("id") String[] id);

    /**
     * Returns a specific version for each item id and version pair
     *
     * @param query
     * @return An instance of {@link CatalogueServerItem}
     */
    @PostMapping(value = "/api/history/get_list", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<CatalogueResponse<List<CatalogueFeature>>> findAllHistoryByIdAndVersion(@RequestBody HistoryIdVersionQuery query);

    /**
     * Find related assets given an asset's identifier
     *
     * @param id The asset identifier whose related assets are requested
     * @return An instance of {@link CatalogueResponse}
     */
    @GetMapping(value = "/api/published/related_data_source_items/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<CatalogueResponse<List<CatalogueFeature>>> findAllRelatedAssets(@PathVariable("id") String id);

    /**
     * Find collections of assets (bundles) that the asset belongs to
     *
     * @param id The asset identifier
     * @return An instance of {@link CatalogueResponse}
     */
    @GetMapping(value = "/api/published/available_as/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<CatalogueResponse<List<CatalogueFeature>>> findAllRelatedBundles(@PathVariable("id") String id);

    /**
     * Get an item by id
     *
     * @param id The item unique identifier.
     * @return An instance of {@link CatalogueFeature}
     */
    @GetMapping(value = "/api/published/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<CatalogueResponse<CatalogueFeature>> findOneById(@PathVariable("id") String id);

    /**
     * Get an item by id and version
     *
     * @param id The item unique identifier.
     * @param version The selected version
     * @return An instance of {@link CatalogueFeature}
     */
    @GetMapping(value = "/api/history/get", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<CatalogueResponse<CatalogueFeature>> findOneByIdAndVersion(
        @RequestParam("id") String id, @RequestParam("version") String version
    );

    /**
     * Delete a published asset
     *
     * @param id The identifier of the asset to update
     * @return
     */
    @DeleteMapping(value = "/api/published/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Void> deletePublished(@PathVariable("id") String id);

    /**
     * Search draft items
     *
     * @param publisher (Optional) Publisher unique id
     * @param status (Optional) Draft item status
     * @param pageIndex The page index. Page index is 1-based
     * @param pageSize The page size
     * @return An instance of {@link CatalogCollection}
     */
    @GetMapping(value = "/api/draft/search")
    ResponseEntity<CatalogueResponse<CatalogueCollection>> findAllDraft(
        @RequestParam(name = "publisher_id", required = false) String publisher,
        @RequestParam(name = "status", required = false) String status,
        @RequestParam(name = "page", defaultValue = "1") int pageIndex,
        @RequestParam(name = "per_page", defaultValue = "10") int pageSize
    );

    /**
     * Get a draft item by id
     *
     * @param id The item unique identifier.
     * @return An instance of {@link CatalogueFeature}
     */
    @GetMapping(value = "/api/draft/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<CatalogueResponse<CatalogueFeature>> findOneDraftById(@PathVariable("id") String id);

    /**
     * Create a new draft item
     *
     * @param feature The feature to create
     */
    @PostMapping(value = "/api/draft/create", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Void> createDraft(@RequestBody CatalogueFeature feature);

    /**
     * Create a new draft item from an existing catalogue item
     *
     * @param feature The feature to create
     */
    @PostMapping(value = "/api/draft/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Void> createDraftFromPublished(@PathVariable("id") String id);

    /**
     * Create a new draft item from an existing catalogue item
     *
     * @param id The identifier of the harvested item
     */
    @PostMapping(value = "/api/draft/create_from_harvest/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Void> createDraftFromHarvester(@PathVariable("id") String id);

    /**
     * Create a new draft item from an ISO (XML) document
     *
     * @param xml The feature to create
     */
    @PostMapping(value = "/api/draft/create_from_iso/", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Void> createDraftFromIso(@RequestParam("xml") String xml);

    /**
     * Update an existing draft item
     *
     * @param feature The updated feature
     */
    @PutMapping(value = "/api/draft/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Void> updateDraft(@PathVariable("id") String id, @RequestBody CatalogueFeature feature);

    /**
     * Update draft item status
     *
     * @param id The identifier of the draft to update
     * @param status The new status value
     * @return
     */
    @PutMapping(value = "/api/draft/status", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Void> setDraftStatus(
        @RequestParam(name = "id", required = true) String id,
        @RequestParam(name = "status", required = true) String status
    );

    /**
     * Delete a draft item
     * @param id The identifier of the draft to update
     * @return
     */
    @DeleteMapping(value = "/api/draft/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Void> deleteDraft(@PathVariable("id") String id);

    /**
     * Search history items
     *
     * @param id (Optional) Item unique id
     * @param publisher (Optional) Publisher unique id
     * @param deleted (Optional) If <code>true</code> load only deleted items
     * @param status (Optional) Draft item status
     * @param pageIndex The page index. Page index is 1-based
     * @param pageSize The page size
     * @return An instance of {@link CatalogCollection}
     */
    @GetMapping(value = "/api/history/search")
    ResponseEntity<CatalogueResponse<CatalogueCollection>> findAllHistory(
        @RequestParam(name = "item_id", required = false) String id,
        @RequestParam(name = "publisher_id", required = false) UUID publisher,
        @RequestParam(name = "deleted", required = false) Boolean deleted,
        @RequestParam(name = "page", defaultValue = "1") int pageIndex,
        @RequestParam(name = "per_page", defaultValue = "10") int pageSize
    );

    /**
     * Harvest catalogue metadata
     *
     * @param url
     * @param harvester
     * @return
     */
    @PostMapping(value = "/api/harvest", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Void> harvestCatalogue(
        @RequestParam("url") String url,
        @RequestParam("harvester") String harvester
    );

    /**
     * Search harvested items
     *
     * @param url Catalogue URL
     * @param query Search query
     * @param pageIndex The page index. Page index is 1-based
     * @param pageSize The page size
     * @return An instance of {@link CatalogCollection}
     */
    @GetMapping(value = "/api/harvest/search", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<CatalogueResponse<CatalogueCollection>> findAllHarvest(
        @RequestParam("harvest_url") String url,
        @RequestParam("q") String query,
        @RequestParam("page") int pageIndex,
        @RequestParam("per_page") int pageSize
    );

    // TODO: Add URL parameter. Identifiers may not be unique

    /**
     * Get a harvested item by id
     *
     * @param id The item unique identifier.
     * @return An instance of {@link CatalogueFeature}
     */
    @GetMapping(value = "/api/harvest/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<CatalogueResponse<CatalogueFeature>> findOneHarvestedItemById(@PathVariable("id") String id);

}
