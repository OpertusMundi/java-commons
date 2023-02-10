package eu.opertusmundi.common.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import eu.opertusmundi.common.model.account.ServiceUsageSummaryDto;

public interface ServiceUsageSummaryService
{
    List<ServiceUsageSummaryDto> findAllByServiceKey(UUID serviceKey);
    
    Optional<ServiceUsageSummaryDto> findOneByServiceKeyAndMonthOfYear(UUID serviceKey, int year, int month);
}
