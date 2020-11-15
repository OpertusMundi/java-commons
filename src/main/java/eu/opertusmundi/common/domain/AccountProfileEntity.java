package eu.opertusmundi.common.domain;

import java.time.ZonedDateTime;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Type;

import eu.opertusmundi.common.model.dto.AccountProfileDto;
import eu.opertusmundi.common.model.dto.AccountProfileUpdateCommandDto;
import eu.opertusmundi.common.model.dto.CustomerDraftDto;
import eu.opertusmundi.common.model.dto.CustomerDraftProfessionalDto;
import eu.opertusmundi.common.model.dto.CustomerDto;
import eu.opertusmundi.common.model.dto.CustomerProfessionalDto;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "AccountProfile")
@Table(schema = "web", name = "`account_profile`")
public class AccountProfileEntity {

    @Id
    @Column(name = "`id`", updatable = false)
    @SequenceGenerator(sequenceName = "web.account_profile_id_seq", name = "account_profile_id_seq", allocationSize = 1)
    @GeneratedValue(generator = "account_profile_id_seq", strategy = GenerationType.SEQUENCE)
    @Getter
    int id;

    @NotNull
    @OneToOne(
        optional = false, fetch = FetchType.LAZY
    )
    @JoinColumn(name = "`account`", foreignKey = @ForeignKey(name = "fk_account_profile_account"))
    @Getter
    @Setter
    private AccountEntity account;

    @OneToOne(
        optional = true, fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = false
    )
    @JoinColumn(name = "`consumer`", foreignKey = @ForeignKey(name = "fk_account_profile_consumer"))
    @Getter
    @Setter
    private CustomerEntity consumer;

    @OneToOne(
        optional = true, fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = false
    )
    @JoinColumn(name = "`provider`", foreignKey = @ForeignKey(name = "fk_account_profile_provider"))
    @Getter
    @Setter
    private CustomerProfessionalEntity provider;

    @OneToOne(
        optional = true, fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = false
    )
    @JoinColumn(name = "`consumer_registration`", foreignKey = @ForeignKey(name = "fk_account_profile_consumer_registration"))
    @Getter
    @Setter
    private CustomerDraftEntity consumerRegistration;

    @OneToOne(
        optional = true, fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = false
    )
    @JoinColumn(name = "`provider_registration`", foreignKey = @ForeignKey(name = "fk_account_profile_provider_registartion"))
    @Getter
    @Setter
    private CustomerDraftProfessionalEntity providerRegistration;

    @Column(name = "image_binary")
    @Type(type = "org.hibernate.type.BinaryType")
    @Getter
    @Setter
    private byte[] image;

    @Column(name = "image_mime_type")
    @Getter
    @Setter
    private String imageMimeType;

    @Column(name = "`phone`", length = 15)
    @Getter
    @Setter
    private String phone;

    @Column(name = "`mobile`", length = 15)
    @Getter
    @Setter
    private String mobile;

    @Column(name = "`created_at`", updatable = false)
    @Getter
    @Setter
    private ZonedDateTime createdAt;

    @Column(name = "`modified_at`")
    @Getter
    @Setter
    private ZonedDateTime modifiedAt;

    /**
     * Update from a command DTO object
     *
     * @param command The command object
     */
    public void update(AccountProfileUpdateCommandDto command) {
        this.phone         = command.getPhone();
        this.mobile        = command.getMobile();
        this.image         = command.getImage();
        this.imageMimeType = command.getImageMimeType();
    }

    /**
     * Convert to a DTO object
     *
     * @return a new {@link AccountProfileDto} instance
     */
    public AccountProfileDto toDto() {
        final AccountProfileDto profile = new AccountProfileDto();

        // Set provider data
        if (this.provider != null) {
            final CustomerProfessionalDto p = this.provider.toDto();

            profile.getProvider().setCurrent(p);
        }

        if (this.providerRegistration != null && !this.getProviderRegistration().isProcessed()) {
            final CustomerDraftProfessionalDto p = this.providerRegistration.toDto();

            profile.getProvider().setDraft(p);
        }

        // Set consumer data
        if (this.consumer != null) {
            final CustomerDto c = this.consumer.toDto();

            profile.getConsumer().setCurrent(c);
        }

        if (this.consumerRegistration != null) {
            final CustomerDraftDto c = this.consumerRegistration.toDto();

            profile.getConsumer().setDraft(c);
        }

        // Set profile data
        profile.setCreatedAt(this.createdAt);
        profile.setFirstName(this.account.getFirstName());
        profile.setImage(this.image);
        profile.setImageMimeType(this.imageMimeType);
        profile.setLastName(this.account.getLastName());
        profile.setLocale(this.account.getLocale());
        profile.setMobile(this.mobile);
        profile.setModifiedAt(this.modifiedAt);
        profile.setPhone(this.phone);

        return profile;
    }

}
