package eu.opertusmundi.common.model.account;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

@lombok.Data
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
public class ServiceUsageKey implements Serializable
{
    private static final long serialVersionUID = 1L;

    UUID serviceKey;
    
    LocalDate startDate;
    
    public static ServiceUsageKey of(UUID serviceKey, LocalDate startDate)
    {
        return new ServiceUsageKey(serviceKey, startDate);
    }
    
    public static ServiceUsageKey of(String serviceKey, LocalDate startDate)
    {
        return new ServiceUsageKey(UUID.fromString(serviceKey), startDate);
    }
}