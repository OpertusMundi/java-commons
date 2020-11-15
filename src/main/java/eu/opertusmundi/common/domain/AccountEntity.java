package eu.opertusmundi.common.domain;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.NaturalId;

import eu.opertusmundi.common.model.EnumActivationStatus;
import eu.opertusmundi.common.model.EnumAuthProvider;
import eu.opertusmundi.common.model.EnumRole;
import eu.opertusmundi.common.model.dto.AccountDto;
import eu.opertusmundi.common.model.dto.AccountProfileUpdateCommandDto;
import eu.opertusmundi.common.model.dto.PublisherDto;
import eu.opertusmundi.common.model.dto.SimplAccountDto;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "Account")
@Table(schema = "web", name = "`account`", uniqueConstraints = {
        @UniqueConstraint(name = "uq_account_key", columnNames = {"`key`"}),
        @UniqueConstraint(name = "uq_account_email", columnNames = {"`email`"}),
    }
)
public class AccountEntity {

    @Id
    @Column(name = "`id`", updatable = false)
    @SequenceGenerator(sequenceName = "web.account_id_seq", name = "account_id_seq", allocationSize = 1)
    @GeneratedValue(generator = "account_id_seq", strategy = GenerationType.SEQUENCE)
    @Getter
    private Integer id;

    @NotNull
    @NaturalId
    @Column(name = "key", updatable = false, columnDefinition = "uuid")
    @Getter
    private final UUID key = UUID.randomUUID();

