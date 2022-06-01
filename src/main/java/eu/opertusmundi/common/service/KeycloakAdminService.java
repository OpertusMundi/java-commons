package eu.opertusmundi.common.service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.lang.Nullable;

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
import io.jsonwebtoken.lang.Assert;

public interface KeycloakAdminService
{
    default String getDefaultRealm() {
        return "master";
    }

    List<UserDto> findUsers(String realm, @Nullable UserQueryDto query);

    default List<UserDto> findUsers(UserQueryDto query) {
        return this.findUsers(this.getDefaultRealm(), query);
    }
    
    int countUsers(String realm, @Nullable UserQueryDto query);

    default int countUsers(@Nullable UserQueryDto query) {
        return this.countUsers(this.getDefaultRealm(), query);
    }
    
    Optional<UserDto> getUser(String realm, UUID userId);

    default Optional<UserDto> getUser(UUID userId) {
        return this.getUser(this.getDefaultRealm(), userId);
    }
    
    UUID createUser(String realm, UserDto user);

    default UUID createUser(UserDto user) {
        return this.createUser(this.getDefaultRealm(), user);
    }
    
    void updateUser(String realm, UserDto user);
    
    default void updateUser(UserDto user) {
        this.updateUser(this.getDefaultRealm(), user);
    }

    void deleteUser(String realm, UUID userId);

    default void deleteUser(UUID userId) {
       this.deleteUser(this.getDefaultRealm(), userId);
    }
    
    void resetPasswordForUser(String realm, UUID userId, CredentialDto cred);

    default void resetPasswordForUser(UUID userId, CredentialDto cred) {
        this.resetPasswordForUser(this.getDefaultRealm(), userId, cred);
    }
    
    default void resetPasswordForUser(String realm, UUID userId, String password, boolean temporary) {
        this.resetPasswordForUser(realm, userId, CredentialDto.ofPassword(password, temporary));
    }
    
    default void resetPasswordForUser(UUID userId, String password, boolean temporary) {
        this.resetPasswordForUser(this.getDefaultRealm(), userId, CredentialDto.ofPassword(password, temporary));
    }
    
    void executeEmailActionsForUser(String realm, UUID userId, Set<EnumRequiredAction> actions);

    default void executeEmailActionsForUser(UUID userId, Set<EnumRequiredAction> actions) {
        this.executeEmailActionsForUser(this.getDefaultRealm(), userId, actions);
    }
    
    List<GroupDto> findGroups(String realm, @Nullable GroupQueryDto query);

    default List<GroupDto> findGroups(@Nullable GroupQueryDto query) {
        return this.findGroups(this.getDefaultRealm(), query);
    }
    
    Optional<GroupDto> getGroup(String realm, UUID groupId);
    
    default Optional<GroupDto> getGroup(UUID groupId) {
        return this.getGroup(this.getDefaultRealm(), groupId);
    }
    
    UUID createGroup(String realm, String groupName);
    
    default UUID createGroup(String groupName) {
        return this.createGroup(this.getDefaultRealm(), groupName);
    }
    
    void updateGroup(String realm, GroupDto group);

    default void updateGroup(GroupDto group) {
        this.updateGroup(this.getDefaultRealm(), group);
    }
    
    List<UserDto> getGroupMembers(String realm, UUID groupId, PageRequest pageRequest);

    default List<UserDto> getGroupMembers(UUID groupId, PageRequest pageRequest) {
        return this.getGroupMembers(this.getDefaultRealm(), groupId, pageRequest);
    }
    
    List<GroupDto> getUserGroups(String realm, UUID userId);

    default List<GroupDto> getUserGroups(UUID userId) {
        return this.getUserGroups(this.getDefaultRealm(), userId);
    }
    
    void deleteGroup(String realm, UUID groupId);

    default void deleteGroup(UUID groupId) {
        this.deleteGroup(this.getDefaultRealm(), groupId);
    }

    List<RoleDto> findRoles(String realm, @Nullable RoleQueryDto query);

    default List<RoleDto> findRoles(@Nullable RoleQueryDto query) {
        return this.findRoles(this.getDefaultRealm(), query);
    }

    List<UserDto> findUsersInRole(String realm, String roleName, @Nullable PageRequest pageRequest);

    default List<UserDto> findUsersInRole(String roleName, @Nullable PageRequest pageRequest) {
        return this.findUsersInRole(this.getDefaultRealm(), roleName, pageRequest);
    }
    
    RoleDto getRoleByName(String realm, String roleName);

    default RoleDto getRoleByName(String roleName) {
        return this.getRoleByName(this.getDefaultRealm(), roleName);
    }
    
    List<RoleDto> getRolesForUser(String realm, UUID userId);
    
    default List<RoleDto> getRolesForUser(UUID userId) {
        return this.getRolesForUser(this.getDefaultRealm(), userId);
    }

    void addRoleForUser(String realm, UUID userId, String roleName);

