package eu.opertusmundi.common.service.mangopay;

import java.time.ZonedDateTime;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mangopay.core.enumerations.EventType;

import eu.opertusmundi.common.model.kyc.UpdateKycLevelCommand;
import eu.opertusmundi.common.model.payment.PaymentException;
import eu.opertusmundi.common.model.payment.PaymentMessageCode;

@Service
@Transactional
public class MangoPayWebhookHandler {

    private final CustomerVerificationService customerVerificationService;
    private final PayInService                paymentService;
    private final PayOutService               payoutService;
    private final TransferService             transferService;
    private final UserService                 userService;

    @Autowired
    public MangoPayWebhookHandler(
        CustomerVerificationService customerVerificationService,
        PayInService                paymentService,
        PayOutService               payoutService,
        TransferService             transferService,
        UserService                 userService
    ) {
        this.paymentService              = paymentService;
        this.payoutService               = payoutService;
        this.transferService             = transferService;
        this.customerVerificationService = customerVerificationService;
        this.userService                 = userService;
    }

    public void handleWebHook(String eventType, String resourceId, ZonedDateTime date) throws PaymentException {
        // Ignore empty requests (requests may originate from web hook registration)
        if (StringUtils.isBlank(resourceId)) {
            return;
        }

        switch (EventType.valueOf(eventType)) {
            case KYC_SUCCEEDED :
            case KYC_FAILED :
            case KYC_OUTDATED :
                // TODO: Notify user
                break;

            case PAYIN_NORMAL_SUCCEEDED :
            case PAYIN_NORMAL_FAILED :
                // Update PayIn status
                this.paymentService.updateWorkflowInstancePayInStatus(resourceId);
                break;

            case PAYIN_REFUND_SUCCEEDED :
            case PAYIN_REFUND_FAILED :
                // TODO: Update PayIn refund
                break;

            case PAYOUT_NORMAL_SUCCEEDED :
            case PAYOUT_NORMAL_FAILED :
                // Update PayOut
                this.payoutService.sendPayOutStatusUpdateMessage(resourceId);
                break;

            case PAYOUT_REFUND_SUCCEEDED :
            case PAYOUT_REFUND_FAILED :
                // Update PayOut refund
                this.payoutService.updatePayOutRefund(resourceId);
                break;

            case TRANSFER_NORMAL_FAILED :
            case TRANSFER_NORMAL_SUCCEEDED :
                // Update Transfer
                this.transferService.updateTransfer(resourceId);
                break;

            case TRANSFER_REFUND_SUCCEEDED :
            case TRANSFER_REFUND_FAILED :
                // TODO: Update transfer refund
                break;

            case UBO_DECLARATION_REFUSED :
            case UBO_DECLARATION_VALIDATED :
            case UBO_DECLARATION_INCOMPLETE :
                // TODO: Notify user
                break;

            case USER_KYC_REGULAR :
            case USER_KYC_LIGHT :
                // Update customer KYC status
                final UpdateKycLevelCommand command = UpdateKycLevelCommand.of(resourceId, date);

                this.customerVerificationService.updateCustomerKycLevel(command);
                break;

            case USER_INFLOWS_BLOCKED :
            case USER_INFLOWS_UNBLOCKED :
            case USER_OUTFLOWS_BLOCKED :
            case USER_OUTFLOWS_UNBLOCKED :
                this.userService.updateUserBlockStatus(resourceId);
                break;

            // TODO: Add support for recurring registration web hooks after java
            // SDK is updated (currently the enumeration values are not declared)
            // See: https://docs.mangopay.com/blog/new-release-shamrock

            /*
            case RECURRING_REGISTRATION_CREATED:
            case RECURRING_REGISTRATION_AUTH_NEEDED:
            case RECURRING_REGISTRATION_IN_PROGRESS:
            case RECURRING_REGISTRATION_ENDED:
            */

            default :
                // Fail on unknown (not registered) web hooks
                throw new PaymentException(PaymentMessageCode.WEB_HOOK_NOT_SUPPORTED);
        }
    }

}
