package eu.opertusmundi.common.feign.client;

import java.util.UUID;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import eu.opertusmundi.common.model.sinergise.CatalogueResponseDto;
import eu.opertusmundi.common.model.sinergise.server.AccountTypeResponseDto;
import eu.opertusmundi.common.model.sinergise.server.ContractResponseDto;
import eu.opertusmundi.common.model.sinergise.server.CreateContractCommandDto;
import eu.opertusmundi.common.model.sinergise.server.CreateContractResponse;
import eu.opertusmundi.common.model.sinergise.server.GroupResponseDto;
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

    /**
     * List groups
     *
     * Only the groups where the requesting user is part of will be listed.
     *
     * @param accessToken
     * @return
     *
     * @see https://docs.sentinel-hub.com/api/management_api/reference/#operation/listGroupsByUser
     */
    @GetMapping(
        value = "/api/v1/management/basic/groups",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    GroupResponseDto getGroups(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String accessToken
    );

    /**
     * List available account types
     *
     * @param accessToken
     * @param groupId
     * @return
     *
     * @see https://docs.sentinel-hub.com/api/management_api/reference/#operation/listAvailableAccountTypes
     */
    @GetMapping(
        value = "/api/v1/management/basic/groups/{groupId}/accounttypes",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    AccountTypeResponseDto getAccountTypes(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String accessToken,
        @PathVariable UUID groupId
    );

    /**
     * List the user contracts on the specified group
     *
     * @param accessToken
     * @param groupId
     * @return
     *
     * @see https://docs.sentinel-hub.com/api/management_api/reference/#operation/listContracts
     */
    @GetMapping(
        value = "/api/v1/management/basic/groups/{groupId}/contracts",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    ContractResponseDto getContracts(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String accessToken,
        @PathVariable UUID groupId
    );

    /**
     * Create a new management contract
     *
     * @param accessToken
     * @param groupId
     * @param command
     * @return
     *
     * @see https://docs.sentinel-hub.com/api/management_api/reference/#operation/createManagementContract
     */
    @PostMapping(
        value = "/api/v1/management/basic/groups/{groupId}/contracts",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    CreateContractResponse createContract(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String accessToken,
        @PathVariable UUID groupId,
        @RequestBody CreateContractCommandDto command
    );

}
