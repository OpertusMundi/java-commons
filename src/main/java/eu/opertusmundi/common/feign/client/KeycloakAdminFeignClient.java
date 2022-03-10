package eu.opertusmundi.common.feign.client;

import java.util.List;
import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
    url = "${opertusmundi.feign.keycloak.url}",
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
    
    //
    // Admin API
    //
    
    /**
     * Find users
     * 
     * @param realm
     * @param authorizationHeader
     * @param query
     * @return
     */
    @GetMapping(
        path = "admin/realms/{realm}/users",
        produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<List<UserDto>> findUsers(
        @PathVariable("realm") String realm,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader, 
        @SpringQueryMap UserQueryDto query);
    
    /**
     * Count users
     * 
     * @param realm
     * @param authorizationHeader
     * @param query
     * @return
     */
    @GetMapping(
        path = "admin/realms/{realm}/users/count",
        produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Integer> countUsers(
        @PathVariable("realm") String realm,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader, 
        @SpringQueryMap UserQueryDto query);
    
    /**
     * Get user by ID
     * 
     * @param realm
     * @param userId
     * @param authorizationHeader
     * @return
     */
    @GetMapping(
        path = "admin/realms/{realm}/users/{userId}",
        produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<UserDto> getUser(
        @PathVariable("realm") String realm,
        @PathVariable("userId") UUID userId,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader);
    
    /**
     * Create user. The URI for the newly created user is returned a {@code location} response header.
     * 
     * @param realm
     * @param authorizationHeader
     * @param user
     * @return
     */
    @PostMapping(
        path = "admin/realms/{realm}/users",
        produces = MediaType.APPLICATION_JSON_VALUE,
        consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Void> createUser(
        @PathVariable("realm") String realm,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
        @RequestBody UserDto user);
    
    /**
     * Update user
     * 
     * @param realm
     * @param userId
     * @param authorizationHeader
     * @param user The DTO from which user is updated; a {@code null} field will leave
     *   the corresponding field unmodified (instead of nulling it)
     * @return
     */
    @PutMapping(
        path = "admin/realms/{realm}/users/{userId}",
        produces = MediaType.APPLICATION_JSON_VALUE, 
        consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Void> updateUser(
        @PathVariable("realm") String realm,
        @PathVariable("userId") UUID userId,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
        @RequestBody UserDto user);
    
    /**
     * Delete user
     * 
     * @param realm
     * @param userId
     * @param authorizationHeader
     * @return
     */
    @DeleteMapping(
        path = "admin/realms/{realm}/users/{userId}",
        produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Void> deleteUser(
        @PathVariable("realm") String realm,
        @PathVariable("userId") UUID userId,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader);
    
    /**
     * Reset password for a user
     * 
     * @param realm
     * @param userId
     * @param authorizationHeader
     * @param cred
     * @return
     */
    @PutMapping(
        path = "admin/realms/{realm}/users/{userId}/reset-password",
        produces = MediaType.APPLICATION_JSON_VALUE,
        consumes = MediaType.APPLICATION_JSON_VALUE) 
    ResponseEntity<Void> resetPasswordForUser(
        @PathVariable("realm") String realm,
        @PathVariable("userId") UUID userId,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
        @RequestBody CredentialDto cred);
    
    @GetMapping(
        path = "admin/realms/{realm}/groups",
        produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<List<GroupDto>> findGroups(
        @PathVariable("realm") String realm,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
        @SpringQueryMap GroupQueryDto query);
    
    /**
     * Get a group
     * 
     * @param realm
     * @param groupId
     * @param authorizationHeader
     * @return
     */
    @GetMapping(
        path = "admin/realms/{realm}/groups/{groupId}",
        produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<GroupDto> getGroup(
        @PathVariable("realm") String realm,
        @PathVariable("groupId") UUID groupId,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader);
    
    /**
     * Get members (users) of a group 
     * 
     * @param realm
     * @param groupId
     * @param authorizationHeader
     * @param pageRequest
     * @return
     */
    @GetMapping(
        path = "admin/realms/{realm}/groups/{groupId}/members",
        produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<List<UserDto>> getGroupMembers(
        @PathVariable("realm") String realm,
        @PathVariable("groupId") UUID groupId,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
        @SpringQueryMap PageRequest pageRequest);
    
    /**
     * Get groups for a given user
     * 
     * @param realm
     * @param userId
     * @param authorizationHeader
     * @return
     */
    @GetMapping(
        path = "admin/realms/{realm}/users/{userId}/groups",
        produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<List<GroupDto>> getUserGroups(
        @PathVariable("realm") String realm,
        @PathVariable("userId") UUID userId,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader);
    
    /**
     * Create a group. The URI for the newly created group is returned a {@code location} response header.
     * 
     * @param realm
     * @param authorizationHeader
     * @param group
     * @return
     */
    @PostMapping(
        path = "admin/realms/{realm}/groups",
        produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Void> createGroup(
        @PathVariable("realm") String realm,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
        @RequestBody GroupDto group);
   
    /**
     * Update a group
     * 
     * @param realm
     * @param groupId
     * @param authorizationHeader
     * @param group The DTO from which group is updated; a {@code null} field will leave
     *   the corresponding field unmodified (instead of nulling it)
     * @return
     */
    @PutMapping(
        path = "admin/realms/{realm}/groups/{groupId}",
        produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Void> updateGroup(
        @PathVariable("realm") String realm,
        @PathVariable("groupId") UUID groupId,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
        @RequestBody GroupDto group);
    
    /**
     * Delete a group
     * 
     * @param realm
     * @param groupId
     * @param authorizationHeader
     * @return
     */
    @DeleteMapping(
        path = "admin/realms/{realm}/groups/{groupId}",
        produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Void> deleteGroup(
        @PathVariable("realm") String realm,
        @PathVariable("groupId") UUID groupId,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader);
}
