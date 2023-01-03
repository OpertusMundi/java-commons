package eu.opertusmundi.common.service.mangopay;

import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mangopay.core.Address;
import com.mangopay.core.enumerations.KycLevel;
import com.mangopay.core.enumerations.LegalPersonType;
import com.mangopay.core.enumerations.NaturalUserCapacity;
import com.mangopay.core.enumerations.PersonType;
import com.mangopay.core.enumerations.UserCategory;
import com.mangopay.entities.User;
import com.mangopay.entities.UserBlockStatus;
import com.mangopay.entities.UserLegal;
import com.mangopay.entities.UserNatural;

import eu.opertusmundi.common.domain.AccountEntity;
import eu.opertusmundi.common.domain.AddressEmbeddable;
import eu.opertusmundi.common.domain.CustomerDraftEntity;
import eu.opertusmundi.common.domain.CustomerDraftIndividualEntity;
import eu.opertusmundi.common.domain.CustomerDraftProfessionalEntity;
import eu.opertusmundi.common.domain.CustomerEntity;
import eu.opertusmundi.common.domain.CustomerProfessionalEntity;
import eu.opertusmundi.common.domain.CustomerRepresentativeEmbeddable;
import eu.opertusmundi.common.model.account.AccountDto;
import eu.opertusmundi.common.model.account.EnumCustomerType;
import eu.opertusmundi.common.model.account.EnumLegalPersonType;
import eu.opertusmundi.common.model.account.EnumMangopayUserType;
import eu.opertusmundi.common.model.payment.BlockStatusDto;
import eu.opertusmundi.common.model.payment.PaymentException;
import eu.opertusmundi.common.model.payment.UserBlockedStatusDto;
import eu.opertusmundi.common.model.payment.UserRegistrationCommand;
import eu.opertusmundi.common.repository.AccountRepository;
import eu.opertusmundi.common.repository.CustomerRepository;
import eu.opertusmundi.common.util.MangopayUtils;

