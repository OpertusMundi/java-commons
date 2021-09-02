package eu.opertusmundi.common.model.payment;

import com.mangopay.core.enumerations.TransactionStatus;

import eu.opertusmundi.common.model.catalogue.client.EnumDeliveryMethod;
import eu.opertusmundi.common.model.order.EnumOrderStatus;

public enum EnumTransactionStatus {
    /**
     * Not specified.
     */
    NotSpecified,
    /**
     * CREATED transaction status.
     */
    CREATED,
    /**
     * SUCCEEDED transaction status.
     */
    SUCCEEDED,
    /**
     * FAILED transaction status.
     */
    FAILED
    ;

    public static EnumTransactionStatus from(TransactionStatus s) throws PaymentException {
        switch (s) {
            case CREATED :
                return EnumTransactionStatus.CREATED;
            case FAILED :
                return EnumTransactionStatus.FAILED;
            case SUCCEEDED :
                return EnumTransactionStatus.SUCCEEDED;
            default :
                throw new PaymentException(
                    PaymentMessageCode.ENUM_MEMBER_NOT_SUPPORTED,
                    String.format("Transaction status [%s] is not supported", s)
                );
        }
    }

    public EnumOrderStatus toOrderStatus(EnumDeliveryMethod deliveryMethod) throws PaymentException {
        switch (this) {
            case FAILED :
                return EnumOrderStatus.CANCELLED;
            case SUCCEEDED :
                return deliveryMethod == EnumDeliveryMethod.DIGITAL_PLATFORM
                    ? EnumOrderStatus.ASSET_REGISTRATION
                    : EnumOrderStatus.PENDING_PROVIDER_SEND_CONFIRMATION;
            default :
                throw new PaymentException(
                    PaymentMessageCode.ENUM_MEMBER_NOT_SUPPORTED,
                    String.format("Transaction status [%s] is not supported", this)
                );
        }
    }

}
