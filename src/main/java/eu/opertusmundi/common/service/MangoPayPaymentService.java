package eu.opertusmundi.common.service;

import java.util.ArrayList;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.mangopay.MangoPayApi;
import com.mangopay.core.Address;
import com.mangopay.core.ResponseException;
import com.mangopay.core.enumerations.BankAccountType;
import com.mangopay.core.enumerations.CountryIso;
import com.mangopay.core.enumerations.CurrencyIso;
import com.mangopay.core.enumerations.FundsType;
import com.mangopay.core.enumerations.KycLevel;
import com.mangopay.core.enumerations.LegalPersonType;
import com.mangopay.core.enumerations.NaturalUserCapacity;
import com.mangopay.core.enumerations.PersonType;
import com.mangopay.core.interfaces.BankAccountDetails;
import com.mangopay.entities.BankAccount;
import com.mangopay.entities.IdempotencyResponse;
import com.mangopay.entities.User;
import com.mangopay.entities.UserLegal;
import com.mangopay.entities.UserNatural;
import com.mangopay.entities.Wallet;
import com.mangopay.entities.subentities.BankAccountDetailsIBAN;

import eu.opertusmundi.common.domain.AccountEntity;
import eu.opertusmundi.common.domain.AddressEmbeddable;
import eu.opertusmundi.common.domain.BankAccountEmbeddable;
import eu.opertusmundi.common.domain.CustomerDraftEntity;
import eu.opertusmundi.common.domain.CustomerDraftIndividualEntity;
import eu.opertusmundi.common.domain.CustomerDraftProfessionalEntity;
import eu.opertusmundi.common.domain.CustomerProfessionalEntity;
import eu.opertusmundi.common.domain.CustomerRrepresentativeEmbeddable;
import eu.opertusmundi.common.model.EnumCustomerRegistrationStatus;
import eu.opertusmundi.common.model.dto.AccountDto;
import eu.opertusmundi.common.model.dto.EnumCustomerType;
import eu.opertusmundi.common.model.dto.EnumLegalPersonType;
import eu.opertusmundi.common.repository.AccountRepository;

@Service
@Transactional
public class MangoPayPaymentService implements PaymentService {

    private static final Logger logger = LoggerFactory.getLogger(MangoPayPaymentService.class);

    @Value("${opertusmundi.payments.mangopay.base-url}")
    private String baseUrl;

    @Value("${opertusmundi.payments.mangopay.client-id}")
    private String clientId;

    @Value("${opertusmundi.payments.mangopay.client-password}")
    private String clientPassword;

    private MangoPayApi api;

    @Autowired
    private AccountRepository accountRepository;

    @PostConstruct
    private void init() {
        this.api = new MangoPayApi();

        this.api.getConfig().setBaseUrl(this.baseUrl);
        this.api.getConfig().setClientId(this.clientId);
        this.api.getConfig().setClientPassword(this.clientPassword);
    }

    @Override
    public AccountDto createUser(UUID userKey, UUID registrationKey) {
        try {
            User user;

            // Get account
            final AccountEntity account = this.getAccount(userKey);

            // Resolve registration
            final CustomerDraftEntity registration   = this.resolveRegistration(account, registrationKey);
            final EnumCustomerType    type           = registration.getType();
            final String              idempotencyKey = registration.getUserIdempotentKey().toString();

            // Check if this is a retry
            switch (type) {
                case INDIVIDUAL :
                    user = this.<UserNatural>getResponse(idempotencyKey);
                    break;
                case PROFESSIONAL :
                    user = this.<UserLegal>getResponse(idempotencyKey);
                    break;
                default :
                    throw new PaymentServiceException(String.format("Customer type [%s] is not supported", type));
            }

            if (user != null) {
                // User has already been created. Check MangoPay user Id
                if (!StringUtils.isBlank(registration.getPaymentProviderUser()) &&
                    !user.getId().equals(registration.getPaymentProviderUser())
                ) {
                    throw new PaymentServiceException(String.format(
                        "[MANGOPAY] Multiple keys [%s, %s] have been found for OpertusMundi user [%s](%s)",
                        registration.getPaymentProviderUser(), user.getId(), account.getEmail(), account.getKey()
                    ));
                }
            } else {
                switch (type) {
                    case INDIVIDUAL :
                        user = this.createUserNatural(account, (CustomerDraftIndividualEntity) registration);
                        break;
                    case PROFESSIONAL :
                        user = this.createUserLegal(account, (CustomerDraftProfessionalEntity) registration);
                        break;
                    default :
                        throw new PaymentServiceException(String.format("Customer type [%s] is not supported", type));
                }

                user = this.api.getUserApi().create(idempotencyKey, user);
            }

            // Update registration
            registration.setPaymentProviderUser(user.getId());

            this.accountRepository.saveAndFlush(account);

            return account.toDto();
        } catch (final ResponseException ex) {
            logger.error("[MANGOPAY] API operation has failed", ex);

            throw new PaymentServiceException("[MANGOPAY] Error: " + ex.getMessage(), ex);
        } catch (final PaymentServiceException ex) {
            throw ex;
        } catch (final Exception ex) {
            logger.error("[MANGOPAY] User creation has failed", ex);

            throw new PaymentServiceException("[MANGOPAY] User creation has failed", ex);
        }
    }

