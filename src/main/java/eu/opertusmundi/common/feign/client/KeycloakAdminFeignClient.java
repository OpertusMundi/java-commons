package eu.opertusmundi.common.feign.client;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import eu.opertusmundi.common.model.keycloak.server.ClientDto;
import eu.opertusmundi.common.model.keycloak.server.ClientQueryDto;
import eu.opertusmundi.common.model.keycloak.server.CredentialDto;
import eu.opertusmundi.common.model.keycloak.server.EnumRequiredAction;
import eu.opertusmundi.common.model.keycloak.server.GroupDto;
import eu.opertusmundi.common.model.keycloak.server.GroupQueryDto;
import eu.opertusmundi.common.model.keycloak.server.PageRequest;
import eu.opertusmundi.common.model.keycloak.server.RoleDto;
import eu.opertusmundi.common.model.keycloak.server.RoleQueryDto;
import eu.opertusmundi.common.model.keycloak.server.UserDto;
import eu.opertusmundi.common.model.keycloak.server.UserQueryDto;


/**
 * A Feign client for the Keycloak Admin REST API.
 * <p>
 * Note: This client supports only a small part of the (v15.0) Admin API.
 * 
 * @see https://www.keycloak.org/docs-api/15.0/rest-api/index.html
 */
public interface KeycloakAdminFeignClient
{    
    @GetMapping(
        path = "admin/realms/{realm}/users", 
        produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<List<UserDto>> findUsers(
        @PathVariable("realm") String realm,
        @SpringQueryMap UserQueryDto query);
    
    @GetMapping(
        path = "admin/realms/{realm}/users/count",
        produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Integer> countUsers(
        @PathVariable("realm") String realm, 
        @SpringQueryMap UserQueryDto query);
    
    @GetMapping(
        path = "admin/realms/{realm}/users/{id}",
        produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<UserDto> getUser(
        @PathVariable("realm") String realm,
        @PathVariable("id") UUID userId);
    
    @PostMapping(
        path = "admin/realms/{realm}/users",
        produces = MediaType.APPLICATION_JSON_VALUE,
        consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Void> createUser(
        @PathVariable("realm") String realm,
        @RequestBody UserDto user);
    
    @PutMapping(
        path = "admin/realms/{realm}/users/{id}",
        produces = MediaType.APPLICATION_JSON_VALUE, 
        consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Void> updateUser(
        @PathVariable("realm") String realm,
        @PathVariable("id") UUID userId,
        @RequestBody UserDto user);
    
    @DeleteMapping(
        path = "admin/realms/{realm}/users/{id}",
        produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Void> deleteUser(
        @PathVariable("realm") String realm,
        @PathVariable("id") UUID userId);
    
    @PutMapping(
        path = "admin/realms/{realm}/users/{id}/reset-password",
        produces = MediaType.APPLICATION_JSON_VALUE,
        consumes = MediaType.APPLICATION_JSON_VALUE) 
    ResponseEntity<Void> resetUserPassword(
        @PathVariable("realm") String realm,
        @PathVariable("id") UUID userId,
        @RequestBody CredentialDto cred);
    
    @PutMapping(
        path = "admin/realms/{realm}/users/{id}/execute-actions-email",
        produces = MediaType.APPLICATION_JSON_VALUE,
        consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Void> executeEmailActionsForUser(
        @PathVariable("realm") String realm,
        @PathVariable("id") UUID userId,
        @RequestBody Set<EnumRequiredAction> actions);
    
    @GetMapping(
        path = "admin/realms/{realm}/groups",
        produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<List<GroupDto>> findGroups(
        @PathVariable("realm") String realm,
        @SpringQueryMap GroupQueryDto query);
    
    @GetMapping(
        path = "admin/realms/{realm}/groups/{groupId}",
        produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<GroupDto> getGroup(
        @PathVariable("realm") String realm,
        @PathVariable("groupId") UUID groupId);
    
    @GetMapping(
        path = "admin/realms/{realm}/groups/{id}/members",
        produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<List<UserDto>> getGroupMembers(
        @PathVariable("realm") String realm,
        @PathVariable("id") UUID groupId,
        @SpringQueryMap PageRequest pageRequest);
    
    @GetMapping(
        path = "admin/realms/{realm}/users/{id}/groups",
        produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<List<GroupDto>> getUserGroups(
        @PathVariable("realm") String realm,
        @PathVariable("id") UUID userId);
    
    @PostMapping(
        path = "admin/realms/{realm}/groups",
        produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Void> createGroup(
        @PathVariable("realm") String realm,
        @RequestBody GroupDto group);
   
    @PutMapping(
        path = "admin/realms/{realm}/groups/{id}",
        produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Void> updateGroup(
        @PathVariable("realm") String realm,
        @PathVariable("id") UUID groupId,
        @RequestBody GroupDto group);
    
    @DeleteMapping(
        path = "admin/realms/{realm}/groups/{id}",
        produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Void> deleteGroup(
        @PathVariable("realm") String realm,
        @PathVariable("id") UUID groupId);
    
    @GetMapping(
        path = "admin/realms/{realm}/roles",
        produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<List<RoleDto>> findRoles(
        @PathVariable("realm") String realm,
        @SpringQueryMap RoleQueryDto query);
    
    @GetMapping(
        path = "admin/realms/{realm}/roles/{roleName}",
        produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<RoleDto> getRoleByName(
        @PathVariable("realm") String realm,
        @PathVariable("roleName") String roleName);
    
    @GetMapping(
        path = "admin/realms/{realm}/roles/{roleName}/users",
        produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<List<UserDto>> findUsersInRole(
        @PathVariable("realm") String realm,
        @PathVariable("roleName") String roleName,
        @SpringQueryMap PageRequest pageRequest);
    
    @GetMapping(
        path = "admin/realms/{realm}/users/{userId}/role-mappings/realm",
        produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<List<RoleDto>> getRolesForUser(
        @PathVariable("realm") String realm,
        @PathVariable("userId") UUID userId);
    
    @PostMapping(
        path = "admin/realms/{realm}/users/{userId}/role-mappings/realm",
        produces = MediaType.APPLICATION_JSON_VALUE,
        consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Void> addRolesForUser(
        @PathVariable("realm") String realm,
        @PathVariable("userId") UUID userId,
        @RequestBody List<RoleDto> roles);
    
    @DeleteMapping(
        path = "admin/realms/{realm}/users/{userId}/role-mappings/realm",
        produces = MediaType.APPLICATION_JSON_VALUE,
        consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Void> removeRolesForUser(
        @PathVariable("realm") String realm,
        @PathVariable("userId") UUID userId,
        @RequestBody List<RoleDto> roles);
    
    @GetMapping(
        path = "admin/realms/{realm}/clients",
        produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<List<ClientDto>> findClients(
        @PathVariable("realm") String realm,
        @SpringQueryMap ClientQueryDto query);
    
    @GetMapping(
        path = "admin/realms/{realm}/clients/{id}",
        produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ClientDto> getClient(
        @PathVariable("realm") String realm,
        @PathVariable("id") UUID id);

    @GetMapping(
        path = "admin/realms/{realm}/clients/{id}/service-account-user",
        produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<UserDto> getServiceAccountUser(
        @PathVariable("realm") String realm,
        @PathVariable("id") UUID id);
    
    @PostMapping(
        path = "admin/realms/{realm}/clients/{id}/client-secret",
        produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<CredentialDto> regenerateClientSecret(
        @PathVariable("realm") String realm,
        @PathVariable("id") UUID id);
    
    @GetMapping(
        path = "admin/realms/{realm}/clients/{id}/client-secret",
        produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<CredentialDto> getClientSecret(
        @PathVariable("realm") String realm,
        @PathVariable("id") UUID id);
    
    @PostMapping(
        path = "admin/realms/{realm}/clients",
        produces = MediaType.APPLICATION_JSON_VALUE,
        consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Void> createClient(
        @PathVariable("realm") String realm,
        @RequestBody ClientDto client);
    
    @PutMapping(
        path = "admin/realms/{realm}/clients/{id}",
        produces = MediaType.APPLICATION_JSON_VALUE,
        consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Void> updateClient(
        @PathVariable("realm") String realm,
        @PathVariable("id") UUID id,
        @RequestBody ClientDto client);
    
    @DeleteMapping(
        path = "admin/realms/{realm}/clients/{id}",
        produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Void> deleteClient(
        @PathVariable("realm") String realm,
        @PathVariable("id") UUID id);
    
    @GetMapping(
        path = "admin/realms/{realm}/clients/{id}/roles",
        produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<List<RoleDto>> getClientRoles(
        @PathVariable("realm") String realm,
        @PathVariable("id") UUID id);
    
    @GetMapping(
        path = "admin/realms/{realm}/clients/{id}/roles/{roleName}",
        produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<RoleDto> getClientRoleByName(
        @PathVariable("realm") String realm,
        @PathVariable("id") UUID id,
        @PathVariable("roleName") String roleName);
    
    @PostMapping(
        path = "admin/realms/{realm}/clients/{id}/roles",
        produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Void> createClientRole(
        @PathVariable("realm") String realm,
        @PathVariable("id") UUID id,
        @RequestBody RoleDto role);
 
    @GetMapping(
        path = "admin/realms/{realm}/clients/{id}/roles/{roleName}/users",
        produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<List<UserDto>> findUsersInClientRole(
        @PathVariable("realm") String realm,
        @PathVariable("id") UUID id,
        @PathVariable("roleName") String roleName,
        @SpringQueryMap PageRequest pageRequest);
    
    @DeleteMapping(
        path = "admin/realms/{realm}/clients/{id}/roles/{roleName}",
        produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Void> deleteClientRole(
        @PathVariable("realm") String realm,
        @PathVariable("id") UUID id,
        @PathVariable("roleName") String roleName);
    
    @GetMapping(
        path = "admin/realms/{realm}/users/{userId}/role-mappings/clients/{clientUuid}",
        produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<List<RoleDto>> getClientRolesForUser(
        @PathVariable("realm") String realm,
        @PathVariable("userId") UUID userId,
        @PathVariable("clientUuid") UUID clientUuid);

    @PostMapping(
        path = "admin/realms/{realm}/users/{userId}/role-mappings/clients/{clientUuid}",
        produces = MediaType.APPLICATION_JSON_VALUE,
        consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Void> addClientRolesForUser(
        @PathVariable("realm") String realm,
        @PathVariable("userId") UUID userId,
        @PathVariable("clientUuid") UUID clientUuid,
        @RequestBody List<RoleDto> roles);
    
    @DeleteMapping(
        path = "admin/realms/{realm}/users/{userId}/role-mappings/clients/{clientUuid}",
        produces = MediaType.APPLICATION_JSON_VALUE,
        consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Void> removeClientRolesForUser(
        @PathVariable("realm") String realm,
        @PathVariable("userId") UUID userId,
        @PathVariable("clientUuid") UUID clientUuid,
        @RequestBody List<RoleDto> roles);
}