package eu.opertusmundi.common.feign.client;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import eu.opertusmundi.common.feign.client.config.KeycloakAdminFeignClientConfiguration;
import eu.opertusmundi.common.model.keycloak.server.*;

/**
 * A Feign client for the Keycloak Admin REST API.
 * <p>
 * Note: This client supports only a small part of the (v15.0) Admin API.
 * 
 * @see https://www.keycloak.org/docs-api/15.0/rest-api/index.html
 */
@FeignClient(
    value = "keycloakAdminClient",
    url = "${opertusmundi.feign.keycloak.url}/auth",
    configuration = KeycloakAdminFeignClientConfiguration.class)
public interface KeycloakAdminFeignClient
{
    /**
     * Refresh token and obtain a new access token.
     * <p>
     * Note that this method is not technically part of the Admin API 
     * (is part of the OAuth2 spec for refreshing an access token if holding a refresh token).
     * 
     * @param realm
     * @param refreshTokenForm
     * 
     * @see https://datatracker.ietf.org/doc/html/rfc6749#section-6
     * @see https://github.com/keycloak/keycloak-documentation/blob/main/server_admin/topics/sessions/offline.adoc
     */
    @PostMapping(
        path = "realms/{realm}/protocol/openid-connect/token",
        produces = MediaType.APPLICATION_JSON_VALUE, 
        consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    RefreshTokenResponse refreshToken(
        @PathVariable("realm") String realm, 
        RefreshTokenForm refreshTokenForm);
    
    @GetMapping(
        path = "admin/realms/{realm}/users",
        produces = MediaType.APPLICATION_JSON_VALUE)
    List<UserDto> findUsers(
        @PathVariable("realm") String realm,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader, 
        @SpringQueryMap UserQueryDto query);
    
    @GetMapping(
        path = "admin/realms/{realm}/users/count",
        produces = MediaType.APPLICATION_JSON_VALUE)
    int countUsers(
        @PathVariable("realm") String realm,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader, 
        @SpringQueryMap UserQueryDto query);
    
    @GetMapping(
        path = "admin/realms/{realm}/users/{userId}",
        produces = MediaType.APPLICATION_JSON_VALUE)
    UserDto getUser(
        @PathVariable("realm") String realm,
        @PathVariable("userId") UUID userId,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader);
    
    @PostMapping(
        path = "admin/realms/{realm}/users",
        produces = MediaType.APPLICATION_JSON_VALUE,
        consumes = MediaType.APPLICATION_JSON_VALUE)
    void createUser(
        @PathVariable("realm") String realm,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
        @RequestBody UserDto user);
    
    @PutMapping(
        path = "admin/realms/{realm}/users/{userId}",
        produces = MediaType.APPLICATION_JSON_VALUE, 
        consumes = MediaType.APPLICATION_JSON_VALUE)
    void updateUser(
        @PathVariable("realm") String realm,
        @PathVariable("userId") UUID userId,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
        @RequestBody UserDto user);
    
    @DeleteMapping(
        path = "admin/realms/{realm}/users/{userId}",
        produces = MediaType.APPLICATION_JSON_VALUE)
    void deleteUser(
        @PathVariable("realm") String realm,
        @PathVariable("userId") UUID userId,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader);
    
    @PutMapping(
        path = "admin/realms/{realm}/users/{userId}/reset-password",
        produces = MediaType.APPLICATION_JSON_VALUE,
        consumes = MediaType.APPLICATION_JSON_VALUE) 
    void resetPasswordForUser(
        @PathVariable("realm") String realm,
        @PathVariable("userId") UUID userId,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
        @RequestBody CredentialDto cred);
    
    @GetMapping(
        path = "admin/realms/{realm}/groups",
        produces = MediaType.APPLICATION_JSON_VALUE)
    List<GroupDto> findGroups(
        @PathVariable("realm") String realm,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
        @SpringQueryMap GroupQueryDto query);
    
    @GetMapping(
        path = "admin/realms/{realm}/groups/{groupId}/members",
        produces = MediaType.APPLICATION_JSON_VALUE)
    List<UserDto> getGroupMembers(
        @PathVariable("realm") String realm,
        @PathVariable("groupId") UUID groupId,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
        @SpringQueryMap PageRequest pageRequest);
    
    @GetMapping(
        path = "admin/realms/{realm}/users/{userId}/groups",
        produces = MediaType.APPLICATION_JSON_VALUE)
    List<GroupDto> getUserGroups(
        @PathVariable("realm") String realm,
        @PathVariable("userId") UUID userId,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader);
    
    @PostMapping(
        path = "admin/realms/{realm}/groups",
        produces = MediaType.APPLICATION_JSON_VALUE)
    void createGroup(
        @PathVariable("realm") String realm,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
        @RequestBody GroupDto group);
   
    @PutMapping(
        path = "admin/realms/{realm}/groups/{groupId}",
        produces = MediaType.APPLICATION_JSON_VALUE)
    void updateGroup(
        @PathVariable("realm") String realm,
        @PathVariable("groupId") UUID groupId,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
        @RequestBody GroupDto group);
}
