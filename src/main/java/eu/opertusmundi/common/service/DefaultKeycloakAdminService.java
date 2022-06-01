package eu.opertusmundi.common.service;

import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.PostConstruct;

import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.support.ResponseEntityDecoder;
import org.springframework.cloud.openfeign.support.SpringDecoder;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.cloud.openfeign.support.SpringMvcContract;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.PathContainer;
import org.springframework.lang.Nullable;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPattern.PathMatchInfo;
import org.springframework.web.util.pattern.PathPatternParser;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.opertusmundi.common.feign.client.KeycloakAdminFeignClient;
import eu.opertusmundi.common.feign.client.KeycloakRefreshTokenFeignClient;
import eu.opertusmundi.common.model.keycloak.KeycloakClientException;
import eu.opertusmundi.common.model.keycloak.server.ClientDto;
import eu.opertusmundi.common.model.keycloak.server.ClientQueryDto;
import eu.opertusmundi.common.model.keycloak.server.CredentialDto;
import eu.opertusmundi.common.model.keycloak.server.EnumRequiredAction;
import eu.opertusmundi.common.model.keycloak.server.GroupDto;
import eu.opertusmundi.common.model.keycloak.server.GroupQueryDto;
import eu.opertusmundi.common.model.keycloak.server.PageRequest;
import eu.opertusmundi.common.model.keycloak.server.RefreshTokenForm;
import eu.opertusmundi.common.model.keycloak.server.RefreshTokenResponse;
import eu.opertusmundi.common.model.keycloak.server.RoleDto;
import eu.opertusmundi.common.model.keycloak.server.RoleQueryDto;
import eu.opertusmundi.common.model.keycloak.server.UserDto;
import eu.opertusmundi.common.model.keycloak.server.UserQueryDto;
import feign.Feign;
import feign.FeignException;
import feign.FeignException.FeignClientException;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import feign.httpclient.ApacheHttpClient;


@Service
@ConditionalOnProperty("opertusmundi.feign.keycloak.url")
public class DefaultKeycloakAdminService implements KeycloakAdminService
{
    private static final Logger logger = LoggerFactory.getLogger(DefaultKeycloakAdminService.class);
    
    @lombok.ToString
    @lombok.Getter
    private static class Token
    {
        private final String value;
        
        private final Instant expiresAt;
        
        private Token(String value, Instant expiresAt)
        {
            this.value = value;
            this.expiresAt = expiresAt;
        }
    }
    
    private static final String CLIENT_ID = "admin-cli";
 
    private static final UserQueryDto DEFAULT_USER_QUERY = new UserQueryDto();
    
    private static final GroupQueryDto DEFAULT_GROUP_QUERY = new GroupQueryDto();
    
    private static final ClientQueryDto DEFAULT_CLIENT_QUERY = new ClientQueryDto();
    
    private static final RoleQueryDto DEFAULT_ROLE_QUERY = new RoleQueryDto();
    
    private static final PageRequest DEFAULT_PAGE_REQUEST = new PageRequest();
    
    /**
     * The base URL of the Keycloak server
     */
    @Value("${opertusmundi.feign.keycloak.url}")
    private URL url;
    
    /**
     * The realm name
     */
    @Value("${opertusmundi.feign.keycloak.realm:master}")
    private String realm;
        
    /**
     * The refresh token which allows us to refresh access tokens. It must include an {@code offline_access}
     * scope.
     */
    private Token refreshToken;

    @Autowired
    private void setRefreshToken(
        @Value("${opertusmundi.feign.keycloak.admin.refresh-token.refresh-token}") String refreshTokenValue)
    {
        this.refreshToken = new Token(refreshTokenValue, null);
    }
    
    
    private RetryTemplate refreshTokenRetryTemplate;
    