@Service
@Transactional
public class MangoPayUserService extends BaseMangoPayService implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(MangoPayUserService.class);

    @Value("${opertusmundi.topio-account-id:68}")
    private Integer topioAccountId;

    private final CustomerRepository customerRepository;

    @Autowired
    public MangoPayUserService(AccountRepository accountRepository, CustomerRepository customerRepository) {
        super(accountRepository);

        this.customerRepository = customerRepository;
    }

    @Override
    public UserBlockedStatusDto getUserBlockStatus(UUID userKey) throws PaymentException {
        try {
            final AccountEntity              account  = this.getAccount(userKey);
            final CustomerEntity             consumer = account.getConsumer();
            final CustomerProfessionalEntity provider = account.getProvider();

            final String consumerId = consumer == null ? null : consumer.getPaymentProviderUser();
            final String providerId = provider == null ? null : provider.getPaymentProviderUser();

            final BlockStatusDto consumerStatus = this.getUserBlockStatus(consumerId);
            final BlockStatusDto providerStatus = this.getUserBlockStatus(providerId);

            return UserBlockedStatusDto.of(consumerStatus, providerStatus);
        } catch (final Exception ex) {
            throw this.wrapException("Get user block status", ex, userKey);
        }
    }

    @Override
    public void updateUserBlockStatus(String userId) throws PaymentException {
        try {
            final CustomerEntity customer = this.customerRepository.findCustomerByProviderUserId(userId).orElse(null);

            this.ensureCustomer(customer, userId);

            final BlockStatusDto status = this.getUserBlockStatus(userId);

            customer.setBlockedInflows(status.getInflows());
            customer.setBlockedOutflows(status.getOutflows());

            this.customerRepository.saveAndFlush(customer);
        } catch (final Exception ex) {
            throw this.wrapException("Update user block status", ex, userId);
        }
    }

    @Override
    public void updateUserBlockStatus(UUID userKey, EnumCustomerType type) throws PaymentException {
        try {
            final AccountEntity  account  = this.accountRepository.findOneByKey(userKey).orElse(null);
            final CustomerEntity customer = type == EnumCustomerType.CONSUMER ? account.getConsumer() : account.getProvider();

            this.ensureCustomer(customer, userKey);

            final BlockStatusDto status = this.getUserBlockStatus(customer.getPaymentProviderUser());

            customer.setBlockedInflows(status.getInflows());
            customer.setBlockedOutflows(status.getOutflows());

            this.customerRepository.saveAndFlush(customer);
        } catch (final Exception ex) {
            throw this.wrapException("Update user block status", ex, userKey);
        }
    }

    private BlockStatusDto getUserBlockStatus(@Nullable String userId) throws Exception {
        if (StringUtils.isBlank(userId)) {
            return null;
        }
        final UserBlockStatus status = this.api.getUserApi().getRegulatory(userId);
        final BlockStatusDto  result = BlockStatusDto.of(
            status.getActionCode(), status.getScopeBlocked().getInflows(), status.getScopeBlocked().getOutflows()
        );

        return result;
    }

    @Override
    public AccountDto createUser(UserRegistrationCommand command) {
        try {
            User user;

            // Get account
            final AccountEntity account = this.getAccount(command.getUserKey());

            // Resolve registration
            final CustomerDraftEntity  registration   = this.resolveCustomerRegistration(account, command.getType(), command.getRegistrationKey());
            final EnumMangopayUserType type           = registration.getType();
            final String               idempotencyKey = registration.getUserIdempotentKey().toString();

            // Check if this is a retry
            switch (type) {
                case INDIVIDUAL :
                    user = this.<UserNatural>getResponse(idempotencyKey);
                    break;
                case PROFESSIONAL :
                    user = this.<UserLegal>getResponse(idempotencyKey);
                    break;
                default :
                    throw new PaymentException(String.format("Customer type [%s] is not supported", type));
            }

            if (user != null) {
                // User has already been created. Check MangoPay user Id
                if (!StringUtils.isBlank(registration.getPaymentProviderUser()) &&
                    !user.getId().equals(registration.getPaymentProviderUser())
                ) {
                    throw new PaymentException(String.format(
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
                        user = this.createUserLegal(command.getType(), account, (CustomerDraftProfessionalEntity) registration);
                        break;
                    default :
                        throw new PaymentException(String.format("Customer type [%s] is not supported", type));
                }

                user = this.api.getUserApi().create(idempotencyKey, user);
            }

            // Update registration
            registration.setPaymentProviderUser(user.getId());

            this.accountRepository.saveAndFlush(account);

            return account.toDto();
        } catch (final Exception ex) {
            throw this.wrapException("Create User", ex, command);
        }
    }

    @Override
    public AccountDto updateUser(UserRegistrationCommand command) {
        try {
            User user;

            // Get account
            final AccountEntity account = this.getAccount(command.getUserKey());

            // Resolve registration
            final CustomerDraftEntity  registration = this.resolveCustomerRegistration(account, command.getType(), command.getRegistrationKey());
            final EnumMangopayUserType type         = registration.getType();

            // A linked account must already exist
            user = this.api.getUserApi().get(registration.getPaymentProviderUser());

            // NOTE: MANGOPAY API throws an exception when an entity is not
            // found. This check may be redundant
            if (user == null) {
                throw new PaymentException(String.format(
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
                    user = this.createUserLegal(command.getType(), account, (CustomerDraftProfessionalEntity) registration, user.getId());
                    break;
                default :
                    throw new PaymentException(String.format("Customer type [%s] is not supported", type));
            }

            user = this.api.getUserApi().update(user);

            // Update OpertusMundi account
            this.accountRepository.saveAndFlush(account);

            return account.toDto();
        } catch (final Exception ex) {
            throw this.wrapException("Update User", ex, command);
        }
    }

    private UserNatural createUserNatural(AccountEntity a, CustomerDraftIndividualEntity r) {
        return this.createUserNatural(a, r, null);
    }

    /**
     * Create MANGOPAY natural user
     *
     * @see https://docs.mangopay.com/endpoints/v2.01/users#e255_create-a-natural-user
     *
     * @param account
     * @param registration
     * @param id
     * @return
     */
    private UserNatural createUserNatural(AccountEntity account, CustomerDraftIndividualEntity registration, String id) {
        final UserNatural u = new UserNatural();

        if (registration.getAddress() != null) {
            u.setAddress(this.createAddress(registration.getAddress()));
        }
        if (registration.getBirthdate() != null) {
            u.setBirthday(registration.getBirthdate().toEpochSecond());
        }
        if (!StringUtils.isBlank(registration.getCountryOfResidence())) {
            u.setCountryOfResidence(MangopayUtils.countryFromString(registration.getCountryOfResidence()));
        }
        u.setEmail(registration.getEmail());
        u.setFirstName(registration.getFirstName());
        u.setId(id);
        u.setLastName(registration.getLastName());
        if (!StringUtils.isBlank(registration.getNationality())) {
            u.setNationality(MangopayUtils.countryFromString(registration.getNationality()));
        }
        u.setOccupation(registration.getOccupation());
        u.setTag(account.getKey().toString());
        // For natural users, user category is always PAYER
        u.setUserCategory(UserCategory.PAYER);

        u.setCapacity(NaturalUserCapacity.NORMAL);
        u.setPersonType(PersonType.NATURAL);

        return u;
    }

    private UserLegal createUserLegal(EnumCustomerType type, AccountEntity account, CustomerDraftProfessionalEntity customer) {
        return this.createUserLegal(type, account, customer, null);
    }

    /**
     * Create MANGOPAY legal user
     *
     * @see https://docs.mangopay.com/endpoints/v2.01/users#e259_create-a-legal-user
     * @see https://docs.mangopay.com/endpoints/v2.01/users#e1060_create-a-legal-user-owner
     *
     * @param type
     * @param account
     * @param customer
     * @param id
     * @return
     */
    private UserLegal createUserLegal(EnumCustomerType type, AccountEntity account, CustomerDraftProfessionalEntity customer, String id) {
        final UserLegal u = new UserLegal();

        final CustomerRepresentativeEmbeddable representative = customer.getRepresentative();

        // Payer fields
        u.setEmail(customer.getEmail());
        u.setLegalPersonType(this.enumToLegalPersonType(customer.getLegalPersonType()));
        u.setLegalRepresentativeFirstName(representative.getFirstName());
        u.setLegalRepresentativeLastName(representative.getLastName());
        u.setName(customer.getName());
        if (representative.getAddress() != null) {
            u.setLegalRepresentativeAddress(this.createAddress(representative.getAddress()));
        }
        u.setTag(account.getKey().toString());
        // See: https://github.com/Mangopay/mangopay2-java-sdk/issues/285
        //
        // Set terms and conditions acceptance only for new users. If the
        // property `termsAndConditionsAccepted` is set to true for an existing
        // user, the error "The user already accepted our terms and conditions"
        // will be returned
        if (StringUtils.isBlank(id)) {
            u.setTermsAndConditionsAccepted(true);
        } else {
            u.setTermsAndConditionsAccepted(null);
        }
        u.setUserCategory(type == EnumCustomerType.CONSUMER ? UserCategory.PAYER : UserCategory.OWNER);

        // Owner fields
        u.setCompanyNumber(customer.getCompanyNumber());
        if (customer.getHeadquartersAddress() != null) {
            u.setHeadquartersAddress(this.createAddress(customer.getHeadquartersAddress()));
        }
        u.setId(id);
        u.setLegalRepresentativeBirthday(representative.getBirthdate().toEpochSecond());
        u.setLegalRepresentativeCountryOfResidence(MangopayUtils.countryFromString(representative.getCountryOfResidence()));
        u.setLegalRepresentativeEmail(representative.getEmail());
        u.setLegalRepresentativeNationality(MangopayUtils.countryFromString(representative.getNationality()));

        u.setKycLevel(KycLevel.LIGHT);
        u.setPersonType(PersonType.LEGAL);

        return u;
    }

    private Address createAddress(AddressEmbeddable e) {
        final Address a = new Address();

        a.setAddressLine1(e.getLine1());
        a.setAddressLine2(e.getLine2());
        a.setCity(e.getCity());
        a.setCountry(MangopayUtils.countryFromString(e.getCountry()));
        a.setPostalCode(e.getPostalCode());
        a.setRegion(e.getRegion());

        return a;
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

        throw new PaymentException(String.format("[MANGOPAY] Legal person type [%s] is not supported", t));
    }

    private PaymentException wrapException(String operation, Exception ex, Object command) {
        return super.wrapException(operation, ex, command, logger);
    }

}