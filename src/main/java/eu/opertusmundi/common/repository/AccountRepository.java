package eu.opertusmundi.common.repository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.persistence.LockModeType;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.opertusmundi.common.domain.AccountEntity;
import eu.opertusmundi.common.domain.AccountProfileEntity;
import eu.opertusmundi.common.domain.ActivationTokenEntity;
import eu.opertusmundi.common.domain.CustomerDraftEntity;
import eu.opertusmundi.common.domain.CustomerDraftProfessionalEntity;
import eu.opertusmundi.common.domain.CustomerEntity;
import eu.opertusmundi.common.domain.CustomerKycLevelEntity;
import eu.opertusmundi.common.domain.CustomerProfessionalEntity;
import eu.opertusmundi.common.model.EnumAuthProvider;
import eu.opertusmundi.common.model.EnumRole;
import eu.opertusmundi.common.model.account.AccountCommandDto;
import eu.opertusmundi.common.model.account.AccountDto;
import eu.opertusmundi.common.model.account.AccountProfileCommandDto;
import eu.opertusmundi.common.model.account.CustomerCommandDto;
import eu.opertusmundi.common.model.account.EnumActivationStatus;
import eu.opertusmundi.common.model.account.EnumCustomerRegistrationStatus;
import eu.opertusmundi.common.model.account.ProviderProfessionalCommandDto;

@Repository
@Transactional(readOnly = true)
public interface AccountRepository extends JpaRepository<AccountEntity, Integer> {

    @Query("SELECT a FROM Account a LEFT OUTER JOIN FETCH a.profile p WHERE a.key in :keys")
    List<AccountEntity> findAllByKey(@Param("keys") List<UUID> keys);

    @Query("SELECT a FROM Account a "
         + "LEFT OUTER JOIN FETCH a.profile p "
         + "WHERE a.key = :key")
    Optional<AccountEntity> findOneByKey(@Param("key") UUID key);

    @Lock(LockModeType.PESSIMISTIC_READ)
    @Query("SELECT a FROM Account a  "
         + "WHERE a.key = :key")
    Optional<AccountEntity> findAndLockOneByKey(@Param("key") UUID key);

    @Query("SELECT a FROM Account a "
         + "LEFT OUTER JOIN FETCH a.profile p "
         + "WHERE a.email = :email")
    Optional<AccountEntity> findOneByEmail(@Param("email") String email);

    default Optional<AccountEntity> findOneByUsername(String username) {
        return this.findOneByEmail(username);
    }

    @Query("SELECT a FROM Account a "
         + "LEFT OUTER JOIN FETCH a.profile p "
         + "WHERE a.email = :email and a.idpName = :provider")
    Optional<AccountEntity> findOneByEmailAndProvider(@Param("email") String email, @Param("provider") EnumAuthProvider provider);

    @Query("SELECT t FROM ActivationToken t WHERE t.redeemedAt IS NULL AND t.email = :email")
    Optional<ActivationTokenEntity> findActiveActivationTokensForEmail(@Param("email") String email);

    Optional<AccountEntity> findOneByEmailAndIdNot(String email, Integer id);

    @Query("SELECT a FROM Account a "
         + "LEFT OUTER JOIN a.profile p "
         + "LEFT OUTER JOIN p.consumer cr "
         + "LEFT OUTER JOIN p.provider pr "
         + "WHERE      (:email is null or a.email like :email)"
    )
    Page<AccountEntity> findAll(
        @Param("email")String email,
        Pageable pageable
    );
    @Query("SELECT a FROM Account a "
         + "LEFT OUTER JOIN a.profile p "
         + "LEFT OUTER JOIN p.consumer cr "
         + "WHERE  (:email is null or a.email like :email) and "
         + "       (p.consumer is not null) "
    )
    Page<AccountEntity> findAllConsumers(
        @Param("email")String email,
        Pageable pageable
    );

