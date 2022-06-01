package eu.opertusmundi.common.model.keycloak.server;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.util.Assert;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

@lombok.Setter
@lombok.Getter
@lombok.ToString
@lombok.NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
public class ClientDto
{
    public static final String[] DEFAULT_CLIENT_SCOPES = new String[] {
        "web-origins", "roles", "profile", "email"
    };
    
    public static final String[] OPTIONAL_CLIENT_SCOPES = new String[] {
        "address", "phone", "offline_access", "microprofile-jwt"
    };
    
    public static final String ACCESS_TOKEN_LIFESPAN_ATTRIBUTE_NAME = "access.token.lifespan";
    
    public static final String USE_REFRESH_TOKENS_ATTRIBUTE_NAME = "use.refresh.tokens";
    
    public static final String OIDC_PROTOCOL_NAME = "openid-connect";
    
    @JsonProperty("id")
    private UUID id;
    
    @JsonProperty("clientId")
    private String clientId;
    
    @JsonProperty("enabled")
    private Boolean enabled;
    
    @JsonProperty("protocol")
    private String protocol;
    
    @JsonProperty("name")
    private String name;
    
    // NOTE: The following URLs are not always valid URLs (sometimes are substitution 
    // patterns such as "${authBaseUrl}"). So, they are represented as strings (and not as URLs). 
    
    @JsonProperty("rootUrl")
    private String rootUrl;
    
    @JsonProperty("baseUrl")
    private String baseUrl;
    
    @JsonProperty("adminUrl")
    private String adminUrl;
    
    @JsonProperty("redirectUris")
    private String[] redirectUris;
    
    @JsonProperty("webOrigins")
    private String[] webOrigins;
    
    @JsonProperty("bearerOnly")
    private Boolean bearerOnly;
    
    @JsonProperty("publicClient")
    private Boolean publicClient;
    
    @JsonProperty("consentRequired")
    private Boolean consentRequired;

    @JsonProperty("standardFlowEnabled")
    private Boolean standardFlowEnabled;

    @JsonProperty("implicitFlowEnabled")
    private Boolean implicitFlowEnabled;

    @JsonProperty("directAccessGrantsEnabled")
    private Boolean directAccessGrantsEnabled;
    
    @JsonProperty("serviceAccountsEnabled")
    private Boolean serviceAccountsEnabled;
    
    @JsonProperty("frontchannelLogout")
    private Boolean frontchannelLogout;
    
    @JsonProperty("fullScopeAllowed")
    private Boolean fullScopeAllowed;
    
    @JsonProperty("defaultClientScopes")
    private String[] defaultClientScopes;
    
    @JsonProperty("optionalClientScopes")
    private String[] optionalClientScopes;
    
    @JsonProperty("attributes")
    private Map<String, String> attributes = new LinkedHashMap<>();

    @JsonSetter("attributes")
    protected void setAttributes(Map<String, String> attributes)
    {
        this.attributes = Collections.unmodifiableMap(attributes);
    }
    
    public void addAttribute(String name, String value)
    {
        this.attributes.put(name, value);
    }
    
    public void clearAttributes()
    {
        this.attributes.clear();
    }
    
    @JsonIgnore
    public void setAccessTokenLifespan(int seconds)
    {
        Assert.isTrue(seconds >= 60, "lifespan must be >= 1min");
        this.attributes.put(ACCESS_TOKEN_LIFESPAN_ATTRIBUTE_NAME, String.valueOf(seconds));
    }
    
    @JsonIgnore
    public int getAccessTokenLifespan()
    {
        String value = this.attributes.get(ACCESS_TOKEN_LIFESPAN_ATTRIBUTE_NAME);
        return value == null? -1 : Integer.parseInt(value);
    }
    
    @JsonIgnore
    public void setUseRefreshTokens(boolean flag)
    {
        this.attributes.put(USE_REFRESH_TOKENS_ATTRIBUTE_NAME, String.valueOf(flag));
    }
    
    @JsonIgnore
    public boolean isUseRefreshTokens()
    {
        String value = this.attributes.get(USE_REFRESH_TOKENS_ATTRIBUTE_NAME);
        return value == null? false : Boolean.parseBoolean(value);
    }
    
    public static ClientDto createWithDefaults(String clientId, URL baseUrl) throws MalformedURLException
    {
        Assert.hasText(clientId, "clientId must not be empty");
        Assert.notNull(baseUrl, "baseUrl must not be null");
        Assert.isTrue(baseUrl.getPath().endsWith("/"), "baseUrl.path must have a trailing slash (/)");
        
        ClientDto client = new ClientDto();
        client.clientId = clientId;
        client.protocol = OIDC_PROTOCOL_NAME;
        
        client.baseUrl = baseUrl.toString();
        client.redirectUris = new String[] {
            // by default, everything under the base URL is allowed as redirect URI
            (new URL(baseUrl, "*")).toString()
        };
        
        client.bearerOnly = Boolean.FALSE;
        client.publicClient = Boolean.FALSE;
        client.consentRequired = Boolean.FALSE;
        client.standardFlowEnabled = Boolean.TRUE;
        client.implicitFlowEnabled = Boolean.FALSE;
        client.directAccessGrantsEnabled = Boolean.TRUE;
        client.serviceAccountsEnabled = Boolean.TRUE;
        client.frontchannelLogout = Boolean.FALSE;
        client.fullScopeAllowed = Boolean.TRUE;
        
        client.defaultClientScopes = DEFAULT_CLIENT_SCOPES;
        client.optionalClientScopes = OPTIONAL_CLIENT_SCOPES;
        
        return client;
    }
}
