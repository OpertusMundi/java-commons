package eu.opertusmundi.common.model.jupyter.server;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@lombok.Getter
@lombok.Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserTokensDto 
{
    @JsonProperty("api_tokens")
    private List<UserTokenDto> apiTokens;
}