    @Query("SELECT a FROM Account a "
         + "LEFT OUTER JOIN a.profile p "
         + "LEFT OUTER JOIN p.provider pr "
         + "WHERE  (:email is null or a.email like :email) and "
         + "       (p.provider is not null) "
    )
    Page<AccountEntity> findAllProviders(
        @Param("email")String email,
        Pageable pageable
    );

    @Modifying
    @Transactional(readOnly = false)
    @Query("UPDATE Account a SET a.processDefinition = :processDefinition, a.processInstance = :processInstance WHERE a.id = :id")
    void setRegistrationWorkflowInstance(
        @Param("id")                Integer id,
        @Param("processDefinition") String processDefinition,
        @Param("processInstance")   String processInstance
    );

    @Transactional(readOnly = false)
    default AccountDto updateProfile(AccountProfileCommandDto command) {
        // Get account
        final AccountEntity account = this.findById(command.getId()).orElse(null);

        // Initialize profile if not already exists
        if (account.getProfile() == null) {
            account.setProfile(new AccountProfileEntity());
        }

        final AccountProfileEntity profile = account.getProfile();

        // Update account
        account.update(command);

        // Update profile
        profile.update(command);

        this.save(account);

        return account.toDto();
    }

    @Transactional(readOnly = false)
    default AccountDto create(AccountCommandDto command) {
        final AccountEntity        account = new AccountEntity();
        final AccountProfileEntity profile = new AccountProfileEntity();

        final ZonedDateTime createdAt = account.getRegisteredAt();

        // Set account
        account.setActivationStatus(EnumActivationStatus.PENDING);
        account.setActive(true);
        account.setBlocked(false);
        account.setEmail(command.getEmail());
        account.setEmailVerified(false);
        account.setFirstName(command.getProfile().getFirstName());
        account.setIdpName(command.getIdpName());
        account.setLastName(command.getProfile().getLastName());
        if (StringUtils.isBlank(command.getProfile().getLocale())) {
            account.setLocale("en");
        } else {
            account.setLocale(command.getProfile().getLocale());
        }
        account.setProfile(profile);
        account.setTermsAccepted(true);
        account.setTermsAcceptedAt(createdAt);

        if (!StringUtils.isBlank(command.getPassword())) {
            final PasswordEncoder encoder = new BCryptPasswordEncoder();

            account.setPassword(encoder.encode(command.getPassword()));
        }

        // Set profile
        profile.setAccount(account);
        profile.setCreatedAt(createdAt);
        profile.setImage(command.getProfile().getImage());
        profile.setImageMimeType(command.getProfile().getImageMimeType());
        profile.setMobile(command.getProfile().getMobile());
        profile.setModifiedAt(createdAt);
        profile.setPhone(command.getProfile().getPhone());

        // Grant the default role
        if (!account.hasRole(EnumRole.ROLE_USER)) {
            account.grant(EnumRole.ROLE_USER, null);
        }

        this.saveAndFlush(account);

        return account.toDto();
    }

    default void cancelAccountRegistration(UUID key) {
        final AccountEntity e = this.findOneByKey(key).orElse(null);

        Assert.notNull(e, "Expected a non-null account");
        Assert.isTrue(e.getActivationStatus() == EnumActivationStatus.PENDING, "Expected account status to be PENDING");

        this.delete(e);
    }
    
    @Transactional(readOnly = false)
    default AccountDto updateConsumerRegistration(CustomerCommandDto command)  throws IllegalArgumentException {
        return this.updateConsumerRegistration(command, EnumCustomerRegistrationStatus.DRAFT);
    }

    @Transactional(readOnly = false)
    default AccountDto submitConsumerRegistration(CustomerCommandDto command)  throws IllegalArgumentException {
        return this.updateConsumerRegistration(command, EnumCustomerRegistrationStatus.SUBMITTED);
    }

