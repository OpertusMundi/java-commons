package eu.opertusmundi.common.service;

import java.util.List;
import java.util.UUID;

import eu.opertusmundi.common.model.payment.ServiceUseStatsDto;

public interface SubscriptionUseStatsService {

    /**
     * Get use statistics for all subscriptions of the specified user over the
     * given time interval
     *
     * @param userKey
     * @param year
     * @param month
     * @return
     */
    List<ServiceUseStatsDto> getUseStats(UUID userKey, int year, int month);

    /**
     * Get use statistics for the specified user and subscription over the given
     * time interval
     *
     * @param userKey
     * @param subscriptionKey
     * @param year
     * @param month
     * @return
     */
    ServiceUseStatsDto getUseStats(UUID userKey, UUID subscriptionKey, int year, int month);
}
