package eu.opertusmundi.common.feign.client;

import java.util.List;
import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import eu.opertusmundi.common.feign.client.config.RatingServiceFeignClientConfiguration;
import eu.opertusmundi.common.model.BaseResponse;
import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.rating.server.ServerAssetAverageRatingDto;
import eu.opertusmundi.common.model.rating.server.ServerAssetRatingCommandDto;
import eu.opertusmundi.common.model.rating.server.ServerProviderRatingCommandDto;
import eu.opertusmundi.common.model.rating.server.ServerRatingDto;

@FeignClient(
    name = "${opertusmundi.feign.rating-service.name}",
    url = "${opertusmundi.feign.rating-service.url}",
    configuration = RatingServiceFeignClientConfiguration.class
)
public interface RatingServiceFeignClient {

    /**
     * Get ratings for a single asset
     *
     * @param id Asset unique id
     *
     * @return An instance of {@link ResponseEntity}
     */
    @GetMapping(value = "/v1/rating/assets/{id}", produces = "application/json")
    ResponseEntity<RestResponse<List<ServerRatingDto>>> getAssetRatings(@PathVariable(name = "id", required = true) String id);

    /**
     * Get average rating for all assets selected by the specified asset
     * identifiers
     *
     * @param pids Asset identifiers
     *
     * @return An instance of {@link ResponseEntity}
     */
    @PostMapping(value = "/v1/rating/assets", produces = "application/json")
    ResponseEntity<RestResponse<List<ServerAssetAverageRatingDto>>> getAssetsAverageRatings(@RequestBody List<String> pids);

    /**
     * Get ratings for a single provider
     *
     * @param id Provider unique id
     *
     * @return An instance of {@link ResponseEntity}
     */
    @GetMapping(value = "/v1/rating/providers/{id}", produces = "application/json")
    ResponseEntity<RestResponse<List<ServerRatingDto>>> getProviderRatings(@PathVariable(name = "id", required = true) UUID id);

    /**
     * Add asset rating
     *
     * @param id Asset unique id
     * @param command The command object for adding a new rating for an asset
     *
     * @return An instance of {@link ResponseEntity}
     */
    @PostMapping(value = "/v1/rating/assets/{id}", produces = "application/json")
    ResponseEntity<BaseResponse> addAssetRating(
        @PathVariable(name = "id", required = true) String id,
        @RequestBody(required = true) ServerAssetRatingCommandDto command
    );

    /**
     * Add provider rating
     *
     * @param id Provider unique id
     * @param command The command object for adding a new rating for a provider
     *
     * @return An instance of {@link ResponseEntity}
     */
    @PostMapping(value = "/v1/rating/providers/{id}", produces = "application/json")
    ResponseEntity<BaseResponse> addProviderRating(
        @PathVariable(name = "id", required = true) UUID id,
        @RequestBody(required = true) ServerProviderRatingCommandDto command
    );

}
