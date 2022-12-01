package eu.opertusmundi.common.service;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.opertusmundi.common.model.account.AccountSubscriptionDto;
import eu.opertusmundi.common.model.payment.ServiceUseStatsDto;
import eu.opertusmundi.common.repository.AccountSubscriptionRepository;

@Service
public class DefaultSubscriptionUseStatsService implements SubscriptionUseStatsService {

    private final AccountSubscriptionRepository accountSubscriptionRepository;

    @Autowired
    public DefaultSubscriptionUseStatsService(AccountSubscriptionRepository accountSubscriptionRepository) {
        this.accountSubscriptionRepository = accountSubscriptionRepository;
    }

    @Override
    public List<ServiceUseStatsDto> getUseStats(UUID userKey, int year, int month) {
        final List<AccountSubscriptionDto> subs = this.accountSubscriptionRepository.findAllObjectsByConsumer(userKey, null, false);

        final List<ServiceUseStatsDto> result = subs.stream()
            .map(s -> this.getUseStats(userKey, s.getKey(), year, month))
            .collect(Collectors.toList());

        return result;
    }

    @Override
    public ServiceUseStatsDto getUseStats(UUID userKey, UUID subscriptionKey, int year, int month) {
        // TODO: Query service use statistics logs ...

        final int                calls  = ThreadLocalRandom.current().nextInt(1, 100000);
        final ServiceUseStatsDto result = ServiceUseStatsDto.builder()
            .userKey(userKey)
            .subscriptionKey(subscriptionKey)
            .calls(calls)
            .rows(0)
            .build();

        result.getClientCalls().put(UUID.randomUUID(), result.getCalls());

        return result;
    }

}
