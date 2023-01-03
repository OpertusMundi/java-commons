package eu.opertusmundi.common.service.mangopay;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import com.mangopay.MangoPayApi;
import com.mangopay.core.Money;
import com.mangopay.core.ResponseException;
import com.mangopay.core.enumerations.CardType;
import com.mangopay.core.enumerations.CurrencyIso;
import com.mangopay.core.enumerations.PayInExecutionType;
import com.mangopay.core.enumerations.PayInPaymentType;
import com.mangopay.core.enumerations.SecureMode;
import com.mangopay.entities.IdempotencyResponse;
import com.mangopay.entities.PayIn;
import com.mangopay.entities.subentities.PayInExecutionDetailsDirect;
import com.mangopay.entities.subentities.PayInPaymentDetailsCard;

import eu.opertusmundi.common.domain.AccountEntity;
import eu.opertusmundi.common.domain.CustomerDraftEntity;
import eu.opertusmundi.common.domain.CustomerEntity;
import eu.opertusmundi.common.model.account.EnumCustomerRegistrationStatus;
import eu.opertusmundi.common.model.account.EnumCustomerType;
import eu.opertusmundi.common.model.payment.CardDirectPayInCommand;
import eu.opertusmundi.common.model.payment.CardDirectPayInExecutionContext;
import eu.opertusmundi.common.model.payment.PaymentException;
import eu.opertusmundi.common.model.payment.PaymentMessageCode;
import eu.opertusmundi.common.repository.AccountRepository;

public abstract class BaseMangoPayService {

    private static final Logger logger = LoggerFactory.getLogger(BaseMangoPayService.class);

    @Value("${opertusmundi.payments.mangopay.base-url:}")
    private String baseUrl;

    @Value("${opertusmundi.payments.mangopay.client-id:}")
    private String clientId;

    @Value("${opertusmundi.payments.mangopay.client-password:}")
    private String clientPassword;

    protected final AccountRepository accountRepository;
    protected MangoPayApi api;

    protected BaseMangoPayService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    /**
     * This is the URL where users are automatically redirected after 3D secure
     * validation (if activated)
     *
     * @see https://docs.mangopay.com/endpoints/v2.01/payins#e278_create-a-card-direct-payin
     * @see https://docs.mangopay.com/endpoints/v2.01/payins#e1053_create-a-recurring-payin-cit
     */
    @Value("${opertusmundi.payments.mangopay.secure-mode-return-url:}")
    private String secureModeReturnUrl;

    @PostConstruct
    private void init() {
        this.api = new MangoPayApi();

        this.api.getConfig().setBaseUrl(this.baseUrl);
        this.api.getConfig().setClientId(this.clientId);
        this.api.getConfig().setClientPassword(this.clientPassword);
    }

    protected String buildSecureModeReturnUrl(CardDirectPayInCommand command) {
        String baseUrl = this.secureModeReturnUrl;
        if (!baseUrl.endsWith("/")) {
            baseUrl += "/";
        }

        return baseUrl + "webhooks/payins/" + command.getKey().toString();
    }

    /**
     * Create a MANGOPAY PayIn object
     *
     * @param command
     * @return
     */
    protected PayIn createCardDirectPayIn(CardDirectPayInExecutionContext ctx) {
        final var command        = ctx.getCommand();
        final var paymentDetails = new PayInPaymentDetailsCard();

        paymentDetails.setBrowserInfo(command.getBrowserInfo().toMangoPayBrowserInfo());
        paymentDetails.setCardType(CardType.CB_VISA_MASTERCARD);
        paymentDetails.setCardId(command.getCardId());
        paymentDetails.setIpAddress(command.getIpAddress());
        paymentDetails.setStatementDescriptor(ctx.getStatementDescriptor());
        if (command.getShipping() != null) {
            paymentDetails.setShipping(command.getShipping().toMangoPayShipping());
        }

        final PayInExecutionDetailsDirect executionDetails = new PayInExecutionDetailsDirect();
        if (command.getBilling() != null) {
            executionDetails.setBilling(command.getBilling().toMangoPayBilling());
        }
        executionDetails.setCardId(command.getCardId());

        /*
         * Previously, a transaction (PayIn or PreAuthorization) would generally
         * not be subject to strong customer authentication if
         * SecureMode=DEFAULT and the payment amount was inferior to your 3DS
         * limit. Since 1st January 2021, we can no longer guarantee this
         * frictionless payment experience – including on low amount
         * transactions for card verification.
         *
         * See: https://docs.mangopay.com/guide/3ds2-integration
         */
        executionDetails.setSecureMode(SecureMode.FORCE);

        executionDetails.setSecureModeReturnUrl(this.buildSecureModeReturnUrl(command));

        /*
         * This feature is for sandbox testing and will not be available in
         * production. In production, the only change will be that
         * Applied3DSVersion will give the value “V1” before we activate your
         * flows to 3DS2 and the value “V2_1” after activation.
         *
         * https://docs.mangopay.com/guide/3ds2-testing-in-sandbox
         */
        executionDetails.setRequested3DSVersion("V2_1");

        final PayIn result = new PayIn();
        result.setAuthorId(ctx.getCreditedUserId());
        result.setCreditedUserId(ctx.getCreditedUserId());
        result.setCreditedWalletId(ctx.getCreditedWalletId());
        result.setDebitedFunds(new Money(
            CurrencyIso.EUR, ctx.getDebitedFunds().multiply(BigDecimal.valueOf(100L)).intValue()
        ));
        result.setExecutionDetails(executionDetails);
        result.setExecutionType(PayInExecutionType.DIRECT);
        result.setFees(new Money(CurrencyIso.EUR, 0));
        result.setPaymentDetails(paymentDetails);
        result.setTag(command.getKey().toString());
        result.setPaymentType(PayInPaymentType.CARD);

        return result;
    }

