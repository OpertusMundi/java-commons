package eu.opertusmundi.common.service.mangopay;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.mangopay.core.Money;
import com.mangopay.core.enumerations.CurrencyIso;
import com.mangopay.entities.RecurringPayIn;
import com.mangopay.entities.RecurringPayInCIT;
import com.mangopay.entities.RecurringPayInMIT;
import com.mangopay.entities.RecurringPayment;
import com.mangopay.entities.RecurringPaymentExtended;
import com.mangopay.entities.subentities.PayInExecutionDetailsDirect;

import eu.opertusmundi.common.domain.PayInEntity;
import eu.opertusmundi.common.domain.PayInRecurringRegistrationEntity;
import eu.opertusmundi.common.model.order.EnumOrderStatus;
import eu.opertusmundi.common.model.payment.CardDirectPayInExecutionContext;
import eu.opertusmundi.common.model.payment.EnumRecurringPaymentStatus;
import eu.opertusmundi.common.model.payment.EnumTransactionStatus;
import eu.opertusmundi.common.model.payment.PayInDto;
import eu.opertusmundi.common.model.payment.PaymentException;
import eu.opertusmundi.common.model.payment.PaymentMessageCode;
import eu.opertusmundi.common.model.payment.RecurringRegistrationCreateCommand;
import eu.opertusmundi.common.model.payment.RecurringRegistrationDto;
import eu.opertusmundi.common.model.payment.RecurringRegistrationUpdateCommandDto;
import eu.opertusmundi.common.model.payment.RecurringRegistrationUpdateStatusCommand;
import eu.opertusmundi.common.model.payment.consumer.ConsumerCardDirectPayInDto;
import eu.opertusmundi.common.repository.OrderRepository;
import eu.opertusmundi.common.repository.PayInRepository;
import eu.opertusmundi.common.repository.RecurringPaymentRepository;

@Service
@Transactional
public class MangoPayRecurringPaymentService extends BaseMangoPayService implements RecurringPaymentService {

    private static final Logger logger = LoggerFactory.getLogger(MangoPayRecurringPaymentService.class);

    /**
     * This is the URL where users are automatically redirected after 3DS
     * validation (if activated)
     *
     * @see https://docs.mangopay.com/endpoints/v2.01/payins#e278_create-a-card-direct-payin
     * @see https://docs.mangopay.com/endpoints/v2.01/payins#e1053_create-a-recurring-payin-cit
     */
    @Value("${opertusmundi.payments.mangopay.secure-mode-return-url:}")
    private String secureModeReturnUrl;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private RecurringPaymentRepository recurringPaymentRepository;

    @Autowired
    private PayInRepository payInRepository;

    @Override
    public RecurringRegistrationDto initializeRegistration(RecurringRegistrationCreateCommand command) throws PaymentException {
        try {
            // TODO: Enable idempotency key query when MANGOPAY SDK is updated
            // to support the deserialization of RecurringPayment type

            /*
            // Check if this is a retry operation
            RecurringPayment apiResponse = this.<RecurringPayment>getResponse(command.getIdempotencyKey(), RecurringPayment.class);

            // Create a new registration if needed
            if (apiResponse == null) {
                final RecurringPayment paymentRequest = this.createRegistration(command);

                apiResponse = this.api.getPayInApi().createRecurringPayment(command.getIdempotencyKey(), paymentRequest);
            }
            */

            final RecurringPayment paymentRequest = this.createRegistration(command);
            final RecurringPayment apiResponse    = this.api.getPayInApi().createRecurringPayment(command.getIdempotencyKey(), paymentRequest);

            // Update command from service response
            command.setRegistrationId(apiResponse.getId());
            command.setStatus(EnumRecurringPaymentStatus.from(apiResponse.getStatus()));

            // Create database record
            final PayInRecurringRegistrationEntity payment = recurringPaymentRepository.findOneEntityByKey(command.getOrderKey()).orElse(null);

            if (payment != null) {
                return payment.toConsumerDto(true, false);
            } else {
                final RecurringRegistrationDto result = this.recurringPaymentRepository.initialize(command);

                return result;
            }
        } catch(final Exception ex) {
            throw this.wrapException("Create Recurring PayIn Registration", ex);
        }
    }

    @Override
    public RecurringRegistrationDto updateRegistration(RecurringRegistrationUpdateCommandDto command) throws PaymentException {
        throw new PaymentException(PaymentMessageCode.NOT_IMPLEMENTED, "Operation is not implemented");
    }

    @Override
    public RecurringRegistrationDto endRegistration(UUID key) throws PaymentException {
        throw new PaymentException(PaymentMessageCode.NOT_IMPLEMENTED, "Operation is not implemented");
    }

    @Override
    public Optional<RecurringRegistrationDto> getRegistration(UUID key) throws PaymentException {
        return this.recurringPaymentRepository.findOneObjectByKey(key);
    }

    public RecurringPaymentExtended getRegistration(String id) throws Exception {
        return this.api.getPayInApi().getRecurringPayment(id);
    }

