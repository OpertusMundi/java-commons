package eu.opertusmundi.common.service.mangopay;

import eu.opertusmundi.common.model.payment.DisputeDto;
import eu.opertusmundi.common.model.payment.PaymentException;

public interface DisputeService {

    /**
     * Create dispute database record
     *
     * @param eventType
     * @param disputeId
     * @return
     * @throws PaymentException
     */
    DisputeDto createDispute(String eventType, String disputeId) throws PaymentException;

}