    /**
     * Wraps an exception with {@link PaymentException}
     *
     * @param operation
     * @param ex
     * @return
     */
    protected PaymentException wrapException(String operation, Exception ex, Object command, Logger logger) {
        PaymentException pEx         = null;
        final String     commandText = command == null ? "-" : command.toString();


        if (ex instanceof PaymentException) {
            // No action is required for payment exceptions
            pEx = (PaymentException) ex;
        } else if (ex instanceof ResponseException) {
            // Wrap MANGOPAY exceptions with a Payment exception
            final ResponseException rEx     = (ResponseException) ex;
            final String            message = String.format(
                "MANGOPAY operation has failed. [operation=%s, apiMessage=%s, command=[%s]]",
                operation, rEx.getApiMessage(), commandText
            );

            logger.error(message, ex);

            pEx = PaymentException.of(PaymentMessageCode.API_ERROR, message, rEx);
        } else {
            // Default exception handler
            final String message = String.format("[MANGOPAY] %s [%s]", operation, commandText);

            logger.error(message, ex);

            pEx = new PaymentException(PaymentMessageCode.SERVER_ERROR, message, ex);
        }

        return pEx;
    }

    /**
     * Get existing response for an idempotency key
     *
     * See: https://docs.mangopay.com/guide/idempotency-support
     *
     * @param <T>
     * @param idempotencyKey
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    protected <T> T getResponse(String idempotencyKey) throws Exception {
        try {
            final IdempotencyResponse r = this.api.getIdempotencyApi().get(idempotencyKey);

            switch (r.getStatusCode()) {
                case "200" :
                    return (T) r.getResource();
                default :
                    return null;
            }
        } catch (final ResponseException ex) {
            return null;
        } catch (final Exception ex) {
            throw this.wrapException("Get response from idempotency key", ex, idempotencyKey, logger);
        }
    }

    protected AccountEntity getAccount(UUID key) throws PaymentException {
        final AccountEntity account = this.accountRepository.findOneByKey(key).orElse(null);

        if (account == null) {
            throw new PaymentException(String.format("[MANGOPAY] OpertusMundi user [%s] was not found", key));
        }

        return account;
    }

    /**
     * Convert a timestamp numeric value to a {@link ZonedDateTime} instance
     *
     * <p>
     * MANGOPAY returns dates as integer numbers that represent the number of
     * seconds since the Unix Epoch
     *
     * @param timestamp
     * @return
     */
    protected ZonedDateTime timestampToDate(Long timestamp) {
        if (timestamp != null) {
            return ZonedDateTime.ofInstant(Instant.ofEpochSecond(timestamp), ZoneOffset.UTC);
        }
        return null;
    }

    protected void ensureCustomer(CustomerEntity customer, UUID key) throws PaymentException {
        if (customer == null) {
            throw new PaymentException(
                PaymentMessageCode.PLATFORM_CUSTOMER_NOT_FOUND,
                String.format("[MANGOPAY] Customer registration was not found for account with key [%s]", key)
            );
        }
    }

    protected void ensureCustomer(CustomerEntity customer, String id) throws PaymentException {
        if (customer == null) {
            throw new PaymentException(
                PaymentMessageCode.PROVIDER_USER_NOT_FOUND,
                String.format("[MANGOPAY] Customer was not found for MANGOPAY user with resource id [%s]", id)
            );
        }
    }

    protected CustomerDraftEntity resolveCustomerRegistration(AccountEntity account, EnumCustomerType type, UUID key) {
        final CustomerDraftEntity registration = switch (type) {
            case CONSUMER -> account.getProfile().getConsumerRegistration();
            case PROVIDER -> account.getProfile().getProviderRegistration();
            default -> null;
        };

        if (registration != null && registration.getKey().equals(key)) {
            if (registration.getStatus() != EnumCustomerRegistrationStatus.SUBMITTED) {
                throw new PaymentException(String.format(
                    "[MANGOPAY] Invalid registration state [%s] for key [%s]. Expected [SUBMITTED]",
                    registration.getStatus(), key
                ));
            }
            return registration;
        }

        throw new PaymentException(String.format("[MANGOPAY] No active registration found for key [%s]", key));
    }

}
