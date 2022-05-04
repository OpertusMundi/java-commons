package eu.opertusmundi.common.model.keycloak.server;

/**
 * See https://www.keycloak.org/docs-api/15.0/rest-api/index.html#_users_resource
 */
@lombok.Getter
@lombok.Setter
@lombok.ToString(callSuper = true)
@lombok.NoArgsConstructor
public class UserQueryDto extends PageRequest
{
    protected String email;
    
    protected String username;
    
    protected String search;

    protected Boolean exact;

    protected Boolean enabled;
}
