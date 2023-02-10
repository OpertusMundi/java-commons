package eu.opertusmundi.common.model.account;

import java.time.LocalDate;
import java.util.UUID;

import eu.opertusmundi.common.model.asset.service.UserServiceDto;

@lombok.Getter
@lombok.Setter
@lombok.ToString(callSuper = true)
public class ServiceUsageSummaryDto extends ServiceUsageKey
{
    public ServiceUsageSummaryDto(UUID serviceKey, LocalDate startDate)
    {
        super(serviceKey, startDate);
    }

    private static final long serialVersionUID = 1L;

    AccountSubscriptionDto subscription;
    
    UserServiceDto userService;
    
    Long calls;
    
    Float callsNormalized;
    
    Long responseSizeInBytes;
    
    Float responseTimeInSeconds;
}
