package eu.opertusmundi.common.model.account;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import eu.opertusmundi.common.model.asset.service.UserServiceDto;

@lombok.Getter
@lombok.Setter
@lombok.ToString(callSuper = true)
@JsonInclude(value = Include.NON_NULL)
public class ServiceUsageSummaryDto extends ServiceUsageKey
{
    private static final long serialVersionUID = 1L;

    public ServiceUsageSummaryDto(UUID serviceKey, LocalDate startDate)
    {
        super(serviceKey, startDate);
    }

    AccountSubscriptionDto subscription;

    UserServiceDto userService;

    Long calls;

    BigDecimal callsNormalized;

    Long responseSizeInBytes;

    BigDecimal responseTimeInSeconds;
}