    @Autowired
    private void setRefreshTokenRetryTemplate(
        @Value("${opertusmundi.feign.keycloak.admin.refresh-token.retry.backoff.initial-interval-millis:2000}") Long backoffInitialIntervalMillis,
        @Value("${opertusmundi.feign.keycloak.admin.refresh-token.retry.backoff.multiplier:2.0}") Double backoffMultiplier,
        @Value("${opertusmundi.feign.keycloak.admin.refresh-token.retry.backoff.max-interval-millis:360000}") Long backoffMaxIntervalMillis,
        @Value("${opertusmundi.feign.keycloak.admin.refresh-token.retry.backoff.max:5}") Integer maxNumOfAttempts)
    {
        final ExponentialBackOffPolicy backoffPolicy = new ExponentialBackOffPolicy();
        backoffPolicy.setInitialInterval(backoffInitialIntervalMillis);
        backoffPolicy.setMultiplier(backoffMultiplier);
        backoffPolicy.setMaxInterval(backoffMaxIntervalMillis);

        final RetryTemplate retryTemplate = new RetryTemplate();
        retryTemplate.setBackOffPolicy(backoffPolicy);
        retryTemplate.setRetryPolicy(new SimpleRetryPolicy(maxNumOfAttempts));

        this.refreshTokenRetryTemplate = retryTemplate;
    }
    
    /**
     * The interval on which we must refresh the access token (expressed as a fraction of the
     * access token lifespan).
     */
    private final float refreshTokenIntervalAsFraction = 0.7f;
    
    private AtomicReference<Token> accessTokenRef = new AtomicReference<>();
    
    @Autowired
    private TaskScheduler taskScheduler;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private KeycloakAdminFeignClient keycloakAdminClient;
    
    private KeycloakRefreshTokenFeignClient keycloakRefreshTokenClient;
    
    private final PathPatternParser pathPatternParser = new PathPatternParser();
    
    private PathPattern userPathPattern; 
    
    private void initUserPathPattern() 
        throws NoSuchMethodException, SecurityException
    {
        final Method method = KeycloakAdminFeignClient.class
            .getMethod("getUser", String.class, UUID.class);
        this.userPathPattern = pathPatternParser.parse(method.getAnnotation(GetMapping.class).path()[0]);
    }
    
    private PathPattern groupPathPattern;
    
    private void initGroupPathPattern() 
        throws NoSuchMethodException, SecurityException
    {
        final Method method = KeycloakAdminFeignClient.class
            .getMethod("getGroup", String.class, UUID.class);
        this.groupPathPattern = pathPatternParser.parse(method.getAnnotation(GetMapping.class).path()[0]);
    }
    
    private PathPattern clientPathPattern;
    
    private void initClientPathPattern() 
        throws NoSuchMethodException, SecurityException
    {
        final Method method = KeycloakAdminFeignClient.class
            .getMethod("getClient", String.class, UUID.class);
        this.clientPathPattern = pathPatternParser.parse(method.getAnnotation(GetMapping.class).path()[0]);
    }
    
    private PathMatchInfo matchAndExtractFromLocation(PathPattern pathPattern, String location)
    {
        String locationPath = null;
        try {
            locationPath  = (new URL(location)).getPath();
        } catch (MalformedURLException e) {
            throw new IllegalStateException(e);
        }
        
        Assert.state(locationPath.startsWith(url.getPath()), "The location path is outside base path");
        
        final String path = locationPath.substring(url.getPath().length()).replaceFirst("^/", "");
        return pathPattern.matchAndExtract(PathContainer.parsePath(path));
    }
    
