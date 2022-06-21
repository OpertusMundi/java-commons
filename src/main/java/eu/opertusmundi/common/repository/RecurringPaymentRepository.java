package eu.opertusmundi.common.repository;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.mangopay.core.enumerations.CurrencyIso;

import eu.opertusmundi.common.domain.BillingAddressEmbeddable;
import eu.opertusmundi.common.domain.PayInRecurringRegistrationEntity;
import eu.opertusmundi.common.domain.PayInRecurringRegistrationStatusEntity;
import eu.opertusmundi.common.domain.ShippingAddressEmbeddable;
import eu.opertusmundi.common.model.payment.PaymentException;
import eu.opertusmundi.common.model.payment.RecurringRegistrationCreateCommand;
import eu.opertusmundi.common.model.payment.RecurringRegistrationDto;
import eu.opertusmundi.common.model.payment.RecurringRegistrationUpdateStatusCommand;
import io.jsonwebtoken.lang.Assert;

@Repository
@Transactional(readOnly = true)
public interface RecurringPaymentRepository extends JpaRepository<PayInRecurringRegistrationEntity, Integer> {

    @Query("SELECT r FROM PayInRecurringRegistration r WHERE r.key = :key")
    Optional<PayInRecurringRegistrationEntity> findOneEntityByKey(UUID key);

    @Query("SELECT r FROM PayInRecurringRegistration r WHERE r.providerRegistration = :id")
    Optional<PayInRecurringRegistrationEntity> findOneEntityByProviderId(String id);

    default Optional<RecurringRegistrationDto> findOneObjectByKey(UUID key) {
        return this.findOneEntityByKey(key).map(o -> o.toHelpdeskDto(true, true));
    }

    @Transactional(readOnly = false)
    default RecurringRegistrationDto initialize(RecurringRegistrationCreateCommand command) throws IllegalArgumentException {
        Assert.notNull(command, "Expected a non-null command");

        final PayInRecurringRegistrationEntity registration = new PayInRecurringRegistrationEntity();

        registration.setCreatedOn(command.getCreatedOn());
        registration.setCurrency(CurrencyIso.EUR.toString());
        registration.setEndDate(command.getEndDate());
        registration.setFirstTransactionDebitedFunds(command.getFirstTransactionDebitedFunds());
        registration.setFrequency(command.getFrequency());
        registration.setKey(command.getOrderKey());
        registration.setNextTransactionDebitedFunds(command.getFirstTransactionDebitedFunds());
        registration.setProviderCard(command.getCardId());
        registration.setProviderRegistration(command.getRegistrationId());
        registration.setStatus(command.getStatus());
        registration.setStatusUpdatedOn(command.getCreatedOn());

        if (command.getBillingAddress() != null) {
            registration.setBillingAddress(BillingAddressEmbeddable.from(command.getBillingAddress()));
        }
        if (command.getShippingAddress() != null) {
            registration.setShippingAddress(ShippingAddressEmbeddable.from(command.getShippingAddress()));
        }

        final PayInRecurringRegistrationStatusEntity status = new PayInRecurringRegistrationStatusEntity();
        status.setRegistration(registration);
        status.setStatus(registration.getStatus());
        status.setStatusUpdatedOn(registration.getStatusUpdatedOn());

        registration.getStatusHistory().add(status);

        this.saveAndFlush(registration);

        return registration.toConsumerDto(true, false);
    }

    @Transactional(readOnly = false)
    default RecurringRegistrationDto updateStatus(RecurringRegistrationUpdateStatusCommand command) throws PaymentException {
        final PayInRecurringRegistrationEntity registration = this.findOneEntityByProviderId(command.getRegistrationId()).orElse(null);

        // Update only on status changes
        if (registration.getStatus() == command.getStatus()) {
            return registration.toHelpdeskDto(true, false);
        }

        registration.setStatus(command.getStatus());
        registration.setStatusUpdatedOn(ZonedDateTime.now());

        // Create status history record
        final PayInRecurringRegistrationStatusEntity status = new PayInRecurringRegistrationStatusEntity();
        status.setRegistration(registration);
        status.setStatus(command.getStatus());
        status.setStatusUpdatedOn(ZonedDateTime.now());

        registration.getStatusHistory().add(status);

        this.saveAndFlush(registration);

        return registration.toHelpdeskDto(true, false);
    }

}
