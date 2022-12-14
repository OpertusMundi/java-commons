package eu.opertusmundi.common.service;

import java.util.List;
import java.util.UUID;

import eu.opertusmundi.common.model.payment.EnumBillableServiceType;
import eu.opertusmundi.common.model.payment.ServiceUseStatsDto;

public interface ServiceUseStatsService {

    /**
     * Get use statistics for all subscriptions and private OGC services of the
     * specified user over the given time interval
     *
     * @param userKey
     * @param year
     * @param month
     * @return
     */
    List<ServiceUseStatsDto> getUseStats(UUID userKey, int year, int month);

    /**
     * Get use statistics for the specified user and subscription/service over
     * the given time interval
     *
     * @param type
     * @param userKey
     * @param serviceKey
     * @param year
     * @param month
     * @return
     */
    ServiceUseStatsDto getUseStats(EnumBillableServiceType type, UUID userKey, UUID serviceKey, int year, int month);
}
