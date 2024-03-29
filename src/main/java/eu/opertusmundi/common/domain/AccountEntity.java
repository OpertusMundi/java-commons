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
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
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

import eu.opertusmundi.common.model.EnumAccountType;
import eu.opertusmundi.common.model.EnumAuthProvider;
import eu.opertusmundi.common.model.EnumRole;
import eu.opertusmundi.common.model.account.AccountDto;
import eu.opertusmundi.common.model.account.AccountProfileCommandDto;
import eu.opertusmundi.common.model.account.EnumAccountActiveTask;
import eu.opertusmundi.common.model.account.EnumActivationStatus;
import eu.opertusmundi.common.model.account.EnumCustomerType;
import eu.opertusmundi.common.model.account.SimpleAccountDto;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "Account")
@Table(
    schema = "web",
    name = "`account`",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_account_key", columnNames = {"`key`"}),
        @UniqueConstraint(name = "uq_account_email", columnNames = {"`email`"}),
    }
)
@Getter
@Setter
public class AccountEntity {

    @Id
    @Column(name = "`id`", updatable = false)
    @SequenceGenerator(sequenceName = "web.account_id_seq", name = "account_id_seq", allocationSize = 1)
    @GeneratedValue(generator = "account_id_seq", strategy = GenerationType.SEQUENCE)
    @Setter(AccessLevel.PRIVATE)
    private Integer id;

    @NotNull
    @NaturalId
    @Column(name = "key", updatable = false, columnDefinition = "uuid")
    @Setter(AccessLevel.PRIVATE)
    private UUID key = UUID.randomUUID();

    @NotNull
    @Column(name = "`type`", nullable = false)
    @Enumerated(EnumType.STRING)
    private EnumAccountType type;

    @OneToOne(
        optional = true, fetch = FetchType.LAZY
    )
    @JoinColumn(name = "`parent`", foreignKey = @ForeignKey(name = "fk_account_parent_account"))
    private AccountEntity parent;

    /**
     * Account registration workflow definition
     */
    @Column(name = "`registration_process_definition`")
    private String processDefinition;

    /**
     * Account registration workflow process instance
     */
    @Column(name = "`registration_process_instance`")
    private String processInstance;

    @OneToOne(mappedBy = "account", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private AccountProfileEntity profile;

    @Column(name = "`active`")
    private boolean active = true;

    @Column(name = "`blocked`")
    private boolean blocked = false;

    @NotNull
    @Email
    @Column(name = "`email`", nullable = false, length = 120)
    private String email;

    @Column(name = "`email_verified`")
    private boolean emailVerified = false;

    @Column(name = "`email_verified_at`")
    private ZonedDateTime emailVerifiedAt;

    @Column(name = "`firstname`", length = 64)
    private String firstName;

    @Column(name = "`lastname`", length = 64)
    private String lastName;

    @NotNull
    @Pattern(regexp = "[a-z][a-z]")
    @Column(name = "`locale`")
    private String locale;

    @Column(name = "`password`")
    private String password;

    @Column(name = "`registered_at`", nullable = false)
    @Setter(AccessLevel.PRIVATE)
    private ZonedDateTime registeredAt = ZonedDateTime.now();

    @NotNull
    @Column(name = "`activation_status`", nullable = false)
    @Enumerated(EnumType.STRING)
    private EnumActivationStatus activationStatus;

    @Column(name = "`activation_at`")
    private ZonedDateTime activatedAt;

    @Column(name = "`idp_name`", length = 20)
    @Enumerated(EnumType.STRING)
    private EnumAuthProvider idpName;

    @NotNull
    @Column(name = "`terms_accepted`")
    private boolean termsAccepted = false;

    @Column(name = "`terms_accepted_at`")
    private ZonedDateTime termsAcceptedAt;

    @NotNull
    @Column(name = "`active_task`")
    @Enumerated(EnumType.STRING)
    private EnumAccountActiveTask activeTask = EnumAccountActiveTask.NONE;

    @OneToMany(
        targetEntity = AccountRoleEntity.class,
        mappedBy = "account",
        fetch = FetchType.LAZY,
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    @Setter(AccessLevel.PRIVATE)
    private List<AccountRoleEntity> roles = new ArrayList<>();

    @Transient
    public UUID getParentKey() {
        return this.getParent() == null ? this.key : this.getParent().getKey();
    }

    @Transient
    public String getFullName() {
        return String.format("%s %s", this.firstName, this.lastName).trim();
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

    public void revokeAll() {
        this.roles.clear();
    }

    public AccountDto toDto() {
        return this.toDto(false);
    }

    /**
     * Convert to a DTO object
     *
     * @param includeHelpdeskData
     * @return a new {@link AccountDto} instance
     */
    public AccountDto toDto(boolean includeHelpdeskData) {
        final AccountDto a = new AccountDto();

        a.setActivatedAt(this.activatedAt);
        a.setActivationStatus(this.activationStatus);
        a.setActive(this.active);
        a.setActiveTask(this.activeTask);
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
            a.setProfile(this.profile.toDto(includeHelpdeskData));
        }
        if (this.getParent() != null) {
            a.setParentId(this.parent.getId());
            a.setParentKey(this.parent.getKey());
        }

        if (includeHelpdeskData) {
            a.setProcessDefinition(this.processDefinition);
            a.setProcessInstance(this.processInstance);
            a.setType(this.type);
            if (this.getParent() != null) {
                a.setParent(this.getParent().toDto(includeHelpdeskData));
            }
        }

        return a;
    }

    /**
     * Convert to a simple account DTO object
     *
     * @return a new {@link SimpleAccountDto} instance
     */
    public SimpleAccountDto toSimpleDto() {
        final SimpleAccountDto a = new SimpleAccountDto();

        a.setKey(this.key);
        a.setType(this.type);
        a.setUsername(this.email);

        return a;
    }

    /**
     * Update from profile properties
     *
     * @param command The command object
     */
    public void update(AccountProfileCommandDto command) {
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

    public CustomerEntity getCustomer(EnumCustomerType type) {
        switch (type) {
            case CONSUMER :
                return this.getConsumer();
            case PROVIDER :
                return this.getProvider();
            default :
                return null;
        }
    }

    public CustomerEntity getConsumer() {
        return this.profile != null ? this.profile.getConsumer() : null;
    }

    public CustomerProfessionalEntity getProvider() {
        return this.profile != null ? this.profile.getProvider() : null;
    }

    @Transient
    public String getCountry() {
        final CustomerEntity customer = this.getConsumer();
        if (customer == null) {
            return null;
        }
        switch (customer.getType()) {
            case INDIVIDUAL :
                return ((CustomerIndividualEntity) customer).getCountryOfResidence();
            case PROFESSIONAL :
                return ((CustomerProfessionalEntity) customer).getHeadquartersAddress().getCountry();
            default :
                return null;
        }
    }

}