    @Autowired
    private void setupFeignClients(
        @Value("${opertusmundi.feign.keycloak.url}") URL url,
        ObjectFactory<HttpMessageConverters> messageConverters, 
        CloseableHttpClient httpClient)
    {
        final feign.Contract contract = new SpringMvcContract();
        final feign.codec.Encoder encoder = new SpringEncoder(messageConverters);
        final feign.codec.Decoder decoder = new ResponseEntityDecoder(new SpringDecoder(messageConverters));
        final feign.Client client = new ApacheHttpClient(httpClient);
        final feign.Logger logger = new feign.Logger.ErrorLogger();
        
        this.keycloakRefreshTokenClient = Feign.builder()
            .contract(contract)
            .encoder(encoder)
            .decoder(decoder)
            .client(client)
            .logger(logger)
            .logLevel(feign.Logger.Level.BASIC)
            .target(KeycloakRefreshTokenFeignClient.class, url.toString());
        
        this.keycloakAdminClient = Feign.builder()
            .contract(contract)
            .encoder(encoder)
            .decoder(decoder)
            .client(client)
            .requestInterceptor(new RequestInterceptor() {
                @Override
                public void apply(RequestTemplate requestTemplate)
                {
                    requestTemplate.header(HttpHeaders.AUTHORIZATION, authorizationHeader());
                }
            })
            .logger(logger)
            .logLevel(feign.Logger.Level.BASIC)
            .target(KeycloakAdminFeignClient.class, url.toString());
    }
    
    private int refreshAccessToken()
    {
        final RefreshTokenForm refreshTokenForm = RefreshTokenForm.of(CLIENT_ID, refreshToken.value);
        final RefreshTokenResponse refreshTokenResponse = keycloakRefreshTokenClient.refreshToken(refreshTokenForm);
        
        final String accessToken = refreshTokenResponse.getAccessToken();
        final int expiresInSeconds = refreshTokenResponse.getExpiresIn();
        this.accessTokenRef.set(new Token(accessToken, Instant.now().plusSeconds(expiresInSeconds)));
        
        return expiresInSeconds;
    }
    
    private void refreshAccessTokenAndScheduleNextRefresh()
    {
        final int expiresInSeconds = refreshTokenRetryTemplate.execute(context -> {
            if (logger.isDebugEnabled()) {
                logger.info("Trying to refresh token for {} [context={}]", url, context);
            }
            return refreshAccessToken();
        });

        if (logger.isDebugEnabled()) {
            final Token accessToken = accessTokenRef.get();
            logger.debug("Acquired access token for {}: {} ", url, accessToken.value);
        }

        taskScheduler.schedule(this::refreshAccessTokenAndScheduleNextRefresh,
            Instant.now().plusSeconds((long) (expiresInSeconds * refreshTokenIntervalAsFraction)));
    }
        
    @PostConstruct
    private void setup() throws NoSuchMethodException, SecurityException
    {
        Assert.state(url != null, "url must be non null");
        Assert.state(StringUtils.hasText(realm), "realm must be non empty");
        Assert.state(StringUtils.hasText(refreshToken.value), "refreshToken must be non empty");
        Assert.state(keycloakRefreshTokenClient != null, "keycloakRefreshTokenClient should be initialized");
        Assert.state(keycloakAdminClient != null, "keycloakAdminClient should be initialized");
        
        this.initUserPathPattern();
        this.initGroupPathPattern();
        this.initClientPathPattern();
        
        this.refreshAccessTokenAndScheduleNextRefresh();
    }
    
    private String authorizationHeader()
    {
        final Token accessToken = accessTokenRef.get();
        return String.format("Bearer %s", accessToken.value);
    }
    
    private KeycloakClientException translateException(String message, FeignClientException ex)
    {
        final String content = ex.contentUTF8();
        String detailsMessage = null;
        
        if (!content.isEmpty()) {
            try {
                Map<?, ?> contentAsMap = objectMapper.readValue(content, Map.class);
                detailsMessage = (String) contentAsMap.get("errorMessage");
            } catch (JsonProcessingException ex1) { /* no-op */ }
        }
        if (StringUtils.hasText(detailsMessage)) {
            message = message + ": " + detailsMessage;
        }
        return new KeycloakClientException(message, ex);
    }
    
    @Override
    public String getDefaultRealm()
    {
        return realm;
    }
    
    //
    //  Admin API
    //
    
    @Override
    public List<UserDto> findUsers(String realm, @Nullable UserQueryDto query)
    {
        Assert.hasText(realm, "realm must not be empty");
        
        final ResponseEntity<List<UserDto>> response = 
            keycloakAdminClient.findUsers(realm, query != null? query : DEFAULT_USER_QUERY);
        return response.getBody(); 
    }
        
