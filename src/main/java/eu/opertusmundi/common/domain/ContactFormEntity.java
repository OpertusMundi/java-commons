package eu.opertusmundi.common.domain;

import java.time.ZonedDateTime;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.NaturalId;

import eu.opertusmundi.common.model.message.ContactFormDto;
import eu.opertusmundi.common.model.message.EnumContactFormStatus;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "ContactForm")
@Table(schema = "messaging", name = "`contact_form`")
@Getter
@Setter
public class ContactFormEntity {

    @Id
    @Column(name = "`id`", updatable = false)
    @SequenceGenerator(sequenceName = "messaging.contact_form_id_seq", name = "contact_form_id_seq", allocationSize = 1)
    @GeneratedValue(generator = "contact_form_id_seq", strategy = GenerationType.SEQUENCE)
    @Setter(AccessLevel.PRIVATE)
    private Integer id;

    @NotNull
    @NaturalId
    @Column(name = "key", updatable = false, columnDefinition = "uuid")
    @Setter(AccessLevel.PRIVATE)
    private UUID key = UUID.randomUUID();

    @Column(name = "`company_name`", length = 64)
    private String companyName;

    @Column(name = "`firstname`", length = 64)
    private String firstName;

    @Column(name = "`lastname`", length = 64)
    private String lastName;

    @NotNull
    @Email
    @Column(name = "`email`", nullable = false, length = 120)
    private String email;

    @Column(name = "`phone_country_code`", length = 4)
    private String phoneCountryCode;

    @Column(name = "`phone_number`", length = 14)
    private String phoneNumber;

    @NotNull
    @Column(name = "`message`")
    private String message;

    @NotNull
    @Column(name = "`created_at`", nullable = false, updatable = false)
    @Setter(AccessLevel.PRIVATE)
    private ZonedDateTime createdAt = ZonedDateTime.now();

    @NotNull
    @Column(name = "`updated_at`", nullable = false)
    private ZonedDateTime updatedAt;

    @NotNull
    @Column(name = "`status`", nullable = false)
    @Enumerated(EnumType.STRING)
    private EnumContactFormStatus status;

    @NotNull
    @Column(name = "`privacy_terms_accepted`")
    private boolean privacyTermsAccepted = false;

    @Column(name = "`registration_process_definition`")
    private String processDefinition;

    @Column(name = "`registration_process_instance`")
    private String processInstance;

    public ContactFormDto toDto() {
        return this.toDto(false);
    }

    public ContactFormDto toDto(boolean includeHelpdeskData) {
        final ContactFormDto f = new ContactFormDto();

        f.setCompanyName(companyName);
        f.setCreatedAt(createdAt);
        f.setEmail(email);
        f.setFirstName(firstName);
        f.setId(id);
        f.setKey(key);
        f.setLastName(lastName);
        f.setMessage(message);
        f.setPhoneCountryCode(phoneCountryCode);
        f.setPhoneNumber(phoneNumber);
        f.setPrivacyTermsAccepted(privacyTermsAccepted);
        f.setStatus(status);
        f.setUpdatedAt(updatedAt);

        if (includeHelpdeskData) {
            f.setProcessDefinition(processDefinition);
            f.setProcessInstance(processInstance);
        }

        return f;
    }

}