    @Transactional(readOnly = false)
    default AccountDto updateConsumerRegistration(
        CustomerCommandDto command, EnumCustomerRegistrationStatus status
    ) throws IllegalArgumentException {
        final AccountEntity        account = this.findById(command.getUserId()).orElse(null);
        final AccountProfileEntity profile = account.getProfile();

        // Create/Update consumer draft
        CustomerDraftEntity registration = profile.getConsumerRegistration();

        if (registration == null || registration.isProcessed()) {
            // Create new registration
            registration = CustomerDraftEntity.from(profile.getConsumer(), command);
            registration.setAccount(account);

            profile.setConsumerRegistration(registration);
        } else {
            // The registration must be already in DRAFT state
            if (registration.getStatus() != EnumCustomerRegistrationStatus.DRAFT) {
                throw new IllegalArgumentException("Expected current status to be [DRAFT]");
            }

            // If customer type has changed, update registration object type
            if (registration.getType() != command.getType()) {
                // Cancel exiting registration
                registration.setStatus(EnumCustomerRegistrationStatus.CANCELLED);

                // Create new registration
                registration = CustomerDraftEntity.from(profile.getConsumer(), command);
                registration.setAccount(account);

                profile.setConsumerRegistration(registration);
            } else {
                registration.update(command);
            }
        }

        // Update registration status
        registration.setStatus(status);

        this.save(account);

        return account.toDto();
    }

    @Transactional(readOnly = false)
    default AccountDto cancelConsumerRegistration(UUID userKey) throws IllegalArgumentException {
        final AccountEntity        account      = this.findOneByKey(userKey).orElse(null);
        final AccountProfileEntity profile      = account.getProfile();
        final CustomerDraftEntity  registration = profile.getConsumerRegistration();

        // A registration must exist and have status [DRAFT]
        if (registration == null || registration.getStatus() != EnumCustomerRegistrationStatus.DRAFT) {
            throw new IllegalArgumentException("Expected a non-null registration with status [DRAFT]");
        }

        // Update registration
        registration.setStatus(EnumCustomerRegistrationStatus.CANCELLED);
        registration.setModifiedAt(ZonedDateTime.now());

        // Update profile
        profile.setConsumerRegistration(null);

        this.save(account);

        return account.toDto();
    }

    @Transactional(readOnly = false)
    default AccountDto completeConsumerRegistration(UUID userKey) throws IllegalArgumentException {
        // Get account
        final AccountEntity        account      = this.findOneByKey(userKey).orElse(null);
        final AccountProfileEntity profile      = account.getProfile();
        final CustomerDraftEntity  registration = profile.getConsumerRegistration();

        // A registration must exist and have status [SUBMITTED]
        if (registration == null || registration.getStatus() != EnumCustomerRegistrationStatus.SUBMITTED) {
            throw new IllegalArgumentException("Expected a non-null registration with status [SUBMITTED]");
        }

        // Create/Update consumer
        if (profile.getConsumer() == null) {
            final CustomerEntity consumer = CustomerEntity.from(registration);

            consumer.setAccount(account);
            consumer.setDraftKey(registration.getKey());
            consumer.setTermsAccepted(true);
            consumer.setTermsAcceptedAt(consumer.getCreatedAt());

            // Initialize KYC level history
            final CustomerKycLevelEntity level = new CustomerKycLevelEntity();
            level.setCustomer(consumer);
            level.setLevel(consumer.getKycLevel());
            level.setUpdatedOn(consumer.getCreatedAt());
            consumer.getLevelHistory().add(level);

            profile.setConsumer(consumer);

            // Add ROLE_CONSUMER to the account
            account.grant(EnumRole.ROLE_CONSUMER, null);
        } else {
            profile.getConsumer().update(registration);
        }

        // Update registration
        registration.setStatus(EnumCustomerRegistrationStatus.COMPLETED);
        profile.setConsumerRegistration(null);

        this.save(account);

        return account.toDto();
    }

    @Transactional(readOnly = false)
    default AccountDto updateProviderRegistration(ProviderProfessionalCommandDto command) throws IllegalArgumentException {
        return this.updateProviderRegistration(command, EnumCustomerRegistrationStatus.DRAFT);
    }

    @Transactional(readOnly = false)
    default AccountDto submitProviderRegistration(ProviderProfessionalCommandDto command) throws IllegalArgumentException {
        return this.updateProviderRegistration(command, EnumCustomerRegistrationStatus.SUBMITTED);
    }

