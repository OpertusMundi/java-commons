package eu.opertusmundi.common.model.keycloak.server;

import com.fasterxml.jackson.annotation.JsonProperty;

@lombok.Getter
@lombok.Setter
@lombok.ToString(callSuper = true)
@lombok.NoArgsConstructor
public class RoleQueryDto extends PageRequest
{
    @JsonProperty("search")
    String search;
}
