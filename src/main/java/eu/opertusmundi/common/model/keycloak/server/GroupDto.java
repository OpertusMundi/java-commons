package eu.opertusmundi.common.model.keycloak.server;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@lombok.Getter
@lombok.ToString
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
public class GroupDto
{
    @lombok.Setter
    @JsonProperty("id")
    private UUID id;
    
    @lombok.Setter
    @JsonProperty("name")
    private String name;
    
    @lombok.Setter
    @JsonProperty("path")
    private String path;
}