    @Override
    public AccountDto updateUser(UUID userKey, UUID registrationKey) {
        try {
            User user;

            // Get account
            final AccountEntity account = this.getAccount(userKey);

            // Resolve registration
            final CustomerDraftEntity registration = this.resolveRegistration(account, registrationKey);
            final EnumCustomerType    type         = registration.getType();

            // A linked account must already exist
            user = this.api.getUserApi().get(registration.getPaymentProviderUser());

            // NOTE: MANGOPAY API throws an exception when an entity is not
            // found. This check may be redundant
            if (user == null) {
                throw new PaymentServiceException(String.format(
                    "[MANGOPAY] User with id [%s] was not found for OpertusMundi user [%s](%s)",
                    registration.getPaymentProviderUser(), account.getEmail(), account.getKey()
                ));
            }

            // Update user
            switch (type) {
                case INDIVIDUAL :
                    user = this.createUserNatural(account, (CustomerDraftIndividualEntity) registration, user.getId());
                    break;
                case PROFESSIONAL :
                    user = this.createUserLegal(account, (CustomerDraftProfessionalEntity) registration,user.getId());
                    break;
                default :
                    throw new PaymentServiceException(String.format("Customer type [%s] is not supported", type));
            }

            user = this.api.getUserApi().update(user);

            // Update OpertusMundi account
            this.accountRepository.saveAndFlush(account);

            return account.toDto();
        } catch (final ResponseException ex) {
            logger.error("[MANGOPAY] API operation has failed", ex);

            throw new PaymentServiceException("[MANGOPAY] Error: " + ex.getApiMessage(), ex);
        } catch (final PaymentServiceException ex) {
            throw ex;
        } catch (final Exception ex) {
            logger.error("[MANGOPAY] User update has failed", ex);

            throw new PaymentServiceException("[MANGOPAY] User update has failed", ex);
        }
    }

    @Override
    public AccountDto createWallet(UUID userKey, UUID registrationKey) {
        try {
            Wallet wallet;

            // Get account
            final AccountEntity account = this.getAccount(userKey);

            // Resolve registration
            final CustomerDraftEntity registration   = this.resolveRegistration(account, registrationKey);
            final String              idempotencyKey = registration.getWalletIdempotentKey().toString();

            // OpertusMundi user must be registered to the MangoPay platform
            if (StringUtils.isBlank(registration.getPaymentProviderUser())) {
                throw new PaymentServiceException(String.format("[MANGOPAY] OpertusMundi User [%s] is not registered", userKey));
            }

            // Check if this is a retry
            wallet = this.<Wallet>getResponse(idempotencyKey);

            if (wallet != null) {
                // Wallet has already been created. Check MangoPay wallet Id
                if (!StringUtils.isBlank(registration.getPaymentProviderWallet()) &&
                    !wallet.getId().equals(registration.getPaymentProviderWallet())
                ) {
                    throw new PaymentServiceException(String.format(
                        "[MANGOPAY] Multiple keys [%s, %s] have been found for the wallet of OpertusMundi user [%s](%s)",
                        registration.getPaymentProviderWallet(), wallet.getId(), account.getEmail(), account.getKey()
                    ));
                }
            } else {
                // Create wallet
                final ArrayList<String> owners = new ArrayList<>();

                owners.add(registration.getPaymentProviderUser());

                wallet = new Wallet();

                wallet.setCurrency(CurrencyIso.EUR);
                wallet.setDescription(this.getWalletDescription(account));
                wallet.setFundsType(FundsType.DEFAULT);
                wallet.setOwners(owners);
                wallet.setTag(userKey.toString());

                wallet = this.api.getWalletApi().create(idempotencyKey, wallet);
            }

            // Update registration
            registration.setPaymentProviderWallet(wallet.getId());

            this.accountRepository.saveAndFlush(account);

            return account.toDto();
        } catch (final ResponseException ex) {
            logger.error("[MANGOPAY] API operation has failed", ex);

            throw new PaymentServiceException("[MANGOPAY] Error: " + ex.getApiMessage(), ex);
        } catch (final PaymentServiceException ex) {
            throw ex;
        } catch (final Exception ex) {
            logger.error("[MANGOPAY] User creation has failed", ex);

            throw new PaymentServiceException("[MANGOPAY] User creation has failed", ex);
        }
    }

