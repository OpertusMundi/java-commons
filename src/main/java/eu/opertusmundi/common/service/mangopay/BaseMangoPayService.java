package eu.opertusmundi.common.service.mangopay;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;

import com.mangopay.MangoPayApi;
import com.mangopay.core.ResponseException;

import eu.opertusmundi.common.model.payment.PaymentException;
import eu.opertusmundi.common.model.payment.PaymentMessageCode;

public abstract class BaseMangoPayService {

    @Value("${opertusmundi.payments.mangopay.base-url:}")
    private String baseUrl;

    @Value("${opertusmundi.payments.mangopay.client-id:}")
    private String clientId;

    @Value("${opertusmundi.payments.mangopay.client-password:}")
    private String clientPassword;
    
    protected MangoPayApi api;
    
    @PostConstruct
    private void init() {
        this.api = new MangoPayApi();

        this.api.getConfig().setBaseUrl(this.baseUrl);
        this.api.getConfig().setClientId(this.clientId);
        this.api.getConfig().setClientPassword(this.clientPassword);
    }
    
    /**
     * Wraps an exception with {@link PaymentException}
     *
     * @param operation
     * @param ex
     * @return
     */
    protected PaymentException wrapException(String operation, Exception ex, Object command, Logger logger) {
        final String commandText = command == null ? "-" : command.toString();

        // Ignore service exceptions
        if (ex instanceof PaymentException) {
            return (PaymentException) ex;
        }

        // MANGOPAY exceptions
        if (ex instanceof ResponseException) {
            final String message = String.format(
                "MANGOPAY operation has failed. [operation=%s, apiMessage=%s, command=[%s]]",
                operation, ((ResponseException) ex).getApiMessage(), commandText
            );

            logger.error(message, ex);

            return new PaymentException(PaymentMessageCode.API_ERROR, message, ex);
        }

        // Global exception handler
        final String message = String.format("[MANGOPAY] %s [%s]", operation, commandText);

        logger.error(message, ex);

        return new PaymentException(PaymentMessageCode.SERVER_ERROR, message, ex);
    }
    
}
