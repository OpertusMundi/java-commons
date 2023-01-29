package eu.opertusmundi.common.feign.client;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import eu.opertusmundi.common.model.discovery.server.ServerJoinableTableResultDto;
import eu.opertusmundi.common.model.discovery.server.ServerRelatedTableResultDto;

@ConditionalOnProperty(name = "opertusmundi.feign.discovery.url", matchIfMissing = true)
@FeignClient(
    name = "discovery-service",
    url = "${opertusmundi.feign.discovery.url}"
)
public interface DiscoveryServiceFeignClient {

    /**
     * Gets all assets that are joinable with the given source asset
     *
     * @param assetId The id of the asset to get the table from
     * @return
     */
    @GetMapping(value = "/get-joinable", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ServerJoinableTableResultDto> getJoinable(@RequestParam("asset_id") String assetId);

    /**
     * Get all the assets on the path connecting the source and the target tables
     *
     * @param sourceAssetId The id of the asset to get the table from as source
     * @param targetAssetIds The id of the asset to get the table from as target
     * @return
     */
    @GetMapping(value = "/get-related", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ServerRelatedTableResultDto> getRelated(
        @RequestParam("source_asset_id")  String   sourceAssetId,
        @RequestParam("target_asset_ids") String[] targetAssetIds
    );

}
