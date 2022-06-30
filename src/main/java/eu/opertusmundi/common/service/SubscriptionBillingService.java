package eu.opertusmundi.common.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import eu.opertusmundi.common.model.payment.PaymentException;
import eu.opertusmundi.common.model.payment.SubscriptionBillingBatchCommandDto;
import eu.opertusmundi.common.model.payment.SubscriptionBillingBatchDto;
import eu.opertusmundi.common.model.payment.SubscriptionBillingDto;

public interface SubscriptionBillingService {

    /**
     * Find subscription billing batch by key
     * 
     * @param key
     * @return
     */
    Optional<SubscriptionBillingBatchDto> findOneByKey(UUID key);

    /**
     * Start a new workflow instance for creating subscription billing records
     *
     * @param command
     * @return
     * @throws PaymentException if the workflow instance fails to start
     */
    SubscriptionBillingBatchDto start(SubscriptionBillingBatchCommandDto command) throws PaymentException;

    /**
     * Create billing records for all the subscriptions for the specified user
     * over the given interval
     *
     * @param userKey
     * @param year
     * @param month
     * @param quotationOnly
     * @return
     * @throws PaymentException if the user is not found
     */
    List<SubscriptionBillingDto> create(UUID userKey, int year, int month, boolean quotationOnly) throws PaymentException;

    /**
     * Mark a subscription billing batch execution as completed
     * 
     * @param key
     * @param totalSubscriptions
     * @param totalPrice
     * @param totalPriceExcludingTax
     * @param totalTax
     */
    void complete(UUID key, int totalSubscriptions, BigDecimal totalPrice, BigDecimal totalPriceExcludingTax, BigDecimal totalTax);
    
    /**
     * Mark a subscription billing batch execution as failed
     * 
     * @param key
     */
    void fail(UUID key);
}
