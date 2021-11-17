package eu.opertusmundi.common.feign.client;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import eu.opertusmundi.common.model.sinergise.CatalogueResponseDto;
import eu.opertusmundi.common.model.sinergise.server.ServerCatalogueQueryDto;
import eu.opertusmundi.common.model.sinergise.server.ServerTokenResponseDto;
import feign.Headers;

/**
 * Feign client for Sentinel Hub services
 */
public interface SentinelHubFeignClient {

    /**
     * Request token
     *
     * @see https://docs.sentinel-hub.com/api/latest/api/overview/authentication/
     */
    @PostMapping(
        value = "/oauth/token",
        consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Headers("Content-Type: application/x-www-form-urlencoded")
    ServerTokenResponseDto requestToken(
        @RequestParam("grant_type") String grantType,
        @RequestParam("client_id") String clientId,
        @RequestParam("client_secret") String clientSecret
    );

    /**
     * Search STAC items with full-featured filtering
     *
     * @param accessToken
     * @param query
     * @return
     *
     * @see https://docs.sentinel-hub.com/api/latest/reference/#operation/postSearchSTAC
     */
    @PostMapping(
        value = "/api/v1/catalog/search",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    CatalogueResponseDto search(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String accessToken,
        @RequestBody ServerCatalogueQueryDto query
    );

}
