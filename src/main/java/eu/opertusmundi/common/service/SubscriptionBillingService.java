package eu.opertusmundi.common.service;

import java.util.List;
import java.util.UUID;

import eu.opertusmundi.common.model.payment.PaymentException;
import eu.opertusmundi.common.model.payment.SubscriptionBillingDto;

public interface SubscriptionBillingService {

    /**
     * Create billing records for all the subscriptions for the specified user
     * over the given interval
     *
     * @param userKey
     * @param year
     * @param month
     * @return
     * @throws PaymentException if the user is not found
     */
    List<SubscriptionBillingDto> create(UUID userKey, int year, int month) throws PaymentException;

}
