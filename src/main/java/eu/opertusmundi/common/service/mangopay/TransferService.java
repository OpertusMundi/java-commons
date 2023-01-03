package eu.opertusmundi.common.service.mangopay;

import java.util.List;
import java.util.UUID;

import eu.opertusmundi.common.model.payment.PaymentException;
import eu.opertusmundi.common.model.payment.TransferDto;

public interface TransferService {


    /**
     * Create transfers for all items in a PayIn record
     *
     * @param userKey
     * @param payInKey
     * @return
     * @throws PaymentException
     */
    List<TransferDto> createTransfer(UUID userKey, UUID payInKey) throws PaymentException;

    /**
     * Update transfer
     *
     * This method checks the payment provider for updates of a specific
     * Transfer. A webhook may invoke this method to update a pending transfer
     * with the execution date and its status.
     *
     * @param transfer The payment provider Transfer unique identifier
     * @throws PaymentException
     */
    void updateTransfer(String transferId) throws PaymentException;

}