    @Override
    public int countUsers(String realm, @Nullable UserQueryDto query)
    {
        Assert.hasText(realm, "realm must not be empty");
        
        final ResponseEntity<Integer> response = 
            keycloakAdminClient.countUsers(realm, query != null? query : DEFAULT_USER_QUERY);
        return response.getBody();
    }
    
    @Override
    public Optional<UserDto> getUser(String realm, UUID userId)
    {
        Assert.hasText(realm, "realm must not be empty");
        Assert.notNull(userId, "userId must not be null");
        
        ResponseEntity<UserDto> response = null;
        try {
            response = keycloakAdminClient.getUser(realm, userId);
        } catch (FeignException.NotFound ex) { 
            response = null;
        }
        return Optional.ofNullable(response).map(ResponseEntity::getBody);
    }
    
    @Override
    public UUID createUser(String realm, UserDto user)
    {
        Assert.hasText(realm, "realm must not be empty");
        Assert.notNull(user, "user object must be non null");
        Assert.isNull(user.getId(), "id must be null");
        Assert.hasText(user.getUsername(), "username must not be empty");
        Assert.hasText(user.getEmail(), "email must not be empty");
        
        ResponseEntity<Void> response = null;
        try {
            response = keycloakAdminClient.createUser(realm, user);
        } catch (FeignException.BadRequest ex) {
            throw translateException("user representation is invalid", ex);
        } catch (FeignException.Conflict ex) {
            throw translateException("user conflicts with another user", ex);
        }
        
        Assert.state(response.getStatusCode() == HttpStatus.CREATED, "Expected an HTTP status of CREATED");
        final String location = response.getHeaders().get(HttpHeaders.LOCATION).get(0);
        final PathMatchInfo pathMatchInfo = matchAndExtractFromLocation(userPathPattern, location);
        Assert.state(pathMatchInfo != null, "The location path does not match with path of a user resource");
        final String userId = pathMatchInfo.getUriVariables().get("id");
        
        return UUID.fromString(userId);
    }
    
    @Override
    public void updateUser(String realm, UserDto user)
    {
        Assert.hasText(realm, "realm must not be empty");
        Assert.notNull(user, "user object must be non null");
        
        final UUID userId = user.getId();
        Assert.notNull(userId, "user.id must be non null");
        Assert.isNull(user.getUsername(), "username must be null (cannot be updated)");
        
        try {
            keycloakAdminClient.updateUser(realm, userId, user);
        } catch (FeignException.NotFound ex) { 
            throw translateException("user not found", ex);
        } catch (FeignException.BadRequest ex) {
            throw translateException("user representation is invalid", ex);
        } catch (FeignException.Conflict ex) {
            throw translateException("user conflicts with another user", ex);
        }
    }
    
    @Override
    public void deleteUser(String realm, UUID userId)
    {
        Assert.hasText(realm, "realm must not be empty");
        Assert.notNull(userId, "userId must not be null");
        
        try {
            keycloakAdminClient.deleteUser(realm, userId);
        } catch (FeignException.NotFound ex) {
            throw translateException("user not found", ex);
        }
    }
    
    @Override
    public void resetPasswordForUser(String realm, UUID userId, CredentialDto cred)
    {
        Assert.hasText(realm, "realm must not be empty");
        Assert.notNull(userId, "userId must not be null");
        Assert.notNull(cred, "credential must not be null");
        Assert.hasText(cred.getValue(), "credential.value must not be empty");
        
        try {
            keycloakAdminClient.resetUserPassword(realm, userId, cred);
        } catch (FeignException.NotFound ex) {
            throw translateException("user not found", ex);
        } catch (FeignException.BadRequest ex) {
            // this is probably due to the password policy enforced by the realm 
            throw translateException("password request is rejected", ex);
        }
    }
    