    @Transactional(readOnly = false)
    default AccountDto updateProviderRegistration(
        ProviderProfessionalCommandDto command, EnumCustomerRegistrationStatus status
    ) throws IllegalArgumentException {
        // Get account
        final AccountEntity account = this.findById(command.getUserId()).orElse(null);

        // Initialize profile if not already exists
        if (account.getProfile() == null) {
            account.setProfile(new AccountProfileEntity());
        }

        final AccountProfileEntity profile = account.getProfile();

        // Create/Update provider draft
        CustomerDraftProfessionalEntity registration = profile.getProviderRegistration();

        if (registration == null || registration.isProcessed()) {
            // Create new registration
            registration = (CustomerDraftProfessionalEntity) CustomerDraftEntity.from(profile.getProvider(), command);
            registration.setAccount(account);

            profile.setProviderRegistration(registration);
        } else {
            // The registration must be already in DRAFT state
            if (registration.getStatus() != EnumCustomerRegistrationStatus.DRAFT) {
                throw new IllegalArgumentException("Expected current status to be [DRAFT]");
            }

            registration.update(command);
        }

        // Update registration status
        registration.setStatus(status);

        this.save(account);

        return account.toDto();
    }

    @Transactional(readOnly = false)
    default AccountDto cancelProviderRegistration(UUID userKey) throws IllegalArgumentException {
        final AccountEntity        account      = this.findOneByKey(userKey).orElse(null);
        final AccountProfileEntity profile      = account.getProfile();
        final CustomerDraftEntity  registration = profile.getProviderRegistration();

        // A registration must exist and have status [DRAFT]
        if (registration == null || registration.getStatus() != EnumCustomerRegistrationStatus.DRAFT) {
            throw new IllegalArgumentException("Expected a non-null registration with status [DRAFT]");
        }

        // Update registration
        registration.setStatus(EnumCustomerRegistrationStatus.CANCELLED);
        registration.setModifiedAt(ZonedDateTime.now());

        // Update profile
        profile.setProviderRegistration(null);

        this.save(account);

        return account.toDto();
    }

    @Transactional(readOnly = false)
    default AccountDto completeProviderRegistration(UUID userKey, Integer pidServiceUserId) throws IllegalArgumentException {
        final AccountEntity                   account      = this.findOneByKey(userKey).orElse(null);
        final AccountProfileEntity            profile      = account.getProfile();
        final CustomerDraftProfessionalEntity registration = profile.getProviderRegistration();
        final ZonedDateTime                   now          = ZonedDateTime.now();

        // A registration must exist and have status [SUBMITTED]
        if (registration == null || registration.getStatus() != EnumCustomerRegistrationStatus.SUBMITTED) {
            throw new IllegalArgumentException("Expected a non-null registration with status [SUBMITTED]");
        }

        // Create/Update provider
        if (profile.getProvider() == null) {
            final CustomerProfessionalEntity provider = CustomerProfessionalEntity.from(registration);

            provider.setAccount(account);
            provider.setCreatedAt(now);
            provider.setModifiedAt(now);
            provider.setPidServiceUserId(pidServiceUserId);
            provider.setTermsAccepted(true);
            provider.setTermsAcceptedAt(now);

            // Initialize KYC level history
            final CustomerKycLevelEntity level = new CustomerKycLevelEntity();
            level.setCustomer(provider);
            level.setLevel(provider.getKycLevel());
            level.setUpdatedOn(provider.getCreatedAt());
            provider.getLevelHistory().add(level);

            profile.setProvider(provider);

            // Add ROLE_PROVIDER to the account
            account.grant(EnumRole.ROLE_PROVIDER, null);
        } else {
            profile.getProvider().update(registration);
        }

        // Update registration
        registration.setStatus(EnumCustomerRegistrationStatus.COMPLETED);
        profile.setProviderRegistration(null);

        this.save(account);

        return account.toDto();
    }

}
