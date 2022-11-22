package eu.opertusmundi.common.repository;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import eu.opertusmundi.common.domain.ContactFormEntity;
import eu.opertusmundi.common.model.ServiceException;
import eu.opertusmundi.common.model.message.ContactFormCommandDto;
import eu.opertusmundi.common.model.message.ContactFormDto;
import eu.opertusmundi.common.model.message.EnumContactFormStatus;

@Repository
@Transactional(readOnly = true)
public interface ContactFormRepository extends JpaRepository<ContactFormEntity, Integer> {

    @Query("SELECT f FROM ContactForm f WHERE f.key = :key")
    Optional<ContactFormEntity> findOneByKey(UUID key);

    @Query("SELECT f FROM ContactForm f "
         + "WHERE (f.email like :email or :email is null) and "
         + "      (f.status = :status or :status is null) and "
         + "      (cast(:createdAtFrom as org.hibernate.type.TimestampType) is null or f.createdAt >= :createdAtFrom) and "
         + "      (cast(:createdAtTo as org.hibernate.type.TimestampType) is null or f.createdAt <= :createdAtTo)")
    Page<ContactFormEntity> findAll(
        String email, EnumContactFormStatus status, ZonedDateTime createdAtFrom, ZonedDateTime createdAtTo, Pageable pageable
    );
    
    @Query("SELECT count(f) from ContactForm f where status = :status")
    Long countFormsWithStatus(EnumContactFormStatus status);

    @Transactional(readOnly = false)
    default ContactFormDto create(ContactFormCommandDto command) {
        final var e = new ContactFormEntity();

        e.setCompanyName(command.getCompanyName());
        e.setEmail(command.getEmail());
        e.setFirstName(command.getFirstName());
        e.setLastName(command.getLastName());
        e.setMessage(command.getMessage());
        e.setPhoneCountryCode(command.getPhoneCountryCode());
        e.setPhoneNumber(command.getPhoneNumber());
        e.setPrivacyTermsAccepted(true);
        e.setStatus(EnumContactFormStatus.PENDING);
        e.setUpdatedAt(e.getCreatedAt());

        this.saveAndFlush(e);
        return e.toDto();
    }

    @Transactional(readOnly = false)
    default ContactFormDto completeForm(UUID formKey) {
        var e = this.findOneByKey(formKey).orElse(null);
        if (e == null) {
            throw new ServiceException("Form was not found");
        }
        e.setUpdatedAt(ZonedDateTime.now());
        e.setStatus(EnumContactFormStatus.COMPLETED);

        this.saveAndFlush(e);

        return e.toDto(true);
    }

}
