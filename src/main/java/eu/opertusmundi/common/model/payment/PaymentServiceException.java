package eu.opertusmundi.common.model.payment;

import eu.opertusmundi.common.model.ServiceException;

public final class PaymentServiceException extends ServiceException {

    private static final long serialVersionUID = 1L;

    public PaymentServiceException(PaymentMessageCode code) {
        super(code, "An unhandled exception has occurred");
    }

    public PaymentServiceException(String message) {
        super(PaymentMessageCode.ERROR, "An unhandled exception has occurred");
    }

    public PaymentServiceException(PaymentMessageCode code, String message) {
        super(code, message);
    }

    public PaymentServiceException(String message, Throwable cause) {
        this(PaymentMessageCode.ERROR, message, cause);
    }

    public PaymentServiceException(PaymentMessageCode code, String message, Throwable cause) {
        super(code, message, cause);
    }

}
