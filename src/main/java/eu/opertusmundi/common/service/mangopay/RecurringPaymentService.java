package eu.opertusmundi.common.service.mangopay;

import java.util.Optional;
import java.util.UUID;

import eu.opertusmundi.common.model.payment.CardDirectPayInCommand;
import eu.opertusmundi.common.model.payment.PayInDto;
import eu.opertusmundi.common.model.payment.PaymentException;
import eu.opertusmundi.common.model.payment.RecurringRegistrationCreateCommand;
import eu.opertusmundi.common.model.payment.RecurringRegistrationDto;
import eu.opertusmundi.common.model.payment.RecurringRegistrationUpdateCommandDto;

public interface RecurringPaymentService {

    /**
     * Initializes a new recurring PayIn registration
     *
     * If {@link RecurringRegistrationCreateCommand#isMigrate()} returns
     * {@code true}, getter
     * {@link RecurringRegistrationCreateCommand#getMigrateRegistration()}
     * should return the MANGOPAY identifier of the registration being migrated.
     *
     * @param command
     * @return
     * @throws Exception
     */
    RecurringRegistrationDto initializeRegistration(RecurringRegistrationCreateCommand command) throws PaymentException;

    /**
     * Updates an existing recurring PayIn registration
     *
     * @param command
     * @return
     * @throws PaymentException
     */
    RecurringRegistrationDto updateRegistration(RecurringRegistrationUpdateCommandDto command) throws PaymentException;

    /**
     * Ends an existing recurring PayIn registration
     *
     * @param key
     * @return
     * @throws PaymentException
     */
    RecurringRegistrationDto endRegistration(UUID key) throws PaymentException;

    /**
     * Gets an existing recurring PayIn registration
     *
     * @param key
     * @return
     * @throws PaymentException
     */
    Optional<RecurringRegistrationDto> getRegistration(UUID key) throws PaymentException;

    /**
     * Create a customer initiated transaction (CIT) for a recurring
     * registration
     *
     * @param command
     * @return
     * @throws PaymentException
     */
    PayInDto createConsumerPayIn(CardDirectPayInCommand command) throws PaymentException;

    /**
     * Create a merchant initiated transaction (MIT) for a recurring
     * registration
     *
     * @param command
     * @return
     * @throws PaymentException
     */
    PayInDto createMerchantPayIn(CardDirectPayInCommand command) throws PaymentException;

    /**
     * Update registration status
     *
     * @param registrationProviderId
     */
    void updateStatus(String registrationProviderId);

}