    @Override
    public void executeEmailActionsForUser(String realm, UUID userId, Set<EnumRequiredAction> actions)
    {
        Assert.hasText(realm, "realm must not be empty");
        Assert.notNull(userId, "userId must not be null");
        Assert.notEmpty(actions, "actions may not be empty");
        
        try {
            keycloakAdminClient.executeEmailActionsForUser(realm, userId, actions);
        } catch (FeignException.BadRequest ex) {
            throw translateException("email actions did not execute", ex);
        }
    }
    
    @Override
    public List<GroupDto> findGroups(String realm, @Nullable GroupQueryDto query)
    {
        Assert.hasText(realm, "realm must not be empty");
        
        final ResponseEntity<List<GroupDto>> response = 
            keycloakAdminClient.findGroups(realm, query != null? query : DEFAULT_GROUP_QUERY);
        return response.getBody();
    }
    
    @Override
    public Optional<GroupDto> getGroup(String realm, UUID groupId)
    {
        Assert.hasText(realm, "realm must not be empty");
        Assert.notNull(groupId, "groupId must not be null");
        
        ResponseEntity<GroupDto> response = null;
        try {
            response = keycloakAdminClient.getGroup(realm, groupId);
        } catch (FeignException.NotFound ex) { 
            response = null;
        }
        return Optional.ofNullable(response).map(ResponseEntity::getBody);
    }
    
    @Override
    public UUID createGroup(String realm, String groupName)
    {
        Assert.hasText(realm, "realm must not be empty");
        Assert.hasText(groupName, "groupName must not be emptyl");
        
        final GroupDto group = new GroupDto();
        group.setName(groupName);
        
        ResponseEntity<Void> response = null;
        try {
            response = keycloakAdminClient.createGroup(realm, group);
        } catch (FeignException.BadRequest ex) {
            throw translateException("group representation is invalid", ex);
        } catch (FeignException.Conflict ex) {
            throw translateException("group conflicts with another group", ex);
        }
        
        Assert.state(response.getStatusCode() == HttpStatus.CREATED, "Expected an HTTP status of CREATED");
        final String location = response.getHeaders().get(HttpHeaders.LOCATION).get(0);
        final PathMatchInfo pathMatchInfo = matchAndExtractFromLocation(groupPathPattern, location);
        Assert.state(pathMatchInfo != null, "The location path does not match with path of a group resource");
        final String groupId = pathMatchInfo.getUriVariables().get("groupId");
        
        return UUID.fromString(groupId);
    }
    
    @Override
    public void updateGroup(String realm, GroupDto group)
    {
        Assert.hasText(realm, "realm must not be empty");
        Assert.notNull(group, "group object must be non null");
        
        final UUID groupId = group.getId();
        Assert.notNull(groupId, "group.id must be non null");
        
        try {
            keycloakAdminClient.updateGroup(realm, groupId, group);
        } catch (FeignException.NotFound ex) {
            throw translateException("group not found", ex);
        } catch (FeignException.BadRequest ex) {
            throw translateException("group representation is invalid", ex);
        } catch (FeignException.Conflict ex) {
            throw translateException("group conflicts with another group", ex);
        }
    }
    
    @Override
    public List<UserDto> getGroupMembers(String realm, UUID groupId, @Nullable PageRequest pageRequest)
    {
        Assert.hasText(realm, "realm must not be empty");
        Assert.notNull(groupId, "groupId must not be null");
        
        ResponseEntity<List<UserDto>> response = null;
        try {
            response = keycloakAdminClient.getGroupMembers(realm, groupId, 
                pageRequest != null? pageRequest : DEFAULT_PAGE_REQUEST);
        } catch (FeignException.NotFound ex) {
            throw translateException("group not found", ex);
        }
        
        return response.getBody();
    }
    
    @Override
    public List<GroupDto> getUserGroups(String realm, UUID userId)
    {
        Assert.hasText(realm, "realm must not be empty");
        Assert.notNull(userId, "userId must not be null");
        
        ResponseEntity<List<GroupDto>> response = null;
        try {
            response = keycloakAdminClient.getUserGroups(realm, userId);
        } catch (FeignException.NotFound ex) {
            throw translateException("user not found", ex);
        }
        
        return response.getBody();
    }
    
