package eu.opertusmundi.common.model.payment;

import java.util.Map;

import com.mangopay.core.ResponseException;

import eu.opertusmundi.common.model.ServiceException;
import lombok.Getter;

public final class PaymentException extends ServiceException {

    private static final long serialVersionUID = 1L;

    @Getter
    private String providerMessage;

    @Getter
    private Map<String, String> providerErrors;

    public PaymentException(PaymentMessageCode code) {
        super(code, "An unhandled exception has occurred");
    }

    public PaymentException(String message) {
        super(PaymentMessageCode.API_ERROR, "An unhandled exception has occurred");
    }

    public PaymentException(PaymentMessageCode code, String message) {
        super(code, message);
    }

    public PaymentException(String message, Throwable cause) {
        this(PaymentMessageCode.API_ERROR, message, cause);
    }

    public PaymentException(PaymentMessageCode code, String message, Throwable cause) {
        super(code, message, cause);
    }

    public PaymentException(
        PaymentMessageCode code, String message, Throwable cause, String providerMessage, Map<String, String> providerErrors
    ) {
        super(code, message, cause);

        this.providerMessage = providerMessage;
        this.providerErrors = providerErrors;
    }

    public PaymentException(PaymentMessageCode code, String message, ResponseException cause) {
        this(code, message, cause, cause.getApiMessage(), cause.getErrors());      
    }

}
