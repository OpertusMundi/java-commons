package eu.opertusmundi.common.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import eu.opertusmundi.common.model.EnumSortingOrder;
import eu.opertusmundi.common.model.EnumView;
import eu.opertusmundi.common.model.PageResultDto;
import eu.opertusmundi.common.model.account.EnumPayoffStatus;
import eu.opertusmundi.common.model.account.EnumServiceBillingRecordSortField;
import eu.opertusmundi.common.model.payment.EnumBillableServiceType;
import eu.opertusmundi.common.model.payment.EnumTransactionStatus;
import eu.opertusmundi.common.model.payment.PaymentException;
import eu.opertusmundi.common.model.payment.ServiceBillingDto;
import eu.opertusmundi.common.model.payment.ServiceBillingBatchCommandDto;
import eu.opertusmundi.common.model.payment.ServiceBillingBatchDto;
import eu.opertusmundi.common.model.pricing.PerCallPricingModelCommandDto;

public interface ServiceBillingService {

    /**
     * Find subscription billing records
     *
     * @param view
     * @param type The type of the billing record
     * @param ownerKey The owner of the subscription or user service
     * @param providerKey The provider of the subscription service
     * @param serviceKey The subscription or user service unique key
     * @param status
     * @param pageIndex
     * @param pageSize
     * @param orderBy
     * @param order
     * @return
     */
    PageResultDto<ServiceBillingDto> findAllServiceBillingRecords(
        EnumView view, EnumBillableServiceType type,
        UUID ownerKey, UUID providerKey, UUID serviceKey, Set<EnumPayoffStatus> status,
        int pageIndex, int pageSize,
        EnumServiceBillingRecordSortField orderBy, EnumSortingOrder order
    );
    /**
     * Find subscription billing record by key
     *
     * @param view
     * @param key
     * @return
     */
    Optional<ServiceBillingDto> findOneServiceBillingRecord(EnumView view, UUID key);

    /**
     * Find subscription billing batch by key
     *
     * @param key
     * @return
     */
    Optional<ServiceBillingBatchDto> findOneBillingIntervalByKey(UUID key);

    /**
     * Start a new workflow instance for creating subscription billing records
     *
     * @param command
     * @return
     * @throws PaymentException if the workflow instance fails to start
     */
    ServiceBillingBatchDto start(ServiceBillingBatchCommandDto command) throws PaymentException;

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
    List<ServiceBillingDto> create(UUID userKey, int year, int month, boolean quotationOnly) throws PaymentException;

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

    /**
     * Initialize a workflow instance to process the referenced PayIn.
     *
     * This method is applicable for PayIns that refer to one or more subscription billing records
     *
     * @param payInKey
     * @param payInId
     * @param payInStatus
     * @return
     */
    String startPayInWorkflow(UUID payInKey, String payInId, EnumTransactionStatus payInStatus);

    /**
     * Updates the status of the subscription billing records for a successful
     * PayIn to {@link EnumPayoffStatus#PAID}
     *
     * @param payInKey
     * @throws PaymentException
     */
    void updatePayoff(UUID payInKey) throws PaymentException;

    /**
     * Resets the status of the subscription billing records for a failed
     * PayIn to {@link EnumPayoffStatus#DUE}
     *
     * @param payInKey
     * @throws PaymentException
     */
    void cancelPayoff(UUID payInKey) throws PaymentException;

    /**
     * Get the default pricing model for private OGC Services
     *
     * @return
     */
    PerCallPricingModelCommandDto getPrivateServicePricingModel();

    /**
     * Set the default pricing model for private OGC services
     *
     * @param userId
     * @param model
     */
    void setPrivateServicePricingModel(int userId, PerCallPricingModelCommandDto model);

}
