package eu.opertusmundi.common.service.mangopay;

import java.util.UUID;

import eu.opertusmundi.common.model.EnumSortingOrder;
import eu.opertusmundi.common.model.PageResultDto;
import eu.opertusmundi.common.model.payment.EnumPayOutSortField;
import eu.opertusmundi.common.model.payment.EnumTransactionStatus;
import eu.opertusmundi.common.model.payment.PayOutCommandDto;
import eu.opertusmundi.common.model.payment.PayOutDto;
import eu.opertusmundi.common.model.payment.PaymentException;

public interface PayOutService {

    /**
     * Create a PayOut record from a provider's wallet to her bank account in
     * the OpertusMundi platform.
     *
     * @param command
     * @return
     * @throws PaymentException
     */
    PayOutDto createPayOutAtOpertusMundi(PayOutCommandDto command) throws PaymentException;

    /**
     * Create a PayOut at the MANGOPAY service
     *
     * @param payOutKey
     * @return
     * @throws PaymentException
     */
    PayOutDto createPayOutAtProvider(UUID payOutKey) throws PaymentException;

    /**
     * Sends a message to a PayOut process instance to update its status
     *
     * @param payOutId
     * @throws PaymentException
     */
    void sendPayOutStatusUpdateMessage(String payOutId) throws PaymentException;

    /**
     * Update PayOut
     *
     * @param payOutKey PayOut unique OpertusMundi key
     * @param payOutId The payment provider unique PayOut identifier
     * @return
     * @throws PaymentException
     */
    PayOutDto updatePayOut(UUID payOutKey, String payOutId) throws PaymentException;

    /**
     * Update PayOut refund
     *
     * @param refundId
     * @return
     * @throws PaymentException
     */
    PayOutDto updatePayOutRefund(String refundId) throws PaymentException;

    /**
     * Get provider PayOut by key
     *
     * @param userId
     * @param payOutKey
     * @return
     */
    PayOutDto getProviderPayOut(Integer userId, UUID payOutKey);

    /**
     * Search provider PayOuts
     *
     * @param userKey
     * @param status
     * @param pageIndex
     * @param pageSize
     * @param orderBy
     * @param order
     * @return
     */
    PageResultDto<PayOutDto> findAllProviderPayOuts(
        UUID userKey, EnumTransactionStatus status, int pageIndex, int pageSize, EnumPayOutSortField orderBy, EnumSortingOrder order
    );
}
