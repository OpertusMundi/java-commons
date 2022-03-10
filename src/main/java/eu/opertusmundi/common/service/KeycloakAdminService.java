package eu.opertusmundi.common.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.lang.Nullable;

import eu.opertusmundi.common.model.keycloak.server.CredentialDto;
import eu.opertusmundi.common.model.keycloak.server.GroupDto;
import eu.opertusmundi.common.model.keycloak.server.GroupQueryDto;
import eu.opertusmundi.common.model.keycloak.server.PageRequest;
import eu.opertusmundi.common.model.keycloak.server.UserDto;
import eu.opertusmundi.common.model.keycloak.server.UserQueryDto;

public interface KeycloakAdminService {

    List<UserDto> findUsers(@Nullable UserQueryDto query);

    int countUsers(@Nullable UserQueryDto query);

    Optional<UserDto> getUser(UUID userId);

    UUID createUser(UserDto user);

    void updateUser(UserDto user);

    void deleteUser(UUID userId);

    void resetPasswordForUser(UUID userId, CredentialDto cred);

    void resetPasswordForUser(UUID userId, String password, boolean temporary);

    List<GroupDto> findGroups(@Nullable GroupQueryDto query);

    Optional<GroupDto> getGroup(UUID groupId);

    UUID createGroup(String groupName);

    void updateGroup(GroupDto group);

    List<UserDto> getGroupMembers(UUID groupId, @Nullable PageRequest pageRequest);

    List<GroupDto> getUserGroups(UUID userId);

    void deleteGroup(UUID groupId);

}