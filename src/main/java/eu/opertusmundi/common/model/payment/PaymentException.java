package eu.opertusmundi.common.model.payment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.mangopay.core.ResponseException;

import eu.opertusmundi.common.model.Message;
import eu.opertusmundi.common.model.ServiceException;
import eu.opertusmundi.common.model.ValidationMessage;
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

    public PaymentException() {
        super(PaymentMessageCode.API_ERROR, "An unhandled exception has occurred");
    }

    public PaymentException(String message) {
        super(PaymentMessageCode.API_ERROR, message);
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
        PaymentMessageCode code, String message, Throwable cause, 
        String providerMessage, Map<String, String> providerErrors, List<Message> messages
    ) {
        super(code, message, cause);

        this.providerMessage = providerMessage;
        this.providerErrors  = providerErrors;

        this.getMessages().addAll(messages);
    }

    public static PaymentException of(PaymentMessageCode code, String message, ResponseException cause) {
        final boolean             isParamError = cause.getType().equalsIgnoreCase("param_error");
        final Map<String, String> errors       = cause.getErrors();
        final List<Message>       messages     = new ArrayList<>();

        if (isParamError && errors != null) {
            errors.keySet().stream()
                .map(key -> new ValidationMessage(key, errors.get(key), null, null))
                .forEach(messages::add);
        }
        
        return new PaymentException(code, message, cause, cause.getApiMessage(), errors, messages);
    }

}