    @Override
    public PayInDto createConsumerPayIn(CardDirectPayInExecutionContext ctx) {
        try {
            final var command = ctx.getCommand();

            // Check if this is a retry operation
            RecurringPayIn apiResponse = this.<RecurringPayIn>getResponse(ctx.getIdempotencyKey());

            // Create a new PayIn if needed
            if (apiResponse == null) {
                final RecurringPayInCIT payInRequest = this.createCardDirectPayInCIT(ctx);

                apiResponse = this.api.getPayInApi().createRecurringPayInCIT(ctx.getIdempotencyKey(), payInRequest);
            }

            // Update command with payment information
            final PayInExecutionDetailsDirect executionDetails = (PayInExecutionDetailsDirect) apiResponse.getExecutionDetails();

            ctx.setApplied3dsVersion(executionDetails.getApplied3DSVersion());
            ctx.setPayIn(apiResponse.getId());
            ctx.setRequested3dsVersion(executionDetails.getRequested3DSVersion());
            ctx.setResultCode(apiResponse.getResultCode());
            ctx.setResultMessage(apiResponse.getResultMessage());
            ctx.setStatus(EnumTransactionStatus.from(apiResponse.getStatus()));
            // MANGOPAY returns dates as integer numbers that represent the
            // number of seconds since the Unix Epoch
            ctx.setCreatedOn(ZonedDateTime.ofInstant(Instant.ofEpochSecond(apiResponse.getCreationDate()), ZoneOffset.UTC));
            // For Card Direct PayIns, if no 3-D Secure validation is required,
            // the transaction may be executed immediately
            if (apiResponse.getExecutionDate() != null) {
                ctx.setExecutedOn(ZonedDateTime.ofInstant(Instant.ofEpochSecond(apiResponse.getExecutionDate()), ZoneOffset.UTC));
            }

            // Create database record
            final PayInEntity          payIn  = payInRepository.findOneByPayInId(apiResponse.getId()).orElse(null);
            ConsumerCardDirectPayInDto result = null;
            if (payIn != null) {
                result = (ConsumerCardDirectPayInDto) payIn.toConsumerDto(true);
            } else {
                result = (ConsumerCardDirectPayInDto) this.payInRepository.createCardDirectPayInForOrder(ctx);

                // Link PayIn record to order
                this.orderRepository.setPayIn(command.getKey(), result.getPayIn(), command.getUserKey());

                // Update order status if we have a valid response i.e.
                // 3D-Secure validation was skipped
                if (result.getStatus() != EnumTransactionStatus.CREATED) {
                    switch (result.getStatus()) {
                        case FAILED :
                            this.orderRepository.setStatus(command.getKey(), EnumOrderStatus.CANCELLED);
                            break;

                        case SUCCEEDED :
                            this.orderRepository.setStatus(command.getKey(), EnumOrderStatus.ASSET_REGISTRATION);
                            break;

                        default :
                            throw new PaymentException(
                                PaymentMessageCode.ENUM_MEMBER_NOT_SUPPORTED,
                                String.format("Transaction status [%s] is not supported", this)
                            );
                    }
                }
            }

            // Add client-only information (card alias is never saved in our
            // database)
            result.setAlias(ctx.getCardAlias());

            /*
             * Since we have set SecureMode=FORCE, we expect SecureModeNeeded to
             * be TRUE and a non-null SecureModeRedirectUrl returned by the API
             * call
             *
             * See: https://docs.mangopay.com/guide/3ds2-integration
             */
            result.setSecureModeRedirectURL(executionDetails.getSecureModeRedirectUrl());

            return result;
        } catch(final PaymentException ex) {
            throw ex;
        } catch(final Exception ex) {
            throw this.wrapException("Create CIT PayIn", ex, ctx, logger);
        }
    }

