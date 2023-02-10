package eu.opertusmundi.common.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.opertusmundi.common.domain.ServiceUsageSummaryEntity;
import eu.opertusmundi.common.model.account.AccountSubscriptionDto;
import eu.opertusmundi.common.model.account.ServiceUsageSummaryDto;
import eu.opertusmundi.common.model.asset.service.UserServiceDto;
import eu.opertusmundi.common.model.payment.EnumBillableServiceType;
import eu.opertusmundi.common.model.payment.ServiceUseStatsDto;
import eu.opertusmundi.common.repository.AccountSubscriptionRepository;
import eu.opertusmundi.common.repository.ServiceUsageSummaryRepository;
import eu.opertusmundi.common.repository.UserServiceRepository;

@Service
public class DefaultServiceUseStatsService implements ServiceUseStatsService {

    private final AccountSubscriptionRepository accountSubscriptionRepository;
    private final UserServiceRepository userServiceRepository;
    private final ServiceUsageSummaryRepository serviceUsageSummaryRepository;
    
    @Autowired
    public DefaultServiceUseStatsService(
        AccountSubscriptionRepository accountSubscriptionRepository,
        UserServiceRepository userServiceRepository,
        ServiceUsageSummaryRepository serviceUsageSummaryRepository
    ) {
        this.accountSubscriptionRepository = accountSubscriptionRepository;
        this.userServiceRepository = userServiceRepository;
        this.serviceUsageSummaryRepository = serviceUsageSummaryRepository;
    }

    @Override
    public List<ServiceUseStatsDto> getUseStats(UUID userKey, int year, int month) {
        final var result = new ArrayList<ServiceUseStatsDto>();

        final List<AccountSubscriptionDto> subscriptions = this.accountSubscriptionRepository.findAllObjectsByConsumer(userKey, null, false);

        subscriptions.stream()
            .map(s -> this.getUseStats(EnumBillableServiceType.SUBSCRIPTION, userKey, s.getKey(), year, month))
            .forEach(result::add);

        final List<UserServiceDto> services = this.userServiceRepository.findAllObjectsByParent(userKey, false);

        services.stream()
            .map(s -> this.getUseStats(EnumBillableServiceType.PRIVATE_OGC_SERVICE, userKey, s.getKey(), year, month))
            .forEach(result::add);

        return result;
    }

    @Override
    public ServiceUseStatsDto getUseStats(EnumBillableServiceType type, UUID userKey, UUID serviceKey, int year, int month) {

        final ServiceUsageSummaryDto serviceUsageSummaryDto = 
            serviceUsageSummaryRepository.findOneByServiceKeyAndMonthOfYear(serviceKey, year, month)
                .map(ServiceUsageSummaryEntity::toDto).orElse(null);
        
        // we are dealing with monthly usage, so we don't expect calls to overflow an int
        final int calls = Math.toIntExact(serviceUsageSummaryDto.getCalls());
        
        final ServiceUseStatsDto result = ServiceUseStatsDto.builder()
            .type(type)
            .userKey(userKey)
            .serviceKey(serviceKey)
            .calls(calls)
            .rows(0)
            .build();

        // Todo; per-client statistics are not available (yet)
        //result.getClientCalls().put(UUID.randomUUID(), result.getCalls());

        return result;
    }

}