    @Override
    public AccountDto createBankAccount(UUID userKey, UUID registrationKey) {
        try {
            BankAccount bankAccount;

            // Get account
            final AccountEntity account = this.getAccount(userKey);

            // Resolve registration
            final CustomerDraftEntity registration   = this.resolveRegistration(account, registrationKey);
            final String              idempotencyKey = registration.getBankAccountIdempotentKey().toString();

            // OpertusMundi user must be registered to the MangoPay platform
            if (StringUtils.isBlank(registration.getPaymentProviderUser())) {
                throw new PaymentServiceException(String.format("[MANGOPAY] OpertusMundi User [%s] is not registered", userKey));
            }

            // Registration must be of type PROFESSIONAL
            if (registration.getType() != EnumCustomerType.PROFESSIONAL) {
                throw new PaymentServiceException(
                    String.format("[MANGOPAY] Cannot create bank account for user [%s] of type [%s]", registration.getType(), userKey)
                );
            }

            final CustomerDraftProfessionalEntity profRegistration = (CustomerDraftProfessionalEntity) registration;

            // Check if this is a retry
            bankAccount = this.<BankAccount>getResponse(idempotencyKey);

            if (bankAccount != null) {
                // Bank account has already been created. Check MangoPay bank account Id
                if (!StringUtils.isBlank(profRegistration.getBankAccount().getId()) &&
                    !bankAccount.getId().equals(profRegistration.getBankAccount().getId())
                ) {
                    throw new PaymentServiceException(String.format(
                        "[MANGOPAY] Multiple keys [%s, %s] have been found for the bank account of OpertusMundi user [%s](%s)",
                        profRegistration.getBankAccount().getId(), bankAccount.getId(), account.getEmail(), account.getKey()
                    ));
                }
            } else {
                // Create bank account
                bankAccount = this.createBankAccount(profRegistration.getBankAccount());

                bankAccount.setTag(userKey.toString());
                bankAccount.setUserId(registration.getPaymentProviderUser());

                bankAccount = this.api.getUserApi().createBankAccount(
                    idempotencyKey, registration.getPaymentProviderUser(), bankAccount
                );
            }

            // Update registration
            profRegistration.getBankAccount().setId(bankAccount.getId());

            this.accountRepository.saveAndFlush(account);

            return account.toDto();
        } catch (final ResponseException ex) {
            logger.error("[MANGOPAY] API operation has failed", ex);

            throw new PaymentServiceException("[MANGOPAY] Error: " + ex.getApiMessage(), ex);
        } catch (final PaymentServiceException ex) {
            throw ex;
        } catch (final Exception ex) {
            logger.error("[MANGOPAY] User creation has failed", ex);

            throw new PaymentServiceException("[MANGOPAY] User creation has failed", ex);
        }
    }

