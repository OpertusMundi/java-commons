package eu.opertusmundi.common.model.keycloak.server;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@lombok.Getter
@lombok.Setter
@lombok.NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class RefreshTokenResponse
{
    @JsonProperty("access_token")
    private String accessToken;
    
    @JsonProperty("expires_in")
    private int expiresIn;
}
