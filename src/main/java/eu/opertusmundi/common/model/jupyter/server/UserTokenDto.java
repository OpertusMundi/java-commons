package eu.opertusmundi.common.model.jupyter.server;

import java.time.ZonedDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@lombok.Setter
@lombok.Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserTokenDto 
{
    /**
     * The user that owns the token
     */
    @JsonProperty("user")
    private String userName;
    
    /**
     * An identifier for this token
     */
    private String id;
    
    @JsonProperty("created")
    private ZonedDateTime createdAt;
    
    @JsonProperty("last_activity")
    private ZonedDateTime lastActivityAt;
    
    @JsonProperty("expires_at")
    private ZonedDateTime expiresAt;
    
    private String note;
}