    /**
     * Update an existing bank account in the external payment service.
     *
     * MANGOPAY does not support updating an account. If any attribute of the account is changed,
     * the account is deactivated and a new one is created.
     *
     * @param userKey
     * @param registrationKey
     * @return
     */
    @Override
    public AccountDto updateBankAccount(UUID userKey, UUID registrationKey) {
        try {
            final AccountEntity                   account               = this.getAccount(userKey);
            final CustomerProfessionalEntity      customer              = account.getProfile().getProvider();
            final CustomerDraftProfessionalEntity registration          = this.getProviderRegistration(account, registrationKey);

            if(registration == null) {
                throw new PaymentServiceException(String.format(
                    "[MANGOPAY] Provider registration was not found for account with key [%s]",
                    userKey
                ));
            }

            if (registration.getStatus() != EnumCustomerRegistrationStatus.SUBMITTED) {
                throw new PaymentServiceException(String.format(
                    "[MANGOPAY] Invalid registration state [%s] for key [%s]. Expected [SUBMITTED]",
                    registration.getStatus(), registrationKey
                ));
            }

            // If no attribute is updated, skip update
            if (customer.getBankAccount().equals(registration.getBankAccount())) {
                return account.toDto();
            }

            final String mangoPayUserId        = customer.getPaymentProviderUser();
            final String mangoPayBankAccountId = customer.getBankAccount().getId();

            // A linked account must already exist
            final BankAccount currentBankAccount = this.api.getUserApi().getBankAccount(mangoPayUserId, mangoPayBankAccountId);

            // Deactivate the current bank account of the provider
            this.deactivateAccount(mangoPayUserId, currentBankAccount);
            customer.getBankAccount().setId(null);

            this.accountRepository.saveAndFlush(account);

            // Create new account
            final AccountDto result = this.createBankAccount(userKey, registrationKey);

            return result;
        } catch (final ResponseException ex) {
            logger.error("[MANGOPAY] API operation has failed", ex);

            throw new PaymentServiceException("[MANGOPAY] Error: " + ex.getApiMessage(), ex);
        } catch (final PaymentServiceException ex) {
            throw ex;
        } catch (final Exception ex) {
            logger.error("[MANGOPAY] User creation has failed", ex);

            throw new PaymentServiceException("[MANGOPAY] User creation has failed", ex);
        }
    }

    private void deactivateAccount(String userId, BankAccount bankAccount) throws PaymentServiceException {
        Assert.notNull(bankAccount, "Expected a non-null bank account");

        // Check if this is a retry
        if (!bankAccount.isActive()) {
            return;
        }

        try {
            bankAccount.setActive(false);
            bankAccount = this.api.getUserApi().updateBankAccount(userId, bankAccount, bankAccount.getId());
        } catch (final ResponseException ex) {
            logger.error("[MANGOPAY] API operation has failed", ex);

            throw new PaymentServiceException("[MANGOPAY] Error: " + ex.getApiMessage(), ex);
        } catch (final PaymentServiceException ex) {
            throw ex;
        } catch (final Exception ex) {
            logger.error("[MANGOPAY] Bank account update has failed", ex);

            throw new PaymentServiceException("[MANGOPAY] Bank account update has failed", ex);
        }
    }

    private AccountEntity getAccount(UUID key) throws PaymentServiceException {
        final AccountEntity account = this.accountRepository.findOneByKey(key).orElse(null);

        if (account == null) {
            throw new PaymentServiceException(String.format("[MANGOPAY] OpertusMundi user [%s] was not found", key));
        }

        return account;
    }

    private UserNatural createUserNatural(AccountEntity a, CustomerDraftIndividualEntity r) {
        return this.createUserNatural(a, r, null);
    }

    private UserNatural createUserNatural(AccountEntity a, CustomerDraftIndividualEntity r, String id) {
        final UserNatural u = new UserNatural();

        u.setAddress(this.createAddress(r.getAddress()));
        u.setBirthday(r.getBirthdate().toEpochSecond());
        u.setCountryOfResidence(this.stringToCountryIso(r.getCountryOfResidence()));
        u.setEmail(r.getEmail());
        u.setFirstName(r.getFirstName());
        u.setId(id);
        u.setLastName(r.getLastName());
        u.setNationality(this.stringToCountryIso(r.getNationality()));
        u.setOccupation(r.getOccupation());
        u.setTag(a.getKey().toString());

        u.setCapacity(NaturalUserCapacity.NORMAL);
        u.setKycLevel(KycLevel.LIGHT);
        u.setPersonType(PersonType.NATURAL);

        return u;
    }

    private UserLegal createUserLegal(AccountEntity a, CustomerDraftProfessionalEntity r) {
        return this.createUserLegal(a, r, null);
    }

