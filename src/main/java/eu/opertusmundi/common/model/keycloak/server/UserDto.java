package eu.opertusmundi.common.model.keycloak.server;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@lombok.Getter
@lombok.Setter
@lombok.ToString
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
public class UserDto
{
    @JsonProperty("id")
    private UUID id;
    
    @JsonProperty("enabled")
    private Boolean enabled;
    
    @JsonProperty("email")
    private String email;
    
    @JsonProperty("emailVerified")
    private Boolean emailVerified; 
    
    @JsonProperty("username")
    private String username;
    
    @JsonProperty("firstName")
    private String firstName;
    
    @JsonProperty("lastName")
    private String lastName;
    
    @JsonIgnore
    private Instant created;
    
    @JsonProperty("requiredActions")
    private Set<EnumRequiredAction> requiredActions;

    @JsonSetter("createdTimestamp")
    public void setCreatedFromTimestamp(Long t)
    {
        this.created = Instant.ofEpochMilli(t);
    }
    
    @JsonGetter("createdTimestamp")
    public Long getCreatedTimestamp()
    {
        return created == null? null : created.toEpochMilli();
    }
    
    @JsonProperty("attributes")
    private Map<String, String[]> attributes;
    
    public void setAttributes(Map<String, String[]> attributes)
    {
        this.attributes = Collections.unmodifiableMap(attributes);
    }
}