    @Override
    public void deleteGroup(String realm, UUID groupId)
    {
        Assert.hasText(realm, "realm must not be empty");
        Assert.notNull(groupId, "groupId must not be null");
        
        try {
            keycloakAdminClient.deleteGroup(realm, groupId);
        } catch (FeignException.NotFound ex) {
            throw translateException("group not found", ex);
        }
    }
    
    @Override
    public List<RoleDto> findRoles(String realm, @Nullable RoleQueryDto query)
    {
        Assert.hasText(realm, "realm must not be empty");
        
        final ResponseEntity<List<RoleDto>> response = 
            keycloakAdminClient.findRoles(realm, query != null? query : DEFAULT_ROLE_QUERY);
        return response.getBody(); 
    }
    
    @Override
    public List<UserDto> findUsersInRole(String realm, String roleName, @Nullable PageRequest pageRequest)
    {
        Assert.hasText(realm, "realm must not be empty");
        
        final ResponseEntity<List<UserDto>> response = 
            keycloakAdminClient.findUsersInRole(realm, roleName,
                pageRequest == null? DEFAULT_PAGE_REQUEST : pageRequest);
        return response.getBody();
    }
    
    @Override
    public RoleDto getRoleByName(String realm, String roleName)
    {
        Assert.hasText(realm, "realm must not be empty");
        
        final ResponseEntity<RoleDto> response = keycloakAdminClient.getRoleByName(realm, roleName);
        return response.getBody();
    }
    
    @Override
    public List<RoleDto> getRolesForUser(String realm, UUID userId)
    {
        Assert.hasText(realm, "realm must not be empty");
        
        final ResponseEntity<List<RoleDto>> response = 
            keycloakAdminClient.getRolesForUser(realm, userId);
        return response.getBody();
    }
    
    @Override
    public void addRoleForUser(String realm, UUID userId, String roleName)
    {
        Assert.hasText(realm, "realm must not be empty");
        Assert.notNull(userId, "userId must not be null");
        Assert.hasText(roleName, "roleName must not be empty");
        
        final RoleDto role = this.getRoleByName(realm, roleName);    
        keycloakAdminClient.addRolesForUser(realm, userId, Collections.singletonList(role));
    }
    
    @Override
    public void removeRoleForUser(String realm, UUID userId, String roleName)
    {
        Assert.hasText(realm, "realm must not be empty");
        Assert.notNull(userId, "userId must not be null");
        Assert.hasText(roleName, "roleName must not be empty");
        
        final RoleDto role = this.getRoleByName(realm, roleName);
        keycloakAdminClient.removeRolesForUser(realm, userId, Collections.singletonList(role));
    }
    
    @Override
    public List<ClientDto> findClients(String realm, @Nullable ClientQueryDto query)
    {
        Assert.hasText(realm, "realm must not be empty");
        
        final ResponseEntity<List<ClientDto>> response = 
            keycloakAdminClient.findClients(realm, query != null? query : DEFAULT_CLIENT_QUERY);
        return response.getBody(); 
    }
    
    @Override
    public Optional<ClientDto> getClient(String realm, UUID clientUuid)
    {
        Assert.hasText(realm, "realm must not be empty");
        Assert.notNull(clientUuid, "id must not be null");
        
        ResponseEntity<ClientDto> response = null;
        try {
            response = keycloakAdminClient.getClient(realm, clientUuid);
        } catch (FeignException.NotFound ex) { 
            response = null;
        }
        return Optional.ofNullable(response).map(ResponseEntity::getBody);
    }
    