    default void addRoleForUser(UUID userId, String roleName) {
        this.addRoleForUser(this.getDefaultRealm(), userId, roleName);
    }
    
    void removeRoleForUser(String realm, UUID userId, String roleName);

    default void removeRoleForUser(UUID userId, String roleName) {
        this.removeRoleForUser(getDefaultRealm(), userId, roleName);
    }
    
    List<ClientDto> findClients(String realm, @Nullable ClientQueryDto query);

    default List<ClientDto> findClients(@Nullable ClientQueryDto query) {
        return this.findClients(this.getDefaultRealm(), query);
    }
    
    Optional<ClientDto> getClient(String realm, UUID clientUuid);

    default Optional<ClientDto> getClient(UUID clientUuid) {
        return this.getClient(this.getDefaultRealm(), clientUuid);
    }
    
    default Optional<ClientDto> getClientById(String realm, String clientId) {
        final List<ClientDto> results = this.findClients(realm, ClientQueryDto.forClientId(clientId));
        Assert.state(results.size() <= 1, "did not expect more than 1 result: clientId is unique!");
        return results.isEmpty()? Optional.empty() : Optional.of(results.get(0));
    }
    
    default Optional<ClientDto> getClientById(String clientId) {
        return this.getClientById(this.getDefaultRealm(), clientId);
    }
    
    /**
     * Get the service-account user created for given client
     * 
     * @param clientUuid The client UUID (not the clientId!)
     * @return a user representation
     */
    Optional<UserDto> getServiceAccountUser(String realm, UUID clientUuid);

    default Optional<UserDto> getServiceAccountUser(UUID clientUuid) {
        return this.getServiceAccountUser(this.getDefaultRealm(), clientUuid);
    }
    
    Optional<CredentialDto> regenerateClientSecret(String realm, UUID clientUuid);

    default Optional<CredentialDto> regenerateClientSecret(UUID clientUuid) {
        return this.regenerateClientSecret(this.getDefaultRealm(), clientUuid);
    }
    
    Optional<CredentialDto> getClientSecret(String realm, UUID clientUuid);

    default Optional<CredentialDto> getClientSecret(UUID clientUuid) {
        return this.getClientSecret(this.getDefaultRealm(), clientUuid);
    }
    
    UUID createClient(String realm, ClientDto client);

    default UUID createClient(ClientDto client) {
        return this.createClient(this.getDefaultRealm(), client);
    }
    
    void updateClient(String realm, ClientDto client);

    default void updateClient(ClientDto client) {
        this.updateClient(this.getDefaultRealm(), client);
    }
    
    void deleteClient(String realm, UUID clientUuid);

    default void deleteClient(UUID clientUuid) {
        this.deleteClient(this.getDefaultRealm(), clientUuid);
    }
    
    List<RoleDto> getClientRoles(String realm, UUID clientUuid);

    default List<RoleDto> getClientRoles(UUID clientUuid) {
        return this.getClientRoles(this.getDefaultRealm(), clientUuid);
    }
    
    List<UserDto> findUsersInClientRole(String realm, UUID clientUuid, String roleName, @Nullable PageRequest pageRequest);

    default List<UserDto> findUsersInClientRole(UUID clientUuid, String roleName, @Nullable PageRequest pageRequest) {
        return this.findUsersInClientRole(this.getDefaultRealm(), clientUuid, roleName, pageRequest);
    }
    
    List<RoleDto> getClientRolesForUser(String realm, UUID clientUuid, UUID userId);

    default List<RoleDto> getClientRolesForUser(UUID clientUuid, UUID userId) {
        return this.getClientRolesForUser(this.getDefaultRealm(), clientUuid, userId);
    }
    
    Optional<RoleDto> getClientRoleByName(String realm, UUID clientUuid, String roleName);

    default Optional<RoleDto> getClientRoleByName(UUID clientUuid, String roleName) {
        return this.getClientRoleByName(this.getDefaultRealm(), clientUuid, roleName);
    }
    
    void createClientRole(String realm, UUID clientUuid, String roleName);

    default void createClientRole(UUID clientUuid, String roleName) {
        this.createClientRole(this.getDefaultRealm(), clientUuid, roleName);
    }
    
    void deleteClientRole(String realm, UUID clientUuid, String roleName);

    default void deleteClientRole(UUID clientUuid, String roleName) {
        this.deleteClientRole(this.getDefaultRealm(), clientUuid, roleName);
    }
    
    void addClientRoleForUser(String realm, UUID clientUuid, UUID userId, String roleName);

    default void addClientRoleForUser(UUID clientUuid, UUID userId, String roleName) {
        this.addClientRoleForUser(this.getDefaultRealm(), clientUuid, userId, roleName);
    }
    
    void removeClientRoleForUser(String realm, UUID clientUuid, UUID userId, String roleName);
    
    default void removeClientRoleForUser(UUID clientUuid, UUID userId, String roleName) {
        this.removeClientRoleForUser(this.getDefaultRealm(), clientUuid, userId, roleName);
    }
}