package eu.opertusmundi.common.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.opertusmundi.common.domain.ServiceUsageSummaryEntity;
import eu.opertusmundi.common.model.account.ServiceUsageSummaryDto;
import eu.opertusmundi.common.repository.ServiceUsageSummaryRepository;

@Service
public class DefaultServiceUsageSummaryService implements ServiceUsageSummaryService
{
    @Autowired
    private ServiceUsageSummaryRepository serviceUsageSummaryRepository;
    
    @Override
    public List<ServiceUsageSummaryDto> findAllByServiceKey(UUID serviceKey)
    {
        return serviceUsageSummaryRepository.findAllByServiceKey(serviceKey).stream()
            .map(ServiceUsageSummaryEntity::toDto)
            .collect(Collectors.toList());
    }

    @Override
    public Optional<ServiceUsageSummaryDto> findOneByServiceKeyAndMonthOfYear(UUID serviceKey, int year, int month)
    {
        return serviceUsageSummaryRepository.findOneByServiceKeyAndMonthOfYear(serviceKey, year, month)
            .map(ServiceUsageSummaryEntity::toDto);
    }

}