    @Override
    public PayInDto createMerchantPayIn(CardDirectPayInExecutionContext ctx) throws PaymentException {
        Assert.isTrue(ctx.isRecurring(), "Merchant initiated transactions are supported only for recurring payments");
        Assert.hasText(ctx.getRecurringPayinRegistrationId(), "Expected an non-empty recurring registration identifier");

        try {
            // Check if this is a retry operation
            RecurringPayIn apiResponse = this.<RecurringPayIn>getResponse(ctx.getIdempotencyKey());

            // Create a new PayIn if needed
            if (apiResponse == null) {
                final RecurringPayInMIT payInRequest = this.createCardDirectPayInMIT(ctx);

                apiResponse = this.api.getPayInApi().createRecurringPayInMIT(ctx.getIdempotencyKey(), payInRequest);
            }

            // Update command with payment information
            final PayInExecutionDetailsDirect executionDetails = (PayInExecutionDetailsDirect) apiResponse.getExecutionDetails();

            ctx.setApplied3dsVersion(executionDetails.getApplied3DSVersion());
            ctx.setPayIn(apiResponse.getId());
            ctx.setRequested3dsVersion(executionDetails.getRequested3DSVersion());
            ctx.setResultCode(apiResponse.getResultCode());
            ctx.setResultMessage(apiResponse.getResultMessage());
            ctx.setStatus(EnumTransactionStatus.from(apiResponse.getStatus()));
            // MANGOPAY returns dates as integer numbers that represent the
            // number of seconds since the Unix Epoch
            ctx.setCreatedOn(ZonedDateTime.ofInstant(Instant.ofEpochSecond(apiResponse.getCreationDate()), ZoneOffset.UTC));
            // For Card Direct PayIns, if no 3-D Secure validation is required,
            // the transaction may be executed immediately
            if (apiResponse.getExecutionDate() != null) {
                ctx.setExecutedOn(ZonedDateTime.ofInstant(Instant.ofEpochSecond(apiResponse.getExecutionDate()), ZoneOffset.UTC));
            }

            // Create database record
            final PayInEntity          payIn  = payInRepository.findOneByPayInId(apiResponse.getId()).orElse(null);
            ConsumerCardDirectPayInDto result = null;
            if (payIn != null) {
                result = (ConsumerCardDirectPayInDto) payIn.toConsumerDto(true);
            } else {
                result = (ConsumerCardDirectPayInDto) this.payInRepository.createCardDirectPayInForOrder(ctx);

                // Update order status if we have a valid response i.e.
                // 3D-Secure validation was skipped
                if (result.getStatus() != EnumTransactionStatus.SUCCEEDED) {
                    // TODO: Inform user and request a CIT PayIn
                }
            }

            // Add client-only information (card alias is never saved in our
            // database)
            result.setAlias(ctx.getCardAlias());

            return result;
        } catch(final PaymentException ex) {
            throw ex;
        } catch(final Exception ex) {
            throw this.wrapException("Create MIT PayIn", ex, ctx, logger);
        }
    }

    @Override
    public void updateStatus(String  registrationProviderId) {
        try {
            final RecurringPaymentExtended registration = this.api.getPayInApi().getRecurringPayment(registrationProviderId);

            final RecurringRegistrationUpdateStatusCommand command = RecurringRegistrationUpdateStatusCommand.builder()
                .registrationId(registrationProviderId)
                .status(EnumRecurringPaymentStatus.from(registration.getStatus()))
                .build();

            this.recurringPaymentRepository.updateStatus(command);
        } catch (final Exception ex) {
            throw this.wrapException("Update Recurring PayIn Registration status", ex, registrationProviderId, logger);
        }
    }

    private RecurringPayment createRegistration(RecurringRegistrationCreateCommand command) {
        final RecurringPayment payment = new RecurringPayment();

        payment.setAuthorId(command.getAuthorId());
        payment.setCardId(command.getCardId());
        payment.setCreditedUserId(command.getCreditedUserId());
        payment.setCreditedWalletId(command.getCreditedWalletId());
        payment.setFirstTransactionDebitedFunds(new Money(
            CurrencyIso.EUR, command.getFirstTransactionDebitedFunds().multiply(BigDecimal.valueOf(100L)).intValue()
        ));
        payment.setFirstTransactionFees(new Money(CurrencyIso.EUR, 0));
        payment.setFixedNextAmount(true);
        payment.setFractionedPayment(false);
        payment.setFrequency(command.getFrequency().getValue());
        payment.setMigration(command.isMigrate());

        if (command.getBillingAddress() != null) {
            payment.setBilling(command.getBillingAddress().toMangoPayBilling());
        }
        // MANGOPAY expects dates as integer numbers that represent the
        // number of seconds since the Unix Epoch
        if (command.getEndDate() != null) {
            payment.setEndDate(command.getEndDate().toEpochSecond());
        }
        if (command.getShippingAddress() != null) {
            payment.setShipping(command.getShippingAddress().toMangoPayShipping());
        }

        return payment;
    }

    /**
     * Create a MANGOPAY Customer-Initiated-Transaction (CIT) object
     *
     * @param command
     * @return
     */
    private RecurringPayInCIT createCardDirectPayInCIT(CardDirectPayInExecutionContext ctx) {
        final var command = ctx.getCommand();
        final var result  = new RecurringPayInCIT();

        result.setBrowserInfo(command.getBrowserInfo().toMangoPayBrowserInfo());
        result.setIpAddress(command.getIpAddress());
        result.setRecurringPayInRegistrationId(ctx.getRecurringPayinRegistrationId());
        result.setSecureModeReturnURL(this.buildSecureModeReturnUrl(command));
        result.setStatementDescriptor(ctx.getStatementDescriptor());
        result.setTag(command.getKey().toString());

        return result;
    }

    /**
     * Create a MANGOPAY Merchant-Initiated-Transaction (CIT) object
     *
     * @param command
     * @return
     */
    private RecurringPayInMIT createCardDirectPayInMIT(CardDirectPayInExecutionContext ctx) {
        final RecurringPayInMIT result = new RecurringPayInMIT();

        result.setRecurringPayInRegistrationId(ctx.getRecurringPayinRegistrationId());
        result.setStatementDescriptor(ctx.getStatementDescriptor());
        result.setTag(ctx.getCommand().getKey().toString());

        return result;
    }

    private PaymentException wrapException(String operation, Exception ex) {
        return super.wrapException(operation, ex, null, logger);
    }

}
