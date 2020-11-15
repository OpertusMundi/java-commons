package eu.opertusmundi.common.service;

public final class PaymentServiceException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public PaymentServiceException(String message) {
        super(message);
    }

    public PaymentServiceException(String message, Throwable cause) {
        super(message, cause);
    }

}
