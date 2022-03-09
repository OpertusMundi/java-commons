package eu.opertusmundi.common.model.keycloak.server;

@lombok.Getter
@lombok.Setter
@lombok.ToString(callSuper = true)
@lombok.NoArgsConstructor
public class GroupQueryDto extends PageRequest
{
    protected String search;
}