    /**
     * Get the service-account user created for given client
     * 
     * @param clientUuid The client UUID (not the clientId!)
     * @return a user representation
     */
    @Override
    public Optional<UserDto> getServiceAccountUser(String realm, UUID clientUuid)
    {
        Assert.hasText(realm, "realm must not be empty");
        Assert.notNull(clientUuid, "id must not be null");
        
        ResponseEntity<UserDto> response = null;
        try {
            response = keycloakAdminClient.getServiceAccountUser(realm, clientUuid);
        } catch (FeignException.NotFound ex) { 
            response = null;
        }
        return Optional.ofNullable(response).map(ResponseEntity::getBody);
    }
    
    @Override
    public Optional<CredentialDto> regenerateClientSecret(String realm, UUID clientUuid)
    {
        Assert.hasText(realm, "realm must not be empty");
        Assert.notNull(clientUuid, "id must not be null");
        
        ResponseEntity<CredentialDto> response = null;
        try {
            response = keycloakAdminClient.regenerateClientSecret(realm, clientUuid);
        } catch (FeignException.NotFound ex) {
            response = null;
        }
        
        return Optional.ofNullable(response).map(ResponseEntity::getBody);
    }
    
    @Override
    public Optional<CredentialDto> getClientSecret(String realm, UUID clientUuid)
    {
        Assert.hasText(realm, "realm must not be empty");
        Assert.notNull(clientUuid, "id must not be null");
        
        ResponseEntity<CredentialDto> response = null;
        try {
            response = keycloakAdminClient.getClientSecret(realm, clientUuid);
        } catch (FeignException.NotFound ex) {
            response = null;
        }
        
        return Optional.ofNullable(response).map(ResponseEntity::getBody);
    }
    
    @Override
    public UUID createClient(String realm, ClientDto client)
    {
        Assert.hasText(realm, "realm must not be empty");
        Assert.notNull(client, "client object must be non null");
        Assert.isNull(client.getId(), "id must be null");
        Assert.hasText(client.getClientId(), "client.id must be empty");
        
        ResponseEntity<Void> response = null;
        try {
            response = keycloakAdminClient.createClient(realm, client);
        } catch (FeignException.BadRequest ex) {
            throw translateException("client representation is invalid", ex);
        } catch (FeignException.Conflict ex) {
            throw translateException("client conflicts with another client", ex);
        }
        
        Assert.state(response.getStatusCode() == HttpStatus.CREATED, "Expected an HTTP status of CREATED");
        final String location = response.getHeaders().get(HttpHeaders.LOCATION).get(0);
        final PathMatchInfo pathMatchInfo = matchAndExtractFromLocation(clientPathPattern, location);
        Assert.state(pathMatchInfo != null, "The location path does not match with path of a client resource");
        final String id = pathMatchInfo.getUriVariables().get("id");
        
        return UUID.fromString(id);
    }
    
    @Override
    public void updateClient(String realm, ClientDto client)
    {
        Assert.hasText(realm, "realm must not be empty");
        Assert.notNull(client, "client object must be non null");
        
        final UUID clientUuid = client.getId();
        Assert.notNull(clientUuid, "client.id must be non null");
        
        try {
            keycloakAdminClient.updateClient(realm, clientUuid, client);
        } catch (FeignException.NotFound ex) { 
            throw translateException("client not found", ex);
        } catch (FeignException.BadRequest ex) {
            throw translateException("client representation is invalid", ex);
        } catch (FeignException.Conflict ex) {
            throw translateException("client conflicts with another client", ex);
        }
    }
    
    @Override
    public void deleteClient(String realm, UUID clientUuid)
    {
        Assert.hasText(realm, "realm must not be empty");
        Assert.notNull(clientUuid, "id must not be null");
        
        try {
            keycloakAdminClient.deleteClient(realm, clientUuid);
        } catch (FeignException.NotFound ex) {
            throw translateException("client not found", ex);
        }
    }
    
    @Override
    public List<RoleDto> getClientRoles(String realm, UUID clientUuid)
    {
        Assert.hasText(realm, "realm must not be empty");
        Assert.notNull(clientUuid, "clientUuid must be non null");
        
        ResponseEntity<List<RoleDto>> response =
            keycloakAdminClient.getClientRoles(realm, clientUuid);
        return response.getBody();
    }
    