    @OneToOne(mappedBy = "account", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @Getter
    @Setter
    private AccountProfileEntity profile;

    @Column(name = "`active`")
    @Getter
    @Setter
    private boolean active = true;

    @Column(name = "`blocked`")
    @Getter
    @Setter
    private boolean blocked = false;

    @NotNull
    @Email
    @Column(name = "`email`", nullable = false, length = 120)
    @Getter
    @Setter
    private String email;

    @Column(name = "`email_verified`")
    @Getter
    @Setter
    private boolean emailVerified = false;

    @Column(name = "`email_verified_at`")
    @Getter
    @Setter
    private ZonedDateTime emailVerifiedAt;

    @Column(name = "`firstname`", length = 64)
    @Getter
    @Setter
    private String firstName;

    @Column(name = "`lastname`", length = 64)
    @Getter
    @Setter
    private String lastName;

    @NotNull
    @Pattern(regexp = "[a-z][a-z]")
    @Column(name = "`locale`")
    @Getter
    @Setter
    private String locale;

    @Column(name = "`password`")
    @Getter
    @Setter
    private String password;

    @Column(name = "`registered_at`", nullable = false)
    @Getter
    private final ZonedDateTime registeredAt = ZonedDateTime.now();

    @NotNull
    @Column(name = "`activation_status`", nullable = false)
    @Enumerated(EnumType.STRING)
    @Getter
    @Setter
    private EnumActivationStatus activationStatus;

    @Column(name = "`activation_at`")
    @Getter
    @Setter
    private ZonedDateTime activatedAt;

    @Column(name = "`idp_name`", length = 20)
    @Enumerated(EnumType.STRING)
    @Getter
    @Setter
    private EnumAuthProvider idpName;

    @NotNull
    @Column(name = "`terms_accepted`")
    @Getter
    @Setter
    private boolean termsAccepted = false;

    @Column(name = "`terms_accepted_at`")
    @Getter
    @Setter
    private ZonedDateTime termsAcceptedAt;

    @OneToMany(
        targetEntity = AccountRoleEntity.class,
        mappedBy = "account",
        fetch = FetchType.EAGER,
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private final List<AccountRoleEntity> roles = new ArrayList<>();

    @Transient
    public String getFullName() {
        if (!StringUtils.isBlank(this.firstName)) {
            if (!StringUtils.isBlank(this.lastName)) {
                return this.firstName + " " + this.lastName;
            }
            return this.firstName;
        }
        return "";
    }

    @Transient
    public CustomerDraftEntity getConsumerRegistration() {
        if (this.profile == null) {
            return null;
        }
        return this.profile.getConsumerRegistration();
    }

    @Transient
    public CustomerDraftProfessionalEntity getProviderRegistration() {
        if (this.profile == null) {
            return null;
        }
        return this.profile.getProviderRegistration();
    }

    public AccountEntity() {

    }

    public AccountEntity(int id) {
        this.id = id;
    }

    public AccountEntity(String email) {
        this.email    = email;
    }

    public void setName(String firstname, String lastname) {
        this.firstName = firstname;
        this.lastName  = lastname;
    }

    public String getUserName() {
        return this.email;
    }

    public boolean hasRole(EnumRole role) {
        for (final AccountRoleEntity ar : this.roles) {
            if (role == ar.role) {
                return true;
            }
        }
        return false;
    }

    public void grant(EnumRole role, AccountEntity grantedBy) {
        if (!this.hasRole(role)) {
            this.roles.add(new AccountRoleEntity(this, role, null, grantedBy));
        }
    }

    public void revoke(EnumRole role) {
        AccountRoleEntity target = null;
        for (final AccountRoleEntity ar : this.roles) {
            if (role == ar.role) {
                target = ar;
                break;
            }
        }
        if (target != null) {
            this.roles.remove(target);
        }
    }

    /**
     * Convert to a DTO object
     *
     * @return a new {@link AccountDto} instance
     */
    public AccountDto toDto() {
        final AccountDto a = new AccountDto();

        a.setActivatedAt(this.activatedAt);
        a.setActivationStatus(this.activationStatus);
        a.setActive(this.active);
        a.setBlocked(this.blocked);
        a.setEmail(this.email);
        a.setEmailVerified(this.emailVerified);
        a.setEmailVerifiedAt(this.emailVerifiedAt);
        a.setId(this.id);
        a.setIdpName(this.idpName);
        a.setKey(this.key);
        a.setPassword(this.password);
        a.setRegisteredAt(this.registeredAt);
        a.setRoles(this.roles.stream().map(r -> r.getRole()).collect(Collectors.toSet()));

        if (this.profile != null) {
            a.setProfile(this.profile.toDto());
        }

        return a;
    }

    /**
     * Convert to a publisher DTO object
     *
     * @return a new {@link PublisherDto} instance
     */
    public PublisherDto toPublisherDto() {
        // A provider must have the role ROLE_PROVIDER
        if (!this.hasRole(EnumRole.ROLE_PROVIDER)) {
            return null;
        }


        final PublisherDto publisher = new PublisherDto();

        final CustomerProfessionalEntity provider = this.profile.getProvider();

        publisher.setCity(provider.getHeadquartersAddress().getCity());
        publisher.setCountry(provider.getHeadquartersAddress().getCountry());
        publisher.setJoinedAt(provider.getCreatedAt());
        publisher.setKey(this.key);
        publisher.setLogoImage(provider.getLogoImage());
        publisher.setLogoImageMimeType(provider.getLogoImageMimeType());
        publisher.setName(provider.getName());
        publisher.setRating(provider.getRating());

        if(provider.isEmailVerified()) {
            publisher.setEmail(provider.getEmail());
        }

        return publisher;
    }

    /**
     * Convert to a simple account DTO object
     *
     * @return a new {@link SimplAccountDto} instance
     */
    public SimplAccountDto toSimpleDto() {
        final SimplAccountDto a = new SimplAccountDto();

        a.setKey(this.key);
        a.setUsername(this.email);

        return a;
    }

    /**
     * Update from profile properties
     *
     * @param command The command object
     */
    public void update(AccountProfileUpdateCommandDto command) {
        if (!StringUtils.isBlank(command.getFirstName())) {
            this.firstName = command.getFirstName();
        }
        if (!StringUtils.isBlank(command.getLastName())) {
            this.lastName = command.getLastName();
        }
        if (!StringUtils.isBlank(command.getLocale())) {
            this.locale = command.getLocale();
        }
    }

}
