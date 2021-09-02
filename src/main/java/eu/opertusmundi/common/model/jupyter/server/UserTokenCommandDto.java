package eu.opertusmundi.common.model.jupyter.server;

import com.fasterxml.jackson.annotation.JsonProperty;

@lombok.Getter
@lombok.Setter
@lombok.AllArgsConstructor
@lombok.NoArgsConstructor
public class UserTokenCommandDto 
{
    /**
     * The lifetime (in seconds) after which the requested token will expire.
     */
    @JsonProperty("expires_in")
    private int expiresIn;
    
    /**
     * An optional description for the token 
     */
    private String note;
}