    @Override
    public List<UserDto> findUsersInClientRole(
        String realm, UUID clientUuid, String roleName, @Nullable PageRequest pageRequest)
    {
        Assert.hasText(realm, "realm must not be empty");
        Assert.notNull(clientUuid, "id must not be null");
        Assert.hasText(roleName, "roleName must not be empty");
        
        ResponseEntity<List<UserDto>> response =
            keycloakAdminClient.findUsersInClientRole(realm, clientUuid, roleName, 
                pageRequest != null? pageRequest : DEFAULT_PAGE_REQUEST);
        return response.getBody();
    }
    
    @Override
    public List<RoleDto> getClientRolesForUser(String realm, UUID clientUuid, UUID userId)
    {
        Assert.hasText(realm, "realm must not be empty");
        Assert.notNull(clientUuid, "clientUuid must be non null");
        Assert.notNull(userId, "userId must be non null");
        
        ResponseEntity<List<RoleDto>> response =
            keycloakAdminClient.getClientRolesForUser(realm, userId, clientUuid);
        return response.getBody();
    }
    
    @Override
    public Optional<RoleDto> getClientRoleByName(String realm, UUID clientUuid, String roleName)
    {
        Assert.hasText(realm, "realm must not be empty");
        Assert.notNull(clientUuid, "clientUuid must be non null");
        Assert.hasText(roleName, "roleName must not be empty");
        
        ResponseEntity<RoleDto> response = null;
        try {
            response = keycloakAdminClient.getClientRoleByName(realm, clientUuid, roleName);
        } catch (FeignException.NotFound ex) {
            response = null;
        }
        
        return Optional.ofNullable(response).map(ResponseEntity::getBody);
    }
    
    @Override
    public void createClientRole(String realm, UUID clientUuid, String roleName)
    {
        Assert.hasText(realm, "realm must not be empty");
        Assert.notNull(clientUuid, "clientUuid must be non null");
        Assert.hasText(roleName, "roleName must not be empty");
        
        RoleDto role = new RoleDto(null, roleName);
        try {
            keycloakAdminClient.createClientRole(realm, clientUuid, role);
        } catch (FeignException.Conflict ex) {
            throw translateException("role already exists", ex);
        }
    }
    
    @Override
    public void deleteClientRole(String realm, UUID clientUuid, String roleName)
    {
        Assert.hasText(realm, "realm must not be empty");
        Assert.notNull(clientUuid, "clientUuid must be non null");
        Assert.hasText(roleName, "roleName must not be empty");
        
        try {
            keycloakAdminClient.deleteClientRole(realm, clientUuid, roleName);
        } catch (FeignException.NotFound ex) {
            throw translateException("client or role do not exist", ex);
        }
    }
    
    @Override
    public void addClientRoleForUser(String realm, UUID clientUuid, UUID userId, String roleName)
    {
        Assert.hasText(realm, "realm must not be empty");
        Assert.notNull(clientUuid, "clientUuid must be non null");
        Assert.notNull(userId, "userId must be non null");
        Assert.hasText(roleName, "roleName must not be empty");
        
        RoleDto role = this.getClientRoleByName(realm, clientUuid, roleName).get();
        keycloakAdminClient.addClientRolesForUser(realm, userId, clientUuid, Collections.singletonList(role));
    }
    
    @Override
    public void removeClientRoleForUser(String realm, UUID clientUuid, UUID userId, String roleName)
    {
        Assert.hasText(realm, "realm must not be empty");
        Assert.notNull(clientUuid, "clientUuid must be non null");
        Assert.notNull(userId, "userId must be non null");
        Assert.hasText(roleName, "roleName must not be empty");
        
        RoleDto role = this.getClientRoleByName(realm, clientUuid, roleName).get();
        keycloakAdminClient.removeClientRolesForUser(realm, userId, clientUuid, Collections.singletonList(role));
    }
}