package eu.opertusmundi.common.model.keycloak.server;

@lombok.Getter
@lombok.Setter
@lombok.ToString
public class PageRequest
{
    Integer max;
    
    Integer first;
    
    public static PageRequest of(int start, int size)
    {
        PageRequest r = new PageRequest();
        r.first = start;
        r.max = size;
        return r;
    }
}