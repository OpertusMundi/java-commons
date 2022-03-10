package eu.opertusmundi.common.service;

import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.PathContainer;
import org.springframework.lang.Nullable;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
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

import feign.FeignException;
import feign.FeignException.FeignClientException;
import eu.opertusmundi.common.model.keycloak.KeycloakClientException;
import eu.opertusmundi.common.model.keycloak.server.*;
import eu.opertusmundi.common.feign.client.KeycloakAdminFeignClient;

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
    
    @Autowired
    private KeycloakAdminFeignClient kcadm;
    
    private PathPattern userPathPattern; 
    
    @Autowired
    private void setUserPathPattern(PathPatternParser pathPatternParser) 
        throws NoSuchMethodException, SecurityException
    {
        final Method method = KeycloakAdminFeignClient.class
            .getMethod("getUser", String.class, UUID.class, String.class);
        this.userPathPattern = pathPatternParser.parse(method.getAnnotation(GetMapping.class).path()[0]);
    }
    
    private PathPattern groupPathPattern;
    
    @Autowired
    private void setGroupPathPattern(PathPatternParser pathPatternParser) 
        throws NoSuchMethodException, SecurityException
    {
        final Method method = KeycloakAdminFeignClient.class
            .getMethod("getGroup", String.class, UUID.class, String.class);
        this.groupPathPattern = pathPatternParser.parse(method.getAnnotation(GetMapping.class).path()[0]);
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
    
    private int refreshAccessToken()
    {
        final RefreshTokenForm refreshTokenForm = RefreshTokenForm.of(CLIENT_ID, refreshToken.value);
        final RefreshTokenResponse refreshTokenResponse = kcadm.refreshToken(realm, refreshTokenForm);
        
        final String accessToken = refreshTokenResponse.getAccessToken();
        final int expiresInSeconds = refreshTokenResponse.getExpiresIn();
        this.accessTokenRef.set(new Token(accessToken, Instant.now().plusSeconds(expiresInSeconds)));
        
        return expiresInSeconds;
    }

    private void refreshAccessTokenAndScheduleNextRefresh()
    {
        final int expiresInSeconds = refreshTokenRetryTemplate.execute(new RetryCallback<Integer, FeignException>() {
            @Override
            public Integer doWithRetry(RetryContext context)
            {
                if (logger.isDebugEnabled()) {
                    logger.info("Trying to refresh token for {} [context={}]", url, context);
                }
                return refreshAccessToken();
            }
        });
        
        if (logger.isDebugEnabled()) {
            final Token accessToken = accessTokenRef.get();
            logger.debug("Acquired access token for {}: {} ", url, accessToken.value);
        }
        
        taskScheduler.schedule(this::refreshAccessTokenAndScheduleNextRefresh, 
            Instant.now().plusSeconds((long) (expiresInSeconds * refreshTokenIntervalAsFraction)));        
    }
    
    @PostConstruct
    private void setup()
    {
        Assert.state(url != null, "url must be non null");
        Assert.state(StringUtils.hasText(realm), "realm must be non empty");
        Assert.state(StringUtils.hasText(refreshToken.value), "refreshToken must be non empty");
        
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
    public List<UserDto> findUsers(@Nullable UserQueryDto query)
    {
        final ResponseEntity<List<UserDto>> response = 
            kcadm.findUsers(realm, authorizationHeader(), query != null? query : DEFAULT_USER_QUERY);
        return response.getBody(); 
    }
        
    @Override
    public int countUsers(@Nullable UserQueryDto query)
    {
        final ResponseEntity<Integer> response = 
            kcadm.countUsers(realm, authorizationHeader(), query != null? query : DEFAULT_USER_QUERY);
        return response.getBody();
    }
    
    @Override
    public Optional<UserDto> getUser(UUID userId)
    {
        Assert.notNull(userId, "userId must not be null");
        
        ResponseEntity<UserDto> response = null;
        try {
            response = kcadm.getUser(realm, userId, authorizationHeader());
        } catch (FeignException.NotFound ex) { 
            response = null;
        }
        return Optional.ofNullable(response).map(ResponseEntity::getBody);
    }
    
    @Override
    public UUID createUser(UserDto user)
    {
        Assert.notNull(user, "user object must be non null");
        Assert.isNull(user.getId(), "id must be null");
        Assert.hasText(user.getUsername(), "username must not be empty");
        Assert.hasText(user.getEmail(), "email must not be empty");
        
        ResponseEntity<Void> response = null;
        try {
            response = kcadm.createUser(realm, authorizationHeader(), user);
        } catch (FeignException.BadRequest ex) {
            throw translateException("user representation is invalid", ex);
        } catch (FeignException.Conflict ex) {
            throw translateException("user conflicts with another user", ex);
        }
        
        Assert.state(response.getStatusCode() == HttpStatus.CREATED, "Expected an HTTP status of CREATED");
        final String location = response.getHeaders().get(HttpHeaders.LOCATION).get(0);
        final PathMatchInfo pathMatchInfo = matchAndExtractFromLocation(userPathPattern, location);
        Assert.state(pathMatchInfo != null, "The location path does not match with path of a user resource");
        final String userId = pathMatchInfo.getUriVariables().get("userId");
        
        return UUID.fromString(userId);
    }
    
    @Override
    public void updateUser(UserDto user)
    {
        Assert.notNull(user, "user object must be non null");
        
        final UUID userId = user.getId();
        Assert.notNull(userId, "user.id must be non null");
        Assert.isNull(user.getUsername(), "username must be null (cannot be updated)");
        
        try {
            kcadm.updateUser(realm, userId, authorizationHeader(), user);
        } catch (FeignException.NotFound ex) { 
            throw translateException("user not found", ex);
        } catch (FeignException.BadRequest ex) {
            throw translateException("user representation is invalid", ex);
        } catch (FeignException.Conflict ex) {
            throw translateException("user conflicts with another user", ex);
        }
    }
    
    @Override
    public void deleteUser(UUID userId)
    {
        Assert.notNull(userId, "userId must not be null");
        
        try {
            kcadm.deleteUser(realm, userId, authorizationHeader());
        } catch (FeignException.NotFound ex) {
            throw translateException("user not found", ex);
        }
    }
    
    @Override
    public void resetPasswordForUser(UUID userId, CredentialDto cred)
    {
        Assert.notNull(userId, "userId must not be null");
        Assert.notNull(cred, "credential must not be null");
        Assert.hasText(cred.getValue(), "credential.value must not be empty");
        
        try {
            kcadm.resetPasswordForUser(realm, userId, authorizationHeader(), cred);
        } catch (FeignException.NotFound ex) {
            throw translateException("user not found", ex);
        } catch (FeignException.BadRequest ex) {
            // this is probably due to the password policy enforced by the realm 
            throw translateException("password request is rejected", ex);
        }
    }
    
    @Override
    public void resetPasswordForUser(UUID userId, String password, boolean temporary)
    {
        Assert.notNull(userId, "userId must not be null");
        
        final CredentialDto cred = CredentialDto.ofPassword(password);
        cred.setTemporary(temporary);
        this.resetPasswordForUser(userId, cred);
    }
  
    @Override
    public List<GroupDto> findGroups(@Nullable GroupQueryDto query)
    {
        final ResponseEntity<List<GroupDto>> response = 
            kcadm.findGroups(realm, authorizationHeader(), query != null? query : DEFAULT_GROUP_QUERY);
        return response.getBody();
    }
    
    @Override
    public Optional<GroupDto> getGroup(UUID groupId)
    {
        Assert.notNull(groupId, "groupId must not be null");
        
        ResponseEntity<GroupDto> response = null;
        try {
            response = kcadm.getGroup(realm, groupId, authorizationHeader());
        } catch (FeignException.NotFound ex) { 
            response = null;
        }
        return Optional.ofNullable(response).map(ResponseEntity::getBody);
    }
    
    @Override
    public UUID createGroup(String groupName)
    {
        Assert.hasText(groupName, "groupName must not be emptyl");
        
        final GroupDto group = new GroupDto();
        group.setName(groupName);
        
        ResponseEntity<Void> response = null;
        try {
            response = kcadm.createGroup(realm, authorizationHeader(), group);
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
    public void updateGroup(GroupDto group)
    {
        Assert.notNull(group, "group object must be non null");
        
        final UUID groupId = group.getId();
        Assert.notNull(groupId, "group.id must be non null");
        
        try {
            kcadm.updateGroup(realm, groupId, authorizationHeader(), group);
        } catch (FeignException.NotFound ex) {
            throw translateException("group not found", ex);
        } catch (FeignException.BadRequest ex) {
            throw translateException("group representation is invalid", ex);
        } catch (FeignException.Conflict ex) {
            throw translateException("group conflicts with another group", ex);
        }
    }
    
    @Override
    public List<UserDto> getGroupMembers(UUID groupId, @Nullable PageRequest pageRequest)
    {
        Assert.notNull(groupId, "groupId must not be null");
        
        ResponseEntity<List<UserDto>> response = null;
        try {
            response = kcadm.getGroupMembers(realm, groupId, authorizationHeader(), 
                pageRequest != null? pageRequest : DEFAULT_PAGE_REQUEST);
        } catch (FeignException.NotFound ex) {
            throw translateException("group not found", ex);
        }
        
        return response.getBody();
    }
    
    @Override
    public List<GroupDto> getUserGroups(UUID userId)
    {
        Assert.notNull(userId, "userId must not be null");
        
        ResponseEntity<List<GroupDto>> response = null;
        try {
            response = kcadm.getUserGroups(realm, userId, authorizationHeader());
        } catch (FeignException.NotFound ex) {
            throw translateException("user not found", ex);
        }
        
        return response.getBody();
    }
    
    @Override
    public void deleteGroup(UUID groupId)
    {
        Assert.notNull(groupId, "groupId must not be null");
        
        try {
            kcadm.deleteGroup(realm, groupId, authorizationHeader());
        } catch (FeignException.NotFound ex) {
            throw translateException("group not found", ex);
        }
    }
}
