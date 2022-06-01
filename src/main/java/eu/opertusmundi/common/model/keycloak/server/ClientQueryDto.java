package eu.opertusmundi.common.model.keycloak.server;

import io.jsonwebtoken.lang.Assert;

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
        Assert.hasText(clientId);
        ClientQueryDto q = new ClientQueryDto();
        q.clientId = clientId;
        return q;
    }
}
