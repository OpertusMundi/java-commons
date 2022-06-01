package eu.opertusmundi.common.model.keycloak.server;

import org.springframework.util.Assert;

@lombok.Getter
@lombok.Setter
@lombok.ToString(callSuper = true)
@lombok.NoArgsConstructor
public class ClientQueryDto extends PageRequest
{
    protected String clientId;
    
    protected String q;
    
    protected Boolean search;
    
    protected Boolean viewableOnly;
    
    public static ClientQueryDto forClientId(String clientId)
    {
        Assert.hasText(clientId, "clientId must not be empty");
        ClientQueryDto q = new ClientQueryDto();
        q.clientId = clientId;
        return q;
    }
}