    private UserLegal createUserLegal(AccountEntity a, CustomerDraftProfessionalEntity r, String id) {
        final UserLegal u = new UserLegal();

        final CustomerRrepresentativeEmbeddable lr = r.getLegalRepresentative();

        u.setCompanyNumber(r.getCompanyNumber());
        u.setEmail(r.getEmail());
        u.setHeadquartersAddress(this.createAddress(r.getHeadquartersAddress()));
        u.setId(id);
        u.setLegalPersonType(this.enumToLegalPersonType(r.getLegalPersonType()));
        u.setLegalRepresentativeAddress(this.createAddress(lr.getAddress()));
        u.setLegalRepresentativeBirthday(lr.getBirthdate().toEpochSecond());
        u.setLegalRepresentativeCountryOfResidence(this.stringToCountryIso(lr.getCountryOfResidence()));
        u.setLegalRepresentativeEmail(lr.getEmail());
        u.setLegalRepresentativeFirstName(lr.getFirstName());
        u.setLegalRepresentativeLastName(lr.getLastName());
        u.setLegalRepresentativeNationality(this.stringToCountryIso(lr.getNationality()));
        u.setName(r.getName());
        u.setTag(a.getKey().toString());

        u.setKycLevel(KycLevel.LIGHT);
        u.setPersonType(PersonType.LEGAL);

        return u;
    }

    private Address createAddress(AddressEmbeddable e) {
        final Address a = new Address();

        a.setAddressLine1(e.getLine1());
        a.setAddressLine2(e.getLine2());
        a.setCity(e.getCity());
        a.setCountry(this.stringToCountryIso(e.getCountry()));
        a.setPostalCode(e.getPostalCode());
        a.setRegion(e.getRegion());

        return a;
    }

    private BankAccount createBankAccount(BankAccountEmbeddable a) {
        return this.createBankAccount(a, null);
    }

    private BankAccount createBankAccount(BankAccountEmbeddable a, String id) {
        final BankAccount bankAccount = new BankAccount();

        bankAccount.setActive(true);
        bankAccount.setDetails(this.createBankAccountDetails(a));
        bankAccount.setId(id);
        bankAccount.setOwnerAddress(this.createAddress(a.getOwnerAddress()));
        bankAccount.setOwnerName(a.getOwnerName());
        bankAccount.setType(BankAccountType.IBAN);

        return bankAccount;
    }


    private BankAccountDetails createBankAccountDetails(BankAccountEmbeddable a) {
        final BankAccountDetailsIBAN d = new BankAccountDetailsIBAN();

        d.setBic(a.getBic());
        d.setIban(a.getIban());

        return d;
    }

    private CustomerDraftProfessionalEntity getProviderRegistration(AccountEntity account, UUID key) {
        return (CustomerDraftProfessionalEntity) this.resolveRegistration(account, key);
    }

    private CustomerDraftEntity resolveRegistration(AccountEntity account, UUID key) {
        // Lookup for consumers
        CustomerDraftEntity registration = account.getProfile().getConsumerRegistration();

        if (registration != null && registration.getKey().equals(key)) {
            if (registration.getStatus() != EnumCustomerRegistrationStatus.SUBMITTED) {
                throw new PaymentServiceException(String.format(
                    "[MANGOPAY] Invalid registration state [%s] for key [%s]. Expected [SUBMITTED]",
                    registration.getStatus(), key
                ));
            }
            return registration;
        }

        // Lookup for providers
        registration = account.getProfile().getProviderRegistration();

        if (registration != null && registration.getKey().equals(key)) {
            if (registration.getStatus() != EnumCustomerRegistrationStatus.SUBMITTED) {
                throw new PaymentServiceException(String.format(
                    "[MANGOPAY] Invalid registration state [%s] for key [%s]. Expected [SUBMITTED]",
                    registration.getStatus(), key
                ));
            }
            return registration;
        }

        throw new PaymentServiceException(String.format("[MANGOPAY] No active registration found for key [%s]", key));
    }

    private CountryIso stringToCountryIso(String value) {
        for (final CountryIso v : CountryIso.values()) {
            if (v.name().equalsIgnoreCase(value)) {
                return v;
            }
        }
        return null;
    }

    private LegalPersonType enumToLegalPersonType(EnumLegalPersonType t) {
        switch (t) {
            case BUSINESS :
                return LegalPersonType.BUSINESS;
            case ORGANIZATION :
                return LegalPersonType.ORGANIZATION;
            case SOLETRADER :
                return LegalPersonType.SOLETRADER;
        }

        throw new PaymentServiceException(String.format("[MANGOPAY] Legal person type [%s] is not supported", t));
    }

    private String getWalletDescription(AccountEntity a) {
        return String.format("Default wallet");
    }

    @SuppressWarnings("unchecked")
    private <T> T getResponse(String idempotencyKey) throws Exception {
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
        }
    }

}
