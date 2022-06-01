package eu.opertusmundi.common.model.keycloak.server;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@lombok.Getter
@lombok.ToString
@lombok.RequiredArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
public class RoleDto
{
    public RoleDto(UUID id, String name)
    {
        this(id, name, null);
    }
    
    @JsonCreator
    static RoleDto createFromServerRepresentation(
        @JsonProperty("id") UUID id, 
        @JsonProperty("name") String name,
        @JsonProperty("clientRole") boolean clientRole,
        @JsonProperty("containerId") String containerId)
    {
        return new RoleDto(id, name, clientRole? UUID.fromString(containerId) : null);
    }
    
    @JsonProperty("id")
    final UUID id;
    
    @JsonProperty("name")
    final String name;
    
    /**
     * The uuid of the client if this role is client-scoped, else {@code null}.
     */
    @JsonProperty("containerId")
    final UUID clientUuid;
}
