package eu.opertusmundi.common.service;

import java.time.ZonedDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mangopay.core.enumerations.EventType;

import eu.opertusmundi.common.model.kyc.UpdateKycLevelCommand;
import eu.opertusmundi.common.model.payment.PaymentException;

@Service
@Transactional
public class MangoPayWebhookHandler {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private CustomerVerificationService customerVerificationService;

    public void handleWebHook(String eventType, String resourceId, ZonedDateTime date) throws PaymentException {
        switch (EventType.valueOf(eventType)) {
            case PAYIN_NORMAL_SUCCEEDED :
            case PAYIN_NORMAL_FAILED :
                // Update PayIn
                this.paymentService.sendPayInStatusUpdateMessage(resourceId);
                break;

            case TRANSFER_NORMAL_CREATED:
            case TRANSFER_NORMAL_FAILED:
            case TRANSFER_NORMAL_SUCCEEDED:
                // Update Transfer
                this.paymentService.updateTransfer(resourceId);
                break;

            case KYC_CREATED :
            case KYC_SUCCEEDED :
            case KYC_FAILED :
            case KYC_VALIDATION_ASKED :
            case KYC_OUTDATED :
                // Update KYC document
                break;

            case USER_KYC_REGULAR :
            case USER_KYC_LIGHT :
                // Update customer KYC status
                final UpdateKycLevelCommand command = UpdateKycLevelCommand.of(resourceId, date);

                this.customerVerificationService.updateCustomerKycLevel(command);
                break;

            default :
                // No operation
                break;
        }
    }

}
