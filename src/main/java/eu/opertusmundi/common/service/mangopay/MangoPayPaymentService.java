package eu.opertusmundi.common.service.mangopay;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import com.mangopay.core.Address;
import com.mangopay.core.FilterEvents;
import com.mangopay.core.Money;
import com.mangopay.core.Pagination;
import com.mangopay.core.ResponseException;
import com.mangopay.core.Sorting;
import com.mangopay.core.enumerations.BankAccountType;
import com.mangopay.core.enumerations.CardType;
import com.mangopay.core.enumerations.CurrencyIso;
import com.mangopay.core.enumerations.EventType;
import com.mangopay.core.enumerations.FundsType;
import com.mangopay.core.enumerations.KycLevel;
import com.mangopay.core.enumerations.LegalPersonType;
import com.mangopay.core.enumerations.NaturalUserCapacity;
import com.mangopay.core.enumerations.PayInExecutionType;
import com.mangopay.core.enumerations.PayInPaymentType;
import com.mangopay.core.enumerations.PayOutPaymentType;
import com.mangopay.core.enumerations.PayoutMode;
import com.mangopay.core.enumerations.PersonType;
import com.mangopay.core.enumerations.SecureMode;
import com.mangopay.core.enumerations.SortDirection;
import com.mangopay.core.enumerations.TransactionType;
import com.mangopay.core.enumerations.UserCategory;
import com.mangopay.core.interfaces.BankAccountDetails;
import com.mangopay.entities.BankAccount;
import com.mangopay.entities.Card;
import com.mangopay.entities.CardRegistration;
import com.mangopay.entities.Client;
import com.mangopay.entities.Event;
import com.mangopay.entities.PayIn;
import com.mangopay.entities.PayOut;
import com.mangopay.entities.Refund;
import com.mangopay.entities.Transfer;
import com.mangopay.entities.User;
import com.mangopay.entities.UserBlockStatus;
import com.mangopay.entities.UserLegal;
import com.mangopay.entities.UserNatural;
import com.mangopay.entities.Wallet;
import com.mangopay.entities.subentities.BankAccountDetailsIBAN;
import com.mangopay.entities.subentities.PayInExecutionDetailsDirect;
import com.mangopay.entities.subentities.PayInPaymentDetailsBankWire;
import com.mangopay.entities.subentities.PayInPaymentDetailsCard;
import com.mangopay.entities.subentities.PayOutPaymentDetailsBankWire;

import eu.opertusmundi.common.domain.AccountEntity;
import eu.opertusmundi.common.domain.AddressEmbeddable;
import eu.opertusmundi.common.domain.CardDirectPayInEntity;
import eu.opertusmundi.common.domain.CustomerBankAccountEmbeddable;
import eu.opertusmundi.common.domain.CustomerDraftEntity;
import eu.opertusmundi.common.domain.CustomerDraftIndividualEntity;
import eu.opertusmundi.common.domain.CustomerDraftProfessionalEntity;
import eu.opertusmundi.common.domain.CustomerEntity;
import eu.opertusmundi.common.domain.CustomerProfessionalEntity;
import eu.opertusmundi.common.domain.CustomerRepresentativeEmbeddable;
import eu.opertusmundi.common.domain.OrderEntity;
import eu.opertusmundi.common.domain.PayInEntity;
import eu.opertusmundi.common.domain.PayInItemEntity;
import eu.opertusmundi.common.domain.PayInOrderItemEntity;
import eu.opertusmundi.common.domain.PayInRecurringRegistrationEntity;
import eu.opertusmundi.common.domain.PayInServiceBillingItemEntity;
import eu.opertusmundi.common.domain.PayOutEntity;
import eu.opertusmundi.common.model.EnumSortingOrder;
import eu.opertusmundi.common.model.EnumView;
import eu.opertusmundi.common.model.PageResultDto;
import eu.opertusmundi.common.model.account.AccountDto;
import eu.opertusmundi.common.model.account.BankAccountDto;
import eu.opertusmundi.common.model.account.EnumCustomerRegistrationStatus;
import eu.opertusmundi.common.model.account.EnumCustomerType;
import eu.opertusmundi.common.model.account.EnumLegalPersonType;
import eu.opertusmundi.common.model.account.EnumMangopayUserType;
import eu.opertusmundi.common.model.account.EnumPayoffStatus;
import eu.opertusmundi.common.model.catalogue.client.CatalogueItemDetailsDto;
import eu.opertusmundi.common.model.catalogue.client.EnumContractType;
import eu.opertusmundi.common.model.email.EnumMailType;
import eu.opertusmundi.common.model.location.Location;
import eu.opertusmundi.common.model.message.EnumNotificationType;
import eu.opertusmundi.common.model.order.CartDto;
import eu.opertusmundi.common.model.order.CartItemDto;
import eu.opertusmundi.common.model.order.EnumOrderItemType;
import eu.opertusmundi.common.model.order.EnumOrderStatus;
import eu.opertusmundi.common.model.order.HelpdeskOrderDto;
import eu.opertusmundi.common.model.order.OrderCommand;
import eu.opertusmundi.common.model.order.OrderDto;
import eu.opertusmundi.common.model.payment.BankwirePayInCommand;
import eu.opertusmundi.common.model.payment.BankwirePayInExecutionContext;
import eu.opertusmundi.common.model.payment.BlockStatusDto;
import eu.opertusmundi.common.model.payment.CardDirectPayInCommand;
import eu.opertusmundi.common.model.payment.CardDirectPayInExecutionContext;
import eu.opertusmundi.common.model.payment.CardDto;
import eu.opertusmundi.common.model.payment.CardRegistrationCommandDto;
import eu.opertusmundi.common.model.payment.CardRegistrationDto;
import eu.opertusmundi.common.model.payment.CheckoutServiceBillingCommandDto;
import eu.opertusmundi.common.model.payment.ClientDto;
import eu.opertusmundi.common.model.payment.ClientWalletDto;
import eu.opertusmundi.common.model.payment.EnumPayInItemSortField;
import eu.opertusmundi.common.model.payment.EnumPayInSortField;
import eu.opertusmundi.common.model.payment.EnumPayOutSortField;
import eu.opertusmundi.common.model.payment.EnumPaymentItemType;
import eu.opertusmundi.common.model.payment.EnumRecurringPaymentFrequency;
import eu.opertusmundi.common.model.payment.EnumRecurringPaymentType;
import eu.opertusmundi.common.model.payment.EnumTransactionStatus;
import eu.opertusmundi.common.model.payment.EventDto;
import eu.opertusmundi.common.model.payment.FreePayInCommand;
import eu.opertusmundi.common.model.payment.FreePayInExecutionContext;
import eu.opertusmundi.common.model.payment.PayInDto;
import eu.opertusmundi.common.model.payment.PayInItemDto;
import eu.opertusmundi.common.model.payment.PayInStatusUpdateCommand;
import eu.opertusmundi.common.model.payment.PayOutCommandDto;
import eu.opertusmundi.common.model.payment.PayOutDto;
import eu.opertusmundi.common.model.payment.PayOutStatusUpdateCommand;
import eu.opertusmundi.common.model.payment.PaymentException;
import eu.opertusmundi.common.model.payment.PaymentMessageCode;
import eu.opertusmundi.common.model.payment.RecurringRegistrationCreateCommand;
import eu.opertusmundi.common.model.payment.RecurringRegistrationDto;
import eu.opertusmundi.common.model.payment.ServiceBillingDto;
import eu.opertusmundi.common.model.payment.TransferDto;
import eu.opertusmundi.common.model.payment.UserBlockedStatusDto;
import eu.opertusmundi.common.model.payment.UserCardCommand;
import eu.opertusmundi.common.model.payment.UserCommand;
import eu.opertusmundi.common.model.payment.UserPaginationCommand;
import eu.opertusmundi.common.model.payment.UserRegistrationCommand;
import eu.opertusmundi.common.model.payment.WalletDto;
import eu.opertusmundi.common.model.payment.consumer.ConsumerCardDirectPayInDto;
import eu.opertusmundi.common.model.payment.consumer.ConsumerPayInDto;
import eu.opertusmundi.common.model.payment.helpdesk.HelpdeskOrderPayInItemDto;
import eu.opertusmundi.common.model.payment.helpdesk.HelpdeskPayInDto;
import eu.opertusmundi.common.model.payment.provider.ProviderPayInItemDto;
import eu.opertusmundi.common.model.pricing.BasePricingModelCommandDto;
import eu.opertusmundi.common.model.pricing.EffectivePricingModelDto;
import eu.opertusmundi.common.model.pricing.QuotationParametersDto;
import eu.opertusmundi.common.model.pricing.SubscriptionQuotationParameters;
import eu.opertusmundi.common.repository.AccountRepository;
import eu.opertusmundi.common.repository.CustomerRepository;
import eu.opertusmundi.common.repository.OrderRepository;
import eu.opertusmundi.common.repository.PayInItemHistoryRepository;
import eu.opertusmundi.common.repository.PayInRepository;
import eu.opertusmundi.common.repository.PayOutRepository;
import eu.opertusmundi.common.repository.ServiceBillingRepository;
import eu.opertusmundi.common.service.CatalogueService;
import eu.opertusmundi.common.service.OrderFulfillmentService;
import eu.opertusmundi.common.service.PayOutService;
import eu.opertusmundi.common.service.QuotationService;
import eu.opertusmundi.common.util.MangopayUtils;
import eu.opertusmundi.common.util.StreamUtils;

@Service
@Transactional
public class MangoPayPaymentService extends BaseMangoPayService implements PaymentService {

    private static final Logger logger = LoggerFactory.getLogger(MangoPayPaymentService.class);

    @Value("${opertusmundi.topio-account-id:68}")
    private Integer topioAccountId;
    
    // TODO: Set from configuration
    private final BigDecimal feePercent = new BigDecimal(5);

    private final AccountRepository          accountRepository;
    private final CustomerRepository         customerRepository;
    private final OrderRepository            orderRepository;
    private final PayInRepository            payInRepository;
    private final PayOutRepository           payOutRepository;
    private final PayInItemHistoryRepository payInItemHistoryRepository;
    private final CatalogueService           catalogueService;
    private final QuotationService           quotationService;
    private final OrderFulfillmentService    orderFulfillmentService;
    private final PayOutService              payOutService;
    private final RecurringPaymentService    recurringPaymentService;
    private final ServiceBillingRepository   serviceBillingRepository;

    @Autowired
    public MangoPayPaymentService(
        AccountRepository             accountRepository,
        CustomerRepository            customerRepository,
        OrderRepository               orderRepository,
        PayInRepository               payInRepository,
        PayOutRepository              payOutRepository,
        PayInItemHistoryRepository    payInItemHistoryRepository,
        CatalogueService              catalogueService,
        QuotationService              quotationService,
        OrderFulfillmentService       orderFulfillmentService,
        PayOutService                 payOutService,
        RecurringPaymentService       recurringPaymentService,
        ServiceBillingRepository      serviceBillingRepository
    ) {
        this.accountRepository          = accountRepository;
        this.customerRepository         = customerRepository;
        this.orderRepository            = orderRepository;
        this.payOutRepository           = payOutRepository;
        this.payInRepository            = payInRepository;
        this.payInItemHistoryRepository = payInItemHistoryRepository;
        this.catalogueService           = catalogueService;
        this.quotationService           = quotationService;
        this.orderFulfillmentService    = orderFulfillmentService;
        this.payOutService              = payOutService;
        this.recurringPaymentService    = recurringPaymentService;
        this.serviceBillingRepository   = serviceBillingRepository;
    }

    @Override
    public ClientDto getClient() throws PaymentException {
        try {
            final Client client = this.api.getClientApi().get();

            return ClientDto.from(client);
        } catch (final Exception ex) {
            throw this.wrapException("Get client", ex);
        }
    }

    @Override
    public List<ClientWalletDto> getClientWallets() throws PaymentException {
        try {
            final List<ClientWalletDto> result        = new ArrayList<>();
            final List<Wallet>          feeWallets    = this.api.getClientApi().getWallets(FundsType.FEES, new Pagination(0, 20));
            final List<Wallet>          creditWallets = this.api.getClientApi().getWallets(FundsType.CREDIT, new Pagination(0, 20));

            StreamUtils.from(feeWallets).map(ClientWalletDto::from).forEach(result::add);
            StreamUtils.from(creditWallets).map(ClientWalletDto::from).forEach(result::add);

            return result;
        } catch (final Exception ex) {
            throw this.wrapException("Get client wallets", ex);
        }
    }

    @Override
    public List<EventDto> getEvents(int days) throws PaymentException {
        try {
            final long afterDate = ZonedDateTime.now()
                .minusDays(Math.min(days, 40))
                .withZoneSameInstant(ZoneOffset.UTC)
                .toEpochSecond();

            // Set filters
            final FilterEvents filter = new FilterEvents();
            filter.setAfterDate(afterDate);
            filter.setType(EventType.ALL);

            // Set pagination
            final Pagination pagination = new Pagination(1, 100);

            // Set sorting
            final Sorting sort = new Sorting();
            sort.addField("Date", SortDirection.desc);

            final List<Event>    events = this.api.getEventApi().get(filter, pagination, sort);
            final List<EventDto> result = StreamUtils.from(events).map(EventDto::from).collect(Collectors.toList());

            return result;
        } catch (final Exception ex) {
            throw this.wrapException("Get events", ex);
        }
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
            final CustomerDraftEntity  registration   = this.resolveRegistration(account, command.getType(), command.getRegistrationKey());
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
            final CustomerDraftEntity  registration = this.resolveRegistration(account, command.getType(), command.getRegistrationKey());
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

    @Override
    public AccountDto createWallet(UserRegistrationCommand command) {
        try {
            Wallet wallet;

            // Get account
            final AccountEntity account = this.getAccount(command.getUserKey());

            // Resolve registration
            final CustomerDraftEntity registration   = this.resolveRegistration(account, command.getType(), command.getRegistrationKey());
            final String              idempotencyKey = registration.getWalletIdempotentKey().toString();

            // OpertusMundi user must be registered to the MangoPay platform
            if (StringUtils.isBlank(registration.getPaymentProviderUser())) {
                throw new PaymentException(String.format("[MANGOPAY] OpertusMundi User [%s] is not registered", command.getUserKey()));
            }

            // Check if this is a retry
            wallet = this.<Wallet>getResponse(idempotencyKey);

            if (wallet != null) {
                // Wallet has already been created. Check MangoPay wallet Id
                if (!StringUtils.isBlank(registration.getPaymentProviderWallet()) &&
                    !wallet.getId().equals(registration.getPaymentProviderWallet())
                ) {
                    throw new PaymentException(String.format(
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
                wallet.setTag(command.getUserKey().toString());

                wallet = this.api.getWalletApi().create(idempotencyKey, wallet);
            }

            // Update registration
            registration.setPaymentProviderWallet(wallet.getId());

            this.accountRepository.saveAndFlush(account);

            return account.toDto();
        } catch (final Exception ex) {
            throw this.wrapException("Create Wallet", ex, command);
        }
    }

    @Override
    public AccountDto createBankAccount(UserRegistrationCommand command) {
        try {
            BankAccount bankAccount;

            // Get account
            final AccountEntity account = this.getAccount(command.getUserKey());

            // Resolve registration
            final CustomerDraftEntity registration   = this.resolveRegistration(account, command.getType(), command.getRegistrationKey());
            final String              idempotencyKey = registration.getBankAccountIdempotentKey().toString();

            // OpertusMundi user must be registered to the MangoPay platform
            if (StringUtils.isBlank(registration.getPaymentProviderUser())) {
                throw new PaymentException(String.format("[MANGOPAY] OpertusMundi User [%s] is not registered", command.getUserKey()));
            }

            // Registration must be of type PROFESSIONAL
            if (registration.getType() != EnumMangopayUserType.PROFESSIONAL) {
                throw new PaymentException(
                    String.format("[MANGOPAY] Cannot create bank account for user [%s] of type [%s]", account.getEmail(), registration.getType())
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
                    throw new PaymentException(String.format(
                        "[MANGOPAY] Multiple keys [%s, %s] have been found for the bank account of OpertusMundi user [%s](%s)",
                        profRegistration.getBankAccount().getId(), bankAccount.getId(), account.getEmail(), account.getKey()
                    ));
                }
            } else {
                // Create bank account
                bankAccount = this.createBankAccount(profRegistration.getBankAccount());

                bankAccount.setTag(command.getUserKey().toString());
                bankAccount.setUserId(registration.getPaymentProviderUser());

                bankAccount = this.api.getUserApi().createBankAccount(
                    idempotencyKey, registration.getPaymentProviderUser(), bankAccount
                );
            }

            // Update registration
            profRegistration.getBankAccount().setId(bankAccount.getId());
            profRegistration.getBankAccount().setTag(bankAccount.getTag());

            this.accountRepository.saveAndFlush(account);

            return account.toDto();
        } catch (final Exception ex) {
            throw this.wrapException("Create Bank Account", ex, command);
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
    public AccountDto updateBankAccount(UserRegistrationCommand command) {
        try {
            final UUID                            registrationKey = command.getRegistrationKey();
            final AccountEntity                   account         = this.getAccount(command.getUserKey());
            final CustomerProfessionalEntity      customer        = account.getProfile().getProvider();
            final CustomerDraftProfessionalEntity registration    = (CustomerDraftProfessionalEntity)
                this.resolveRegistration(account, command.getType(), registrationKey);

            if(registration == null) {
                throw new PaymentException(String.format(
                    "[MANGOPAY] Provider registration was not found for account with key [%s]",
                    command.getUserKey()
                ));
            }

            if (registration.getStatus() != EnumCustomerRegistrationStatus.SUBMITTED) {
                throw new PaymentException(String.format(
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

            // A linked account may already exists
            if (!StringUtils.isBlank(mangoPayBankAccountId)) {
                final BankAccount currentBankAccount = this.api.getUserApi().getBankAccount(mangoPayUserId, mangoPayBankAccountId);

                // Deactivate the current bank account of the provider
                this.deactivateBankAccount(mangoPayUserId, currentBankAccount);
                customer.getBankAccount().setId(null);

                this.accountRepository.saveAndFlush(account);
            }

            // Create new account
            final AccountDto result = this.createBankAccount(command);

            return result;
        } catch (final Exception ex) {
            throw this.wrapException("Update Bank Account", ex, command);
        }
    }

    @Override
    public List<BankAccountDto> getBankAccounts(UserPaginationCommand command) throws PaymentException {
        try {
            final AccountEntity              account  = this.getAccount(command.getUserKey());
            final CustomerProfessionalEntity customer = account.getProfile().getProvider();

            this.ensureCustomer(customer, command.getUserKey());

            final int               page           = command.getPage() < 1 ? 1 : command.getPage();
            final int               size           = command.getSize() < 1 ? 10 : command.getSize();
            final String            mangoPayUserId = customer.getPaymentProviderUser();
            final List<BankAccount> bankAccounts   = this.api.getUserApi().getBankAccounts(mangoPayUserId, new Pagination(page, size), null);

            if (bankAccounts == null) {
                return Collections.emptyList();
            }

            return bankAccounts.stream().map(BankAccountDto::from).collect(Collectors.toList());
        } catch (final Exception ex) {
            throw this.wrapException("Get User Bank Accounts", ex, command);
        }
    }

    @Override
    public List<CardDto> getCardRegistrations(UserPaginationCommand command) {
        try {
            final AccountEntity  account  = this.getAccount(command.getUserKey());
            final CustomerEntity customer = account.getProfile().getConsumer();

            this.ensureCustomer(customer, command.getUserKey());

            final int        page           = command.getPage() < 1 ? 1 : command.getPage();
            final int        size           = command.getSize() < 1 ? 10 : command.getSize();
            final String     mangoPayUserId = customer.getPaymentProviderUser();
            final List<Card> cards          = this.api.getUserApi().getCards(mangoPayUserId, new Pagination(page, size), null);

            if (cards == null) {
                return Collections.emptyList();
            }

            return cards.stream().map(CardDto::from).collect(Collectors.toList());
        } catch (final Exception ex) {
            throw this.wrapException("Get User Cards", ex, command);
        }
    }

    @Override
    public CardDto getCardRegistration(UserCardCommand command) {
        try {
            final AccountEntity  account  = this.getAccount(command.getUserKey());
            final CustomerEntity customer = account.getProfile().getConsumer();

            this.ensureCustomer(customer, command.getUserKey());

            final String mangoPayUserId = customer.getPaymentProviderUser();
            final Card   card           = this.api.getCardApi().get(command.getCardId());

            if (card != null && card.getUserId().equals(mangoPayUserId)) {
                return CardDto.from(card);
            }

            return null;
        } catch (final ResponseException ex) {
            if (ex.getResponseHttpCode() == HttpStatus.NOT_FOUND.value()) {
                return null;
            }
            throw this.wrapException("Get Card", ex, command);
        } catch (final Exception ex) {
            throw this.wrapException("Get Card", ex, command);
        }
    }

    @Override
    public void deactivateCard(UserCardCommand command) {
        try {
            final AccountEntity  account  = this.getAccount(command.getUserKey());
            final CustomerEntity customer = account.getProfile().getConsumer();

            this.ensureCustomer(customer, command.getUserKey());

            final Card card = this.api.getCardApi().get(command.getCardId());

            Assert.isTrue(card.getUserId().equals(customer.getPaymentProviderUser()), "Card user id must be equal to the customer id");

            this.api.getCardApi().disable(card);
        } catch (final Exception ex) {
            throw this.wrapException("Deactivate Card", ex, command);
        }
    }

    // TODO: Consider using idempotency key for this method ...

    @Override
    public CardRegistrationDto createCardRegistration(UserCommand command) {
        try {
            final AccountEntity  account  = this.getAccount(command.getUserKey());
            final CustomerEntity customer = account.getProfile().getConsumer();

            this.ensureCustomer(customer, command.getUserKey());

            final String           mangoPayUserId      = customer.getPaymentProviderUser();
            final CardRegistration registrationRequest = new CardRegistration();

            registrationRequest.setUserId(mangoPayUserId);
            registrationRequest.setCurrency(CurrencyIso.EUR);
            registrationRequest.setCardType(CardType.CB_VISA_MASTERCARD);

            final CardRegistration registrationResponse = this.api.getCardRegistrationApi().create(registrationRequest);

            return CardRegistrationDto.from(registrationResponse);
        } catch (final Exception ex) {
            throw this.wrapException("Create Card Registration", ex, command);
        }
    }

    @Override
    public String registerCard(CardRegistrationCommandDto command) {
        try {
            final AccountEntity  account  = this.getAccount(command.getUserKey());
            final CustomerEntity customer = account.getProfile().getConsumer();

            this.ensureCustomer(customer, command.getUserKey());

            final CardRegistration registrationRequest = new CardRegistration();
            registrationRequest.setId(command.getRegistrationId());
            registrationRequest.setRegistrationData(command.getRegistrationData());

            final CardRegistration registrationResponse = this.api.getCardRegistrationApi().update(registrationRequest);

            return registrationResponse.getCardId();
        } catch (final Exception ex) {
            throw this.wrapException("Update Card Registration", ex, command);
        }
    }

    @Override
    public OrderDto createOrderFromCart(CartDto cart, Location location) throws PaymentException {
        try {
            if (cart == null || cart.getItems().size() == 0) {
                throw new PaymentException(PaymentMessageCode.CART_IS_EMPTY, "Cart is empty");
            }
            if (cart.getItems().size() != 1) {
                throw new PaymentException(PaymentMessageCode.CART_MAX_SIZE, "Cart must contain only one item");
            }

            final CartItemDto cartItem = cart.getItems().get(0);

            // Cart item must be a catalogue published item
            final CatalogueItemDetailsDto asset = this.catalogueService.findOne(null, cartItem.getAssetId(), null, false);
            if (asset == null) {
                throw new PaymentException(PaymentMessageCode.ASSET_NOT_FOUND, "Asset not found");
            }
            if (!asset.isAvailableToPurchase()) {
                throw new PaymentException(PaymentMessageCode.ASSET_PROVIDER_NOT_KYC_VALIDATED, "Asset not available to purchase");
            }
            final boolean vettingRequired           = BooleanUtils.isTrue(asset.getVettingRequired());
            final boolean contractUploadingRequired = asset.getContractTemplateType() == EnumContractType.UPLOADED_CONTRACT;

            // Pricing model must exist. We need to check only the pricing model
            // key. Updating the parameters of a pricing model, creates a new key.
            final EffectivePricingModelDto   cartItemPricingModel = cartItem.getPricingModel();
            final BasePricingModelCommandDto assetPricingModel    = asset.getPricingModels().stream()
                .filter(m -> m.getKey().equals(cartItemPricingModel.getModel().getKey()))
                .findFirst()
                .orElse(null);

            if (assetPricingModel == null) {
                throw new PaymentException(PaymentMessageCode.PRICING_MODEL_NOT_FOUND, "Pricing model not found");
            }

            // Create quotation
            final EffectivePricingModelDto quotation = quotationService.createQuotation(
                asset, cartItemPricingModel.getModel().getKey(), cartItemPricingModel.getUserParameters(), false
            );

            // Create command
            final OrderCommand orderCommand = OrderCommand.builder()
                .asset(asset)
                .cartId(cart.getId())
                .contractType(asset.getContractTemplateType())
                .contractUploadingRequired(contractUploadingRequired)
                .deliveryMethod(asset.getDeliveryMethod())
                .location(location)
                .quotation(quotation)
                .userId(cart.getAccountId())
                .vettingRequired(vettingRequired)
                .build();

            final OrderDto order = this.orderRepository.create(orderCommand);

            if (vettingRequired) {
                final AccountEntity consumer = this.orderRepository.findAccountById(cart.getAccountId()).orElse(null);
                this.orderFulfillmentService.sendOrderStatusByMail(EnumMailType.CONSUMER_PURCHASE_NOTIFICATION, consumer.getKey(), order.getKey());

                final EnumNotificationType notificationType = EnumNotificationType.PURCHASE_REMINDER;
                final String               idempotentKey    = order.getKey().toString() + "::" + notificationType.toString();
                final Map<String, Object>  variables        = new HashMap<>();
                variables.put("orderKey", order.getKey().toString());
                variables.put("assetName", asset.getTitle());
                variables.put("assetVersion", asset.getVersion());

                this.orderFulfillmentService.sendOrderStatusByNotification(notificationType, asset.getPublisher().getKey(), variables, idempotentKey);
            }

            return order;
        } catch (final Exception ex) {
            throw this.wrapException("Create Order", ex, cart == null ? "" : cart.getKey());
        }
    }

    @Override
    public PayInDto preparePayInFromServiceBillingRecords(CheckoutServiceBillingCommandDto command) throws PaymentException {
        try {
            // Validate user
            final AccountEntity  account  = this.getAccount(command.getUserKey());
            final CustomerEntity customer = account.getProfile().getConsumer();

            this.ensureCustomer(customer, command.getUserKey());

            // Validate billing records
            final var records = this.getServiceBillingRecords(command.getUserKey(), command.getKeys());

            BigDecimal totalPrice             = BigDecimal.ZERO;
            BigDecimal totalPriceExcludingTax = BigDecimal.ZERO;
            BigDecimal totalTax               = BigDecimal.ZERO;

            for (final var r : records) {
                totalPrice             = totalPrice.add(r.getTotalPrice());
                totalPriceExcludingTax = totalPriceExcludingTax.add(r.getTotalPriceExcludingTax());
                totalTax               = totalTax.add(r.getTotalTax());
            }

            command.setTotalPrice(totalPrice);
            command.setTotalPriceExcludingTax(totalPriceExcludingTax);
            command.setTotalTax(totalTax);

            if (command.getTotalPrice().compareTo(BigDecimal.ZERO) <= 0) {
                throw new PaymentException(PaymentMessageCode.ZERO_AMOUNT, "[TOPIO] PayIn amount must be greater than 0");
            }

            // Create database record
            final ConsumerCardDirectPayInDto result = (ConsumerCardDirectPayInDto) this.payInRepository.prepareCardDirectPayInForServiceBilling(command);

            return result;
        } catch (final Exception ex) {
            throw this.wrapException("Create Card Direct PayIn", ex, command, logger);
        }
    }

    @Override
    public PayInDto getConsumerPayIn(Integer userId, UUID payInKey) {
        final PayInEntity payIn = this.payInRepository.findOneByConsumerIdAndKey(userId, payInKey).orElse(null);

        return payIn == null ? null : payIn.toConsumerDto(true);
    }

    @Override
    public ProviderPayInItemDto getProviderPayInItem(Integer userId, UUID payInKey, Integer index) {
        final PayInItemEntity item = this.payInRepository.findOnePayInItemByProvider(userId, payInKey, index).orElse(null);

        return item == null ? null : item.toProviderDto(true);
    }

    @Override
    public EnumTransactionStatus getPayInStatus(String payIn) throws PaymentException {
        try {
            final PayIn result = this.api.getPayInApi().get(payIn);

            return EnumTransactionStatus.from(result.getStatus());
        } catch (final Exception ex) {
            throw this.wrapException("Get PayIn status", ex, payIn);
        }
    }

    @Override
    public AccountDto refreshUserWallets(UUID userKey) throws PaymentException {
        try {
            final AccountEntity  account  = this.accountRepository.findOneByKey(userKey).orElse(null);
            final CustomerEntity consumer = account.getConsumer();
            final CustomerEntity provider = account.getProvider();

            if (consumer != null) {
                this.ensureCustomer(consumer, userKey);

                final String    walletId = consumer.getPaymentProviderWallet();
                final Wallet    wallet   = this.api.getWalletApi().get(walletId);
                final WalletDto result   = WalletDto.from(wallet);

                consumer.setWalletFunds(result.getAmount());
                consumer.setWalletFundsUpdatedOn(ZonedDateTime.now());
            }
            if (provider != null) {
                this.ensureCustomer(provider, userKey);

                final String    walletId = provider.getPaymentProviderWallet();
                final Wallet    wallet   = this.api.getWalletApi().get(walletId);
                final WalletDto result   = WalletDto.from(wallet);

                provider.setWalletFunds(result.getAmount());
                provider.setWalletFundsUpdatedOn(ZonedDateTime.now());
            }

            this.accountRepository.saveAndFlush(account);

            return account.toDto();
        } catch (final ResponseException ex) {
            logger.error("Failed to load customer wallet", ex);

            throw new PaymentException("[MANGOPAY] Error: " + ex.getApiMessage(), ex);
        } catch (final PaymentException ex) {
            throw ex;
        } catch (final Exception ex) {
            throw this.wrapException("Get Wallet", ex, userKey);
        }
    }

    @Override
    public WalletDto updateCustomerWalletFunds(UUID userKey, EnumCustomerType type) throws PaymentException {
        try {
            final AccountEntity  account  = this.accountRepository.findOneByKey(userKey).orElse(null);
            final CustomerEntity customer = type == EnumCustomerType.CONSUMER ? account.getConsumer() : account.getProvider();

            this.ensureCustomer(customer, userKey);

            final String    walletId = customer.getPaymentProviderWallet();
            final Wallet    wallet   = this.api.getWalletApi().get(walletId);
            final WalletDto result   = WalletDto.from(wallet);

            customer.setWalletFunds(result.getAmount());
            customer.setWalletFundsUpdatedOn(ZonedDateTime.now());

            this.accountRepository.saveAndFlush(account);

            return result;
        } catch (final ResponseException ex) {
            logger.error("Failed to load customer wallet", ex);

            throw new PaymentException("[MANGOPAY] Error: " + ex.getApiMessage(), ex);
        } catch (final PaymentException ex) {
            throw ex;
        } catch (final Exception ex) {
            throw this.wrapException("Get Wallet", ex, userKey);
        }
    }

    @Override
    public PageResultDto<ConsumerPayInDto> findAllConsumerPayIns(
        UUID userKey, EnumTransactionStatus status, int pageIndex, int pageSize, EnumPayInSortField orderBy, EnumSortingOrder order
    ) {
        final Direction direction = order == EnumSortingOrder.DESC ? Direction.DESC : Direction.ASC;

        final PageRequest       pageRequest = PageRequest.of(pageIndex, pageSize, Sort.by(direction, orderBy.getValue()));
        final Page<PayInEntity> page        = this.payInRepository.findAllConsumerPayIns(userKey, null, status, pageRequest);

        final long                   count   = page.getTotalElements();
        final List<ConsumerPayInDto> records = page.getContent().stream()
            .map(p -> p.toConsumerDto(false))
            .collect(Collectors.toList());

        return PageResultDto.of(pageIndex, pageSize, records, count);
    }

    @Override
    public PageResultDto<ProviderPayInItemDto> findAllProviderPayInItems(
        UUID userKey, EnumTransactionStatus status, int pageIndex, int pageSize, EnumPayInItemSortField orderBy, EnumSortingOrder order
    ) {
        final Direction direction = order == EnumSortingOrder.DESC ? Direction.DESC : Direction.ASC;

        final PageRequest           pageRequest = PageRequest.of(pageIndex, pageSize, Sort.by(direction, orderBy.getValue()));
        final Page<PayInItemEntity> page        = this.payInRepository.findAllProviderPayInItems(
            userKey, null, status == null ? null : Set.of(status), pageRequest
        );

        final long                       count   = page.getTotalElements();
        final List<ProviderPayInItemDto> records = page.getContent().stream()
            .map(p -> p.toProviderDto(false))
            .collect(Collectors.toList());

        return PageResultDto.of(pageIndex, pageSize, records, count);
    }
    @Override
    public PayInDto createPayInBankwireForOrder(BankwirePayInCommand command) throws PaymentException {
        final var ctx = BankwirePayInExecutionContext.of(command);
        try {
            final var account  = this.getAccount(command.getUserKey());
            final var customer = account.getProfile().getConsumer();
            final var order    = this.orderRepository.findByKey(command.getKey()).orElse(null);

            // Check customer
            this.ensureCustomer(customer, command.getUserKey());

            // Check order
            this.ensureOrderForPayIn(order, command.getKey());

            final String idempotencyKey = order.getKey().toString();

            // Check payment
            final PayInEntity payIn = order.getPayin();
            if (payIn != null) {
                return payIn.toConsumerDto(true);
            }

            // Update command with order properties

            // MANGOPAY expects not decimal values e.g. 100,50 is formatted as a
            // integer 10050
            ctx.setDebitedFunds(order.getTotalPrice());
            ctx.setReferenceNumber(order.getReferenceNumber());

            // Funds must be greater than 0
            if (ctx.getDebitedFunds().compareTo(BigDecimal.ZERO) <= 0) {
                throw new PaymentException(PaymentMessageCode.ZERO_AMOUNT, "[TOPIO] PayIn amount must be greater than 0");
            }
            // Bank wire PayIns are allowed only for assets
            if (order.getItems().stream().filter(i -> i.getType() == EnumOrderItemType.SUBSCRIPTION).findAny().isPresent()) {
                throw new PaymentException(
                    PaymentMessageCode.PAYIN_TYPE_NOT_SUPPORTED,
                    "[TOPIO] Bankwire PayIn is not supported for subscriptions"
                );
            }

            // Check if this is a retry operation
            PayIn payInResponse = this.<PayIn>getResponse(idempotencyKey);

            // Create a new PayIn if needed
            if (payInResponse == null) {
                final PayIn payInRequest = this.createBankWirePayIn(customer, ctx);

                payInResponse = this.api.getPayInApi().create(idempotencyKey, payInRequest);
            }

            // Update command with payment information
            final PayInPaymentDetailsBankWire paymentDetails = (PayInPaymentDetailsBankWire) payInResponse.getPaymentDetails();

            ctx.setPayIn(payInResponse.getId());
            ctx.setWireReference(paymentDetails.getWireReference());
            ctx.setBankAccount(BankAccountDto.from(paymentDetails.getBankAccount()));
            // MANGOPAY returns dates as integer numbers that represent the
            // number of seconds since the Unix Epoch
            ctx.setCreatedOn(ZonedDateTime.ofInstant(Instant.ofEpochSecond(payInResponse.getCreationDate()), ZoneOffset.UTC));

            // Create database record
            final PayInDto result = this.payInRepository.createBankwirePayInForOrder(ctx);
            // Link PayIn record to order
            this.orderRepository.setPayIn(command.getKey(), result.getPayIn(), account.getKey());

            return result;
        } catch (final Exception ex) {
            throw this.wrapException("Create PayIn BankWire", ex, ctx, logger);
        }
    }

    @Override
    public PayInDto createPayInFreeForOrder(FreePayInCommand command) throws PaymentException {
        final var ctx = FreePayInExecutionContext.of(command);
        try {
            final var account  = this.getAccount(command.getUserKey());
            final var customer = account.getProfile().getConsumer();
            final var order    = this.orderRepository.findByKey(command.getKey()).orElse(null);

            // Check customer
            this.ensureCustomer(customer, command.getUserKey());

            // Check order
            this.ensureOrderForPayIn(order, command.getKey());

            // Funds must be greater than 0
            if (order.getTotalPrice().longValue() > 0) {
                throw new PaymentException(PaymentMessageCode.NON_ZERO_AMOUNT, "[TOPIO] PayIn amount must be equal to 0");
            }

            // Check payment
            final PayInEntity payIn = order.getPayin();
            if (payIn != null) {
                return payIn.toConsumerDto(true);
            }

            ctx.setReferenceNumber(order.getReferenceNumber());

            // Create database record
            final PayInDto result = this.payInRepository.createFreePayInForOrder(ctx);
            // Link PayIn record to order
            this.orderRepository.setPayIn(command.getKey(), result.getPayIn(), account.getKey());

            return result;
        } catch (final Exception ex) {
            throw this.wrapException("Create PayIn Free", ex, ctx, logger);
        }
    }

    @Override
    public PayInDto createPayInCardDirectForOrder(CardDirectPayInCommand command) throws PaymentException {
        final var ctx = CardDirectPayInExecutionContext.of(command);
        try {
            final var account  = this.getAccount(command.getUserKey());
            final var customer = account.getProfile().getConsumer();
            final var order    = this.orderRepository.findByKey(command.getKey()).orElse(null);

            // Check customer
            this.ensureCustomer(customer, command.getUserKey());

            // Check order
            this.ensureOrderForPayIn(order, command.getKey());

            // Set command properties from customer/order
            // MANGOPAY expects not decimal values e.g. 100,50 is formatted as a
            // integer 10050

            ctx.setCreditedUserId(customer.getPaymentProviderUser());
            ctx.setCreditedWalletId(customer.getPaymentProviderWallet());
            ctx.setDebitedFunds(order.getTotalPrice());
            ctx.setIdempotencyKey(order.getKey().toString());
            ctx.setRecurringTransactionType(EnumRecurringPaymentType.NONE);
            ctx.setReferenceNumber(order.getReferenceNumber());
            ctx.setStatementDescriptor(MangopayUtils.createStatementDescriptor(ctx));

            // Configure recurring payments
            this.configureRecurringPayment(order, ctx);

            if (ctx.isRecurring()) {
                return this.createCardDirectRecurringPaymentForSubscription(ctx);
            } else {
                return this.createCardDirectPaymentForOrder(ctx, order);
            }
        } catch (final PaymentException ex) {
            throw ex;
        } catch (final Exception ex) {
            throw this.wrapException("Create PayIn  Card Direct", ex, ctx, logger);
        }
    }

    @Override
    public PayInDto updatePayInCardDirectForSubscriptions(CardDirectPayInCommand command) throws PaymentException {
        final var ctx = CardDirectPayInExecutionContext.of(command);
        try {
            // Validate command and set card alias
            final CardDto card = this.getCardRegistration(UserCardCommand.of(command.getUserKey(), command.getCardId()));
            if (card == null) {
                throw new PaymentException(PaymentMessageCode.CARD_NOT_FOUND, "Card registration was not found");
            } else {
                ctx.setCardAlias(card.getAlias());
            }

            // Get PayIn
            final PayInEntity payIn = this.payInRepository.findOnePrepared(command.getUserKey(), command.getKey()).orElse(null);
            if (payIn == null) {
                throw new PaymentException(PaymentMessageCode.PAYIN_NOT_FOUND, "Payin record was not found");
            }
            final CustomerEntity customer = payIn.getConsumer().getProfile().getConsumer();

            // Update command from database PayIn record
            ctx.setCreditedUserId(customer.getPaymentProviderUser());
            ctx.setCreditedWalletId(customer.getPaymentProviderWallet());
            ctx.setDebitedFunds(payIn.getTotalPrice());
            ctx.setIdempotencyKey(payIn.getKey().toString());
            ctx.setRecurringTransactionType(EnumRecurringPaymentType.NONE);
            ctx.setReferenceNumber(payIn.getReferenceNumber());
            ctx.setStatementDescriptor(MangopayUtils.createStatementDescriptor(ctx));

            // Check if this is a retry operation
            PayIn payInResponse = this.<PayIn>getResponse(payIn.getKey().toString());

            // Create a new PayIn if needed
            if (payInResponse == null) {
                final PayIn payInRequest = this.createCardDirectPayIn(ctx);

                payInResponse = this.api.getPayInApi().create(ctx.getIdempotencyKey(), payInRequest);
            }

            // Update command with payment information
            final PayInExecutionDetailsDirect executionDetails = (PayInExecutionDetailsDirect) payInResponse.getExecutionDetails();

            ctx.setApplied3dsVersion(executionDetails.getApplied3DSVersion());
            ctx.setPayIn(payInResponse.getId());
            ctx.setRequested3dsVersion(executionDetails.getRequested3DSVersion());
            ctx.setResultCode(payInResponse.getResultCode());
            ctx.setResultMessage(payInResponse.getResultMessage());
            ctx.setStatus(EnumTransactionStatus.from(payInResponse.getStatus()));
            // MANGOPAY returns dates as integer numbers that represent the
            // number of seconds since the Unix Epoch
            ctx.setCreatedOn(ZonedDateTime.ofInstant(Instant.ofEpochSecond(payInResponse.getCreationDate()), ZoneOffset.UTC));
            // For Card Direct PayIns, if no 3-D Secure validation is required,
            // the transaction may be executed immediately
            if (payInResponse.getExecutionDate() != null) {
                ctx.setExecutedOn(ZonedDateTime.ofInstant(Instant.ofEpochSecond(payInResponse.getExecutionDate()), ZoneOffset.UTC));
            }

            // Create database record
            ConsumerCardDirectPayInDto result = null;

            if (StringUtils.isEmpty(payIn.getPayIn())) {
                result = (ConsumerCardDirectPayInDto) this.payInRepository.updateCardDirectPayInForServiceBilling(ctx);
            } else {
                result = (ConsumerCardDirectPayInDto) payIn.toConsumerDto(true);
            }

            // Add client-only information (card alias is never saved in our
            // database)
            result.setAlias(ctx.getCardAlias());

            /*
             * Since we have set SecureMode=FORCE, we expect SecureModeNeeded to
             * be TRUE and a non-null SecureModeRedirectUrl returned by the API
             * call
             *
             * See: https://docs.mangopay.com/guide/3ds2-integration
             */
            result.setSecureModeRedirectURL(executionDetails.getSecureModeRedirectUrl());

            return result;
        } catch (final Exception ex) {
            throw this.wrapException("Create Card Direct PayIn", ex, ctx, logger);
        }

    }

    private List<ServiceBillingDto> getServiceBillingRecords(UUID userKey, List<UUID> keys) {
        if (CollectionUtils.isEmpty(keys)) {
            throw new PaymentException(
                PaymentMessageCode.SUBSCRIPTION_BILLING_SELECTION_EMPTY,
                "No subscription billing record is selected"
            );
        }

        final var records = serviceBillingRepository.findAllObjectsByKey(EnumView.HELPDESK, keys, true);
        if (records.size() != keys.size()) {
            throw new PaymentException(
                PaymentMessageCode.SERVICE_BILLING_RECORD_NOT_FOUND,
                "One or more subscription billing records were not found"
            );
        }

        // A record refers either to a subscription whose consumer is the
        // selected user or to a private OGC service whose owner parent is the
        // billed account. The user key is compared to the owner parent to
        // include VENDOR account private services
        final var unauthorizedKeys = records.stream()
            .filter(r -> !userKey.equals(r.getConsumerKey()) && !userKey.equals(r.getProviderParentKey()))
            .toList();
        if (!unauthorizedKeys.isEmpty()) {
            throw new PaymentException(
                PaymentMessageCode.SERVICE_BILLING_RECORD_ACCESS_DENIED,
                "One or more subscription billing records do not belong to the authenticated user"
            );
        }

        final var invalidStatusRecords = records.stream().filter(r -> r.getStatus() != EnumPayoffStatus.DUE).toList();
        if (!invalidStatusRecords.isEmpty()) {
            throw new PaymentException(
                PaymentMessageCode.SERVICE_BILLING_RECORD_INVALID_STATUS,
                "One or more subscription billing records are not due and cannot included to the order"
            );
        }

        return records;
    }

    private PayInDto createCardDirectRecurringPaymentForSubscription(CardDirectPayInExecutionContext ctx) throws PaymentException {
        final var command                    = ctx.getCommand();
        final var idempotencyKeyPrefix       = ctx.getIdempotencyKey().replace("-", "");
        final var idempotencyKeyRegistration = idempotencyKeyPrefix + "R";
        final var idempotencyKeyPayment      = idempotencyKeyPrefix + "P";

        // Create idempotency keys for registration and payment operations

        // See https://docs.mangopay.com/guide/idempotency-support
        Assert.isTrue(idempotencyKeyRegistration.length() <= 36, "Idempotency Key must be between 16 and 36 characters");
        Assert.isTrue(idempotencyKeyPayment.length() <= 36, "Idempotency Key must be between 16 and 36 characters");

        // Get card
        final CardDto card = this.getCardRegistration(UserCardCommand.of(command.getUserKey(), command.getCardId()));

        // Update command with order properties
        if (card == null) {
            throw new PaymentException(PaymentMessageCode.CARD_NOT_FOUND, "Card registration was not found");
        } else {
            ctx.setCardAlias(card.getAlias());
        }

        // Funds must be greater than 0
        if (ctx.getDebitedFunds().compareTo(BigDecimal.ZERO) <= 0) {
            throw new PaymentException(PaymentMessageCode.ZERO_AMOUNT, "[TOPIO] PayIn amount must be greater than 0");
        }

        final RecurringRegistrationCreateCommand registrationCommand = RecurringRegistrationCreateCommand.builder()
            .authorId(ctx.getCreditedUserId())
            .billingAddress(command.getBilling())
            .cardId(card.getId())
            .creditedWalletId(ctx.getCreditedWalletId())
            .endDate(null)
            .firstTransactionDebitedFunds(ctx.getDebitedFunds())
            .frequency(ctx.getRecurringPaymentFrequency())
            .idempotencyKey(idempotencyKeyRegistration)
            .migrate(false)
            .orderKey(command.getKey())
            .shippingAddress(command.getShipping())
            .userKey(command.getUserKey())
            .build();

        final RecurringRegistrationDto registration = this.recurringPaymentService.initializeRegistration(registrationCommand);
        ctx.setIdempotencyKey(idempotencyKeyPayment);
        ctx.setRecurringPayinRegistrationId(registration.getProviderRegistration());
        ctx.setRecurringTransactionType(EnumRecurringPaymentType.CIT);

        final PayInDto result = this.recurringPaymentService.createConsumerPayIn(ctx);

        return result;
    }

    private PayInDto createCardDirectPaymentForOrder(CardDirectPayInExecutionContext ctx, OrderEntity order) throws PaymentException {
        try {
            final var command = ctx.getCommand();

            // Validate command and set card alias
            final CardDto card = this.getCardRegistration(UserCardCommand.of(command.getUserKey(), command.getCardId()));
            if (card == null) {
                throw new PaymentException(PaymentMessageCode.CARD_NOT_FOUND, "Card registration was not found");
            } else {
                ctx.setCardAlias(card.getAlias());
            }

            if (ctx.getDebitedFunds().compareTo(BigDecimal.ZERO) <= 0) {
                throw new PaymentException(PaymentMessageCode.ZERO_AMOUNT, "[TOPIO] PayIn amount must be greater than 0");
            }

            // Check if this is a retry operation
            PayIn payInResponse = this.<PayIn>getResponse(ctx.getIdempotencyKey());

            // Create a new PayIn if needed
            if (payInResponse == null) {
                final PayIn payInRequest = this.createCardDirectPayIn(ctx);

                payInResponse = this.api.getPayInApi().create(ctx.getIdempotencyKey(), payInRequest);
            }

            // Update command with payment information
            final PayInExecutionDetailsDirect executionDetails = (PayInExecutionDetailsDirect) payInResponse.getExecutionDetails();

            ctx.setApplied3dsVersion(executionDetails.getApplied3DSVersion());
            ctx.setPayIn(payInResponse.getId());
            ctx.setRequested3dsVersion(executionDetails.getRequested3DSVersion());
            ctx.setResultCode(payInResponse.getResultCode());
            ctx.setResultMessage(payInResponse.getResultMessage());
            ctx.setStatus(EnumTransactionStatus.from(payInResponse.getStatus()));
            // MANGOPAY returns dates as integer numbers that represent the
            // number of seconds since the Unix Epoch
            ctx.setCreatedOn(ZonedDateTime.ofInstant(Instant.ofEpochSecond(payInResponse.getCreationDate()), ZoneOffset.UTC));
            // For Card Direct PayIns, if no 3-D Secure validation is required,
            // the transaction may be executed immediately
            if (payInResponse.getExecutionDate() != null) {
                ctx.setExecutedOn(ZonedDateTime.ofInstant(Instant.ofEpochSecond(payInResponse.getExecutionDate()), ZoneOffset.UTC));
            }

            // Create database record
            final PayInEntity          payIn  = order.getPayin();
            ConsumerCardDirectPayInDto result = null;
            if (payIn != null) {
                result = (ConsumerCardDirectPayInDto) payIn.toConsumerDto(true);
            } else {
                result = (ConsumerCardDirectPayInDto) this.payInRepository.createCardDirectPayInForOrder(ctx);

                // Link PayIn record to order
                this.orderRepository.setPayIn(command.getKey(), result.getPayIn(), command.getUserKey());

                // Update order status if we have a valid response i.e.
                // 3D-Secure validation was skipped
                if (result.getStatus() != EnumTransactionStatus.CREATED) {
                    this.orderRepository.setStatus(
                        command.getKey(),
                        result.getStatus().toOrderStatus(order.getDeliveryMethod())
                    );
                }
            }

            // Add client-only information (card alias is never saved in our
            // database)
            result.setAlias(ctx.getCardAlias());

            /*
             * Since we have set SecureMode=FORCE, we expect SecureModeNeeded to
             * be TRUE and a non-null SecureModeRedirectUrl returned by the API
             * call
             *
             * See: https://docs.mangopay.com/guide/3ds2-integration
             */
            result.setSecureModeRedirectURL(executionDetails.getSecureModeRedirectUrl());

            return result;
        } catch (final Exception ex) {
            throw this.wrapException("Create Card Direct PayIn", ex, ctx, logger);
        }
    }

    @Override
    public void updateWorkflowInstancePayInStatus(UUID payInKey, String payInId) throws PaymentException {
        try {
            final PayInEntity payInEntity = this.ensurePayIn(payInId);

            final PayIn payInObject = this.api.getPayInApi().get(payInId);

            // Update order fulfillment workflow instance only if status has been modified
            if (payInEntity.getStatus() != EnumTransactionStatus.from(payInObject.getStatus())) {
                this.orderFulfillmentService.sendPayInStatusUpdateMessage(
                    payInEntity.getKey(),
                    EnumTransactionStatus.from(payInObject.getStatus())
                );
            }
        } catch (final Exception ex) {
            throw this.wrapException("Update PayIn", ex, payInId);
        }
    }

    @Override
    public PayInDto updatePayIn(UUID payInKey, String providerPayInId) throws PaymentException {
        try {
            // Ensure that the PayIn record exists in our database
            final PayInEntity payInEntity = this.ensurePayIn(providerPayInId);
            // Fetch PayIn object from the Payment Provider (MANGOPAY)
            final PayIn payInObject = this.api.getPayInApi().get(providerPayInId);

            // Update PayIn local instance only
            final PayInStatusUpdateCommand command = PayInStatusUpdateCommand.builder()
                .providerPayInId(providerPayInId)
                .executedOn(this.timestampToDate(payInObject.getExecutionDate()))
                .status(EnumTransactionStatus.from(payInObject.getStatus()))
                .resultCode(payInObject.getResultCode())
                .resultMessage(payInObject.getResultMessage())
                .build();

            final HelpdeskPayInDto result = this.payInRepository.updatePayInStatus(command);

            // Update history table only for succeeded PayIns
            if (result.getStatus() == EnumTransactionStatus.SUCCEEDED) {
                for (final PayInItemEntity item : payInEntity.getItems()) {
                    // Create a history item only the first time this method is
                    // invoked
                    payInItemHistoryRepository.createIfNotExists(item);
                }
            }

            // Update order status
            for (final PayInItemDto item : result.getItems()) {
                if(item.getType() == EnumPaymentItemType.ORDER) {
                    final HelpdeskOrderDto order = ((HelpdeskOrderPayInItemDto) item).getOrder();
                    this.orderRepository.setStatus(
                        order.getKey(),
                        result.getStatus().toOrderStatus(order.getDeliveryMethod())
                    );
                }
            }

            // Update consumer wallet if PayIn was successful
            if (result.getStatus() == EnumTransactionStatus.SUCCEEDED) {
                this.updateCustomerWalletFunds(payInEntity.getConsumer().getKey(), EnumCustomerType.CONSUMER);
            }

            // Update recurring PayIn registration status (if one is linked to
            // the updated PayIn)
            if (payInEntity instanceof CardDirectPayInEntity) {
                final CardDirectPayInEntity            cardPayInEntity = (CardDirectPayInEntity) payInEntity;
                final PayInRecurringRegistrationEntity registration    = cardPayInEntity.getRecurringPayment();

                if (registration != null) {
                    this.recurringPaymentService.updateStatus(registration.getProviderRegistration());
                }
            }

            return result;
        } catch (final Exception ex) {
            throw this.wrapException("Update PayIn", ex, providerPayInId);
        }
    }

    @Override
    public List<TransferDto> createTransfer(UUID userKey, UUID payInKey) throws PaymentException {
        try {
            final List<TransferDto> transfers = new ArrayList<>();

            // PayIn must exist with a transaction status SUCCEEDED
            final PayInEntity payIn = this.payInRepository.findOneEntityByKey(payInKey).orElse(null);

            if (payIn == null) {
                throw new PaymentException(
                    PaymentMessageCode.SERVER_ERROR,
                    String.format("[MANGOPAY] PayIn was not found [key=%s]", payInKey)
                );
            }
            if (payIn.getStatus() != EnumTransactionStatus.SUCCEEDED) {
                throw new PaymentException(PaymentMessageCode.SERVER_ERROR, String.format(
                    "[MANGOPAY] PayIn invalid status [key=%s, status=%s, expected=%s]",
                    payInKey, payIn.getStatus(), EnumTransactionStatus.SUCCEEDED
                ));
            }

            // Get debit customer
            final AccountEntity  debitAccount   = payIn.getConsumer();
            final CustomerEntity debitCustomer  = debitAccount.getProfile().getConsumer();

            if (debitCustomer == null) {
                throw new PaymentException(PaymentMessageCode.SERVER_ERROR, String.format(
                    "[MANGOPAY] Debit customer for PayIn was not found [key=%s]",
                    payInKey
                ));
            }

            // Process every item
            for (final PayInItemEntity item : payIn.getItems()) {
                if (!StringUtils.isBlank(item.getTransfer())) {
                    // If a valid transfer transaction identifier exists, this
                    // is a retry operation
                    transfers.add(item.toTransferDto(true));
                    continue;
                }
                final String idempotencyKey = item.getTransferKey().toString();
                TransferDto  transfer       = null;
                switch (item.getType()) {
                    case ORDER :
                        transfer = this.createTransferForOrder(
                            idempotencyKey, payInKey, (PayInOrderItemEntity) item, debitCustomer
                        );
                        break;
                    case SERVICE_BILLING :
                        transfer = this.createTransferForServiceBilling(
                            idempotencyKey, payInKey, (PayInServiceBillingItemEntity) item, debitCustomer
                        );
                        break;
                    default :
                        throw new PaymentException(PaymentMessageCode.SERVER_ERROR, String.format(
                            "[MANGOPAY] PayIn item type not supported [key=%s, index=%d, type=%s]",
                            payInKey, item.getId(), item.getType()
                        ));
                }

                if (transfer != null) {
                    // Update item
                    item.updateTransfer(transfer);

                    // If transfer is successful, update item history record
                    if (transfer.getStatus() == EnumTransactionStatus.SUCCEEDED) {
                        switch (item.getType()) {
                            case ORDER :
                                // For order items, transfer data is stored with
                                // payment analytics data
                                this.payInItemHistoryRepository.updateTransfer(item.getId(), transfer);
                                break;

                            case SERVICE_BILLING :
                                // For service billing items, transfer data is
                                // stored with the billing record
                                final var serviceItem = (PayInServiceBillingItemEntity) item;
                                this.serviceBillingRepository.updateTransfer(serviceItem.getServiceBilling().getId(), transfer);
                                break;

                            default :
                                throw new PaymentException(PaymentMessageCode.SERVER_ERROR, String.format(
                                    "[MANGOPAY] PayIn item type not supported [key=%s, index=%d, type=%s]", 
                                    payInKey, item.getId(), item.getType())
                                );
                        }
                    }

                    transfer.setKey(item.getTransferKey());
                    transfers.add(transfer);

                    // Update provider wallet
                    if (transfer.getStatus() == EnumTransactionStatus.SUCCEEDED) {
                        final UUID providerKey = switch (item.getType()) {
                            case ORDER -> 
                                item.getProvider().getKey();

                            case SERVICE_BILLING -> {
                                final var serviceItem = (PayInServiceBillingItemEntity) item;
                                yield switch (serviceItem.getServiceBilling().getType()) {
                                    case SUBSCRIPTION -> 
                                        serviceItem.getServiceBilling().getSubscription().getProvider().getKey();
                                        
                                    case PRIVATE_OGC_SERVICE -> 
                                        this.accountRepository.findById(topioAccountId).get().getKey();
                                };
                            }
                        };
                        this.updateCustomerWalletFunds(providerKey, EnumCustomerType.PROVIDER);
                    }
                }
            }

            this.payInRepository.saveAndFlush(payIn);

            // Update consumer wallet if at least one transfer exists
            if (!transfers.isEmpty()) {
                this.updateCustomerWalletFunds(debitAccount.getKey(), EnumCustomerType.CONSUMER);
            }

            return transfers;
        } catch (final Exception ex) {
            throw this.wrapException(String.format("PayIn transfer creation has failed [key=%s]", payInKey), ex);
        }
    }

    private TransferDto createTransferForOrder(
        String idempotencyKey, UUID payInKey, PayInOrderItemEntity item, CustomerEntity debitCustomer
    ) throws Exception {
        Assert.isTrue(item.getOrder() != null, "Expected a non-null order");
        Assert.isTrue(item.getOrder().getStatus() == EnumOrderStatus.SUCCEEDED, "Expected order status to be SUCCEEDED");
        Assert.isTrue(item.getOrder().getItems() != null, "Expected a non-null items collection");
        Assert.isTrue(item.getOrder().getItems().size() == 1, "Expected only a single item in the order");

        // Get credit customer
        final AccountEntity  creditAccount  = item.getOrder().getItems().get(0).getProvider();
        final CustomerEntity creditCustomer = creditAccount.getProfile().getProvider();
        final BigDecimal     amount         = item.getOrder().getTotalPrice().multiply(BigDecimal.valueOf(100L));
        final BigDecimal     fees           = item.getOrder().getTotalPrice()
            .multiply(this.feePercent)
            .divide(BigDecimal.valueOf(100L))
            .setScale(2, RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(100L));

        if (creditCustomer == null) {
            throw new PaymentException(PaymentMessageCode.SERVER_ERROR, String.format(
                "[MANGOPAY] Credit customer for PayIn item was not found [key=%s, index=%d]",
                payInKey, item.getIndex()
            ));
        }

        // Check if this is a retry operation
        Transfer transferResponse = this.<Transfer>getResponse(idempotencyKey);

        // Create a new transfer if needed
        if (transferResponse == null) {
            final Transfer transferRequest = this.createTransfer(
                idempotencyKey, debitCustomer, creditCustomer, amount, fees
            );

            transferResponse = this.api.getTransferApi().create(idempotencyKey, transferRequest);
        }

        final TransferDto result = this.transferResponseToDto(transferResponse);

        return result;
    }

    private TransferDto createTransferForServiceBilling(
        String idempotencyKey, UUID payInKey, PayInServiceBillingItemEntity item, CustomerEntity debitCustomer
    ) throws Exception {
        Assert.isTrue(item.getServiceBilling() != null, "Expected a non-null subscription billing record");

        // Get credit customer
        final AccountEntity creditAccount = switch (item.getServiceBilling().getType()) {
            case SUBSCRIPTION -> 
                item.getServiceBilling().getSubscription().getProvider();

            case PRIVATE_OGC_SERVICE -> 
                this.accountRepository.findById(topioAccountId).orElse(null);

            default -> 
                throw new PaymentException(PaymentMessageCode.SERVER_ERROR, String.format(
                    "[MANGOPAY] Service billing item type not supported [payInKey=%s, index=%d, type=%s]", 
                    payInKey, item.getId(), item.getServiceBilling().getType())
                );
        };
        
        final var creditCustomer = creditAccount.getProfile().getProvider();
        final var amount         = item.getServiceBilling().getTotalPrice().multiply(BigDecimal.valueOf(100L));
        final var fees           = switch (item.getServiceBilling().getType()) {
            case SUBSCRIPTION -> item.getServiceBilling().getTotalPrice()     
                .multiply(this.feePercent)
                .divide(BigDecimal.valueOf(100L))
                .setScale(2, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100L));
            
             // For private OGC services, the provider
             // is the Topio platform; Hence all funds
             // are credited to the platform wallet as
             // fees
            case PRIVATE_OGC_SERVICE -> 
                amount;

            default -> 
                throw new PaymentException(PaymentMessageCode.SERVER_ERROR, String.format(
                    "[MANGOPAY] Service billing item type not supported [payInKey=%s, index=%d, type=%s]", 
                    payInKey, item.getId(), item.getServiceBilling().getType())
                );
        };

        if (creditCustomer == null) {
            throw new PaymentException(PaymentMessageCode.SERVER_ERROR, String.format(
                "[MANGOPAY] Credit customer for PayIn item was not found [key=%s, index=%d]",
                payInKey, item.getIndex()
            ));
        }

        // Check if this is a retry operation
        Transfer transferResponse = this.<Transfer>getResponse(idempotencyKey);

        // Create a new transfer if needed
        if (transferResponse == null) {
            final Transfer transferRequest = this.createTransfer(
                idempotencyKey, debitCustomer, creditCustomer, amount, fees
            );

            transferResponse = this.api.getTransferApi().create(idempotencyKey, transferRequest);
        }

        final TransferDto result = this.transferResponseToDto(transferResponse);

        return result;
    }

    @Override
    public void updateTransfer(String transferId) throws PaymentException {
        try {
            final PayInItemEntity payInItemEntity = this.ensurePayInItemTransfer(transferId);
            final PayInEntity     payInEntity     = payInItemEntity.getPayin();

            final Transfer    transferResponse = this.api.getTransferApi().get(transferId);
            final TransferDto transferObject   = this.transferResponseToDto(transferResponse);

            // Handle redundant updates
            if (payInItemEntity.getTransferStatus() == transferObject.getStatus()) {
                return;
            }

            // Update item
            payInItemEntity.updateTransfer(transferObject);

            this.payInRepository.saveAndFlush(payInEntity);

            // Always update history when a transfer is updated. A failed
            // transfer may be retried and succeed.
            payInItemHistoryRepository.updateTransfer(payInItemEntity.getId(), transferObject);
        } catch (final Exception ex) {
            throw this.wrapException("Update Transfer", ex, transferId);
        }
    }

    @Override
    public PayOutDto createPayOutAtOpertusMundi(PayOutCommandDto command) throws PaymentException {
        try {
            // Account with provider role must exist
            final AccountEntity              account  = this.getAccount(command.getProviderKey());
            final CustomerProfessionalEntity provider = account.getProfile().getProvider();

            if (provider == null) {
                throw new PaymentException(
                    PaymentMessageCode.SERVER_ERROR,
                    String.format("[MANGOPAY] Provider was not found for account [key=%s]", command.getProviderKey())
                );
            }

            // No pending PayOut records must exist
            final long pending = this.payOutRepository.countProviderPendingPayOuts(command.getProviderKey());
            if (pending != 0) {
                throw new PaymentException(
                    PaymentMessageCode.VALIDATION_ERROR,
                    "Pending PayOut has been found. Wait until the current operation is completed"
                );
            }

            // Refresh provider's wallet from the payment provider
            this.updateCustomerWalletFunds(command.getProviderKey(), EnumCustomerType.PROVIDER);

            // Funds must exist
            if (provider.getWalletFunds().compareTo(command.getDebitedFunds()) < 0) {
                throw new PaymentException(PaymentMessageCode.VALIDATION_ERROR, "Not enough funds. Check wallet balance");
            }
            // Fees are applied in Transfers.
            command.setFees(BigDecimal.ZERO);
            // Set bank account
            command.setBankAccount(provider.getBankAccount().clone());

            // Update provider pending PayOut funds
            provider.setPendingPayoutFunds(provider.getPendingPayoutFunds().add(command.getDebitedFunds()));
            provider.setPendingPayoutFundsUpdatedOn(ZonedDateTime.now());
            this.accountRepository.saveAndFlush(account);

            final PayOutDto payout = this.payOutRepository.createPayOut(command);

            // Start PayOut workflow instance
            this.payOutService.start(command.getAdminUserKey(), payout.getKey());

            return payout;
        } catch (final Exception ex) {
            throw this.wrapException("Create PayOut", ex, command);
        }
    }

    @Override
    @Retryable(include = {PaymentException.class}, maxAttempts = 3, backoff = @Backoff(delay = 2000, maxDelay = 3000))
    public PayOutDto createPayOutAtProvider(UUID payOutKey) throws PaymentException {
        try {
            final PayOutEntity payOut = this.payOutRepository.findOneEntityByKey(payOutKey).orElse(null);

            if (payOut == null) {
                throw new PaymentException(
                    PaymentMessageCode.SERVER_ERROR,
                    String.format("[MANGOPAY] PayOut was not found [key=%s]", payOutKey)
                );
            }

            final String idempotencyKey = payOut.getKey().toString();

            // Check if this is a retry operation
            PayOut payoutResponse = this.<PayOut>getResponse(idempotencyKey);

            // Create a new PayPout if needed
            if (payoutResponse == null) {
                final PayOut payOutRequest = this.createPayOut(payOut);

                payoutResponse = this.api.getPayOutApi().create(idempotencyKey, payOutRequest);
            }

            final PayOutStatusUpdateCommand update = PayOutStatusUpdateCommand.builder()
                .createdOn(this.timestampToDate(payoutResponse.getCreationDate()))
                .executedOn(this.timestampToDate(payoutResponse.getExecutionDate()))
                .key(payOutKey)
                .providerPayOutId(payoutResponse.getId())
                .resultCode(payoutResponse.getResultCode())
                .resultMessage(payoutResponse.getResultMessage())
                .status(EnumTransactionStatus.from(payoutResponse.getStatus()))
                .build();

            this.payOutRepository.updatePayOutStatus(update);

            // Pending funds will be updated once the web hook event is received

            return payOut.toDto(true);
        } catch (final Exception ex) {
            throw this.wrapException("Create MANGOPAY PayOut", ex, payOutKey);
        }
    }

    @Override
    public void sendPayOutStatusUpdateMessage(String payOutId) throws PaymentException {
        try {
            final PayOutEntity payOutEntity = this.ensurePayOut(payOutId);

            final PayOut payOutObject = this.api.getPayOutApi().get(payOutId);

            // Update workflow instance only if status has been modified
            if (payOutEntity.getStatus() != EnumTransactionStatus.from(payOutObject.getStatus())) {
                this.payOutService.sendPayOutStatusUpdateMessage(
                    payOutEntity.getKey(),
                    EnumTransactionStatus.from(payOutObject.getStatus())
                );
            }
        } catch (final Exception ex) {
            throw this.wrapException("Update PayIn", ex, payOutId);
        }
    }

    @Override
    public PayOutDto updatePayOut(UUID payOutKey, String payOutId) throws PaymentException {
        try {
            // Ensure that the PayOut record exists in our database
            final PayOutEntity payOutEntity = this.ensurePayOut(payOutId);

            Assert.isTrue(payOutKey.equals(payOutEntity.getKey()), String.format(
                "Expected PayOut entity key to match parameter payOutKey [key=%s, payOutKey=%s]" ,
                payOutEntity.getKey(), payOutKey
            ));

            // Fetch PayIn object from the Payment Provider (MANGOPAY)
            final PayOut payOutObject = this.api.getPayOutApi().get(payOutId);

            // Update PayIn local instance only
            final PayOutStatusUpdateCommand command = PayOutStatusUpdateCommand.builder()
                .createdOn(this.timestampToDate(payOutObject.getCreationDate()))
                .executedOn(this.timestampToDate(payOutObject.getExecutionDate()))
                .key(payOutEntity.getKey())
                .providerPayOutId(payOutObject.getId())
                .resultCode(payOutObject.getResultCode())
                .resultMessage(payOutObject.getResultMessage())
                .status(EnumTransactionStatus.from(payOutObject.getStatus()))
                .build();

            final PayOutDto result = this.payOutRepository.updatePayOutStatus(command);

            // Update provider pending PayOut funds
            final AccountEntity              account  = payOutEntity.getProvider();
            final CustomerProfessionalEntity provider = account.getProfile().getProvider();

            provider.setPendingPayoutFunds(provider.getPendingPayoutFunds().subtract(payOutEntity.getDebitedFunds()));
            provider.setPendingPayoutFundsUpdatedOn(ZonedDateTime.now());

            this.accountRepository.saveAndFlush(account);

            // Update provider wallet
            this.updateCustomerWalletFunds(payOutEntity.getProvider().getKey(), EnumCustomerType.PROVIDER);

            return result;
        } catch (final Exception ex) {
            throw this.wrapException("Update PayIn", ex, payOutId);
        }
    }

    @Override
    public PayOutDto updatePayOutRefund(String refundId) throws PaymentException {
        try {
            final Refund refund = this.api.getRefundApi().get(refundId);

            final PayOutEntity payOutEntity = this.ensurePayOut(refund.getInitialTransactionId());

            payOutEntity.setRefund(refundId);
            payOutEntity.setRefundCreatedOn(this.timestampToDate(refund.getCreationDate()));
            payOutEntity.setRefundExecutedOn(this.timestampToDate(refund.getExecutionDate()));
            payOutEntity.setRefundReasonMessage(refund.getRefundReason().getRefundReasonMessage());
            payOutEntity.setRefundReasonType(refund.getRefundReason().getRefundReasonType().toString());
            payOutEntity.setRefundStatus(EnumTransactionStatus.from(refund.getStatus()));

            this.payOutRepository.saveAndFlush(payOutEntity);

            return payOutEntity.toDto();
        } catch (final Exception ex) {
            throw this.wrapException("Update MANGOPAY Refund", ex, refundId);
        }
    }

    @Override
    public PayOutDto getProviderPayOut(Integer userId, UUID payOutKey) {
        final PayOutEntity payOut = this.payOutRepository.findOneByAccountIdAndKey(userId, payOutKey).orElse(null);

        return payOut == null ? null : payOut.toDto(false);
    }

    @Override
    public PageResultDto<PayOutDto> findAllProviderPayOuts(
        UUID userKey, EnumTransactionStatus status, int pageIndex, int pageSize, EnumPayOutSortField orderBy, EnumSortingOrder order
    ) {
        final Direction direction = order == EnumSortingOrder.DESC ? Direction.DESC : Direction.ASC;

        final PageRequest        pageRequest = PageRequest.of(pageIndex, pageSize, Sort.by(direction, orderBy.getValue()));
        final Page<PayOutEntity> page        = this.payOutRepository.findAllProviderPayOuts(userKey, status, pageRequest);

        final long           count   = page.getTotalElements();
        final List<PayOutDto> records = page.getContent().stream()
            .map(p -> p.toDto(false))
            .collect(Collectors.toList());

        return PageResultDto.of(pageIndex, pageSize, records, count);
    }

    private PayOut createPayOut(PayOutEntity payOut) {
        final CustomerProfessionalEntity customer = payOut.getProvider().getProvider();

        Assert.notNull(customer, "Expected a non-null provider");

        final String userId        = customer.getPaymentProviderUser();
        final String walletId      = customer.getPaymentProviderWallet();
        final String bankAccountId = payOut.getBankAccount().getId();

        Assert.hasText(userId, "Expected a non-empty provider user id");
        Assert.hasText(walletId, "Expected a non-empty provider wallet id");
        Assert.hasText(bankAccountId, "Expected a non-empty provider bank account id");

        final PayOutPaymentDetailsBankWire details = new PayOutPaymentDetailsBankWire();
        details.setBankAccountId(bankAccountId);
        details.setBankWireRef(payOut.getBankwireRef());
        details.setPayoutModeRequested(PayoutMode.STANDARD);

        final PayOut result = new PayOut();
        result.setAuthorId(userId);
        result.setDebitedFunds(new Money(CurrencyIso.EUR, payOut.getDebitedFunds().multiply(BigDecimal.valueOf(100L)).intValue()));
        result.setDebitedWalletId(walletId);
        result.setFees(new Money(CurrencyIso.EUR, payOut.getPlatformFees().multiply(BigDecimal.valueOf(100L)).intValue()));
        result.setMeanOfPaymentDetails(details);
        result.setPaymentType(PayOutPaymentType.BANK_WIRE);
        result.setTag(payOut.getKey().toString());
        result.setType(TransactionType.PAYOUT);

        return result;
    }

    private Transfer createTransfer(
        String idempotentKey, CustomerEntity debitCustomer, CustomerEntity creditCustomer, BigDecimal amount, BigDecimal fees
    ) {
        final String debitUserId    = debitCustomer.getPaymentProviderUser();
        final String debitWalletId  = debitCustomer.getPaymentProviderWallet();
        final String creditUserId   = creditCustomer.getPaymentProviderUser();
        final String creditWalletId = creditCustomer.getPaymentProviderWallet();

        final Transfer result = new Transfer();
        result.setAuthorId(debitUserId);
        result.setCreditedUserId(creditUserId);
        result.setDebitedFunds(new Money(CurrencyIso.EUR, amount.intValueExact()));
        result.setDebitedWalletId(debitWalletId);
        result.setFees(new Money(CurrencyIso.EUR, fees.intValueExact()));
        result.setCreditedUserId(creditUserId);
        result.setCreditedWalletId(creditWalletId);
        result.setTag(idempotentKey);

        return result;
    }

    private TransferDto transferResponseToDto(Transfer transfer) {
        final TransferDto result = new TransferDto();

        result.setCreatedOn(this.timestampToDate(transfer.getCreationDate()));
        result.setCreditedFunds(BigDecimal.valueOf(transfer.getCreditedFunds().getAmount()).divide(BigDecimal.valueOf(100)));
        result.setExecutedOn(this.timestampToDate(transfer.getExecutionDate()));
        result.setFees(BigDecimal.valueOf(transfer.getFees().getAmount()).divide(BigDecimal.valueOf(100)));
        result.setId(transfer.getId());
        result.setResultCode(transfer.getResultCode());
        result.setResultMessage(transfer.getResultMessage());
        result.setStatus(EnumTransactionStatus.from(transfer.getStatus()));

        return result;
    }

    /**
     * Create a MANGOPAY PayIn object
     *
     * @param command
     * @return
     */
    @Override
    protected PayIn createCardDirectPayIn(CardDirectPayInExecutionContext ctx) {
        final var command        = ctx.getCommand();
        final var paymentDetails = new PayInPaymentDetailsCard();

        paymentDetails.setBrowserInfo(command.getBrowserInfo().toMangoPayBrowserInfo());
        paymentDetails.setCardType(CardType.CB_VISA_MASTERCARD);
        paymentDetails.setCardId(command.getCardId());
        paymentDetails.setIpAddress(command.getIpAddress());
        paymentDetails.setStatementDescriptor(ctx.getStatementDescriptor());
        if (command.getShipping() != null) {
            paymentDetails.setShipping(command.getShipping().toMangoPayShipping());
        }

        final PayInExecutionDetailsDirect executionDetails = new PayInExecutionDetailsDirect();
        if (command.getBilling() != null) {
            executionDetails.setBilling(command.getBilling().toMangoPayBilling());
        }
        executionDetails.setCardId(command.getCardId());

        /*
         * Previously, a transaction (PayIn or PreAuthorization) would generally
         * not be subject to strong customer authentication if
         * SecureMode=DEFAULT and the payment amount was inferior to your 3DS
         * limit. Since 1st January 2021, we can no longer guarantee this
         * frictionless payment experience – including on low amount
         * transactions for card verification.
         *
         * See: https://docs.mangopay.com/guide/3ds2-integration
         */
        executionDetails.setSecureMode(SecureMode.FORCE);

        executionDetails.setSecureModeReturnUrl(this.buildSecureModeReturnUrl(command));

        /*
         * This feature is for sandbox testing and will not be available in
         * production. In production, the only change will be that
         * Applied3DSVersion will give the value “V1” before we activate your
         * flows to 3DS2 and the value “V2_1” after activation.
         *
         * https://docs.mangopay.com/guide/3ds2-testing-in-sandbox
         */
        executionDetails.setRequested3DSVersion("V2_1");

        final PayIn result = new PayIn();
        result.setAuthorId(ctx.getCreditedUserId());
        result.setCreditedUserId(ctx.getCreditedUserId());
        result.setCreditedWalletId(ctx.getCreditedWalletId());
        result.setDebitedFunds(new Money(
            CurrencyIso.EUR, ctx.getDebitedFunds().multiply(BigDecimal.valueOf(100L)).intValue()
        ));
        result.setExecutionDetails(executionDetails);
        result.setExecutionType(PayInExecutionType.DIRECT);
        result.setFees(new Money(CurrencyIso.EUR, 0));
        result.setPaymentDetails(paymentDetails);
        result.setTag(command.getKey().toString());
        result.setPaymentType(PayInPaymentType.CARD);

        return result;
    }

    private PayIn createBankWirePayIn(CustomerEntity customer, BankwirePayInExecutionContext ctx) {
        final var command          = ctx.getCommand();
        final var mangoPayUserId   = customer.getPaymentProviderUser();
        final var mangoPayWalletId = customer.getPaymentProviderWallet();

        final PayInPaymentDetailsBankWire paymentDetails = new PayInPaymentDetailsBankWire();
        paymentDetails.setDeclaredDebitedFunds(new Money(
            CurrencyIso.EUR, ctx.getDebitedFunds().multiply(BigDecimal.valueOf(100L)).intValue()
        ));
        paymentDetails.setDeclaredFees(new Money(CurrencyIso.EUR, 0));

        final PayInExecutionDetailsDirect executionDetails = new PayInExecutionDetailsDirect();

        final PayIn result = new PayIn();
        result.setAuthorId(mangoPayUserId);
        result.setCreditedUserId(mangoPayUserId);
        result.setCreditedWalletId(mangoPayWalletId);
        result.setExecutionDetails(executionDetails);
        result.setExecutionType(PayInExecutionType.DIRECT);
        result.setPaymentDetails(paymentDetails);
        result.setTag(command.getKey().toString());
        result.setPaymentType(PayInPaymentType.BANK_WIRE);

        return result;
    }

    private void deactivateBankAccount(String userId, BankAccount bankAccount) throws PaymentException {
        Assert.notNull(bankAccount, "Expected a non-null bank account");

        // Check if this is a retry
        if (!bankAccount.isActive()) {
            return;
        }

        try {
            bankAccount.setActive(false);

            this.api.getUserApi().updateBankAccount(userId, bankAccount, bankAccount.getId());
        } catch (final ResponseException ex) {
            logger.error("MANGOPAY operation has failed", ex);

            throw new PaymentException("[MANGOPAY] Error: " + ex.getApiMessage(), ex);
        } catch (final PaymentException ex) {
            throw ex;
        } catch (final Exception ex) {
            logger.error("MANGOPAY bank account update has failed", ex);

            throw new PaymentException("[MANGOPAY] Bank account update has failed", ex);
        }
    }

    private AccountEntity getAccount(UUID key) throws PaymentException {
        final AccountEntity account = this.accountRepository.findOneByKey(key).orElse(null);

        if (account == null) {
            throw new PaymentException(String.format("[MANGOPAY] OpertusMundi user [%s] was not found", key));
        }

        return account;
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

    private BankAccount createBankAccount(CustomerBankAccountEmbeddable a) {
        return this.createBankAccount(a, null);
    }

    private BankAccount createBankAccount(CustomerBankAccountEmbeddable a, String id) {
        final BankAccount bankAccount = new BankAccount();

        bankAccount.setActive(true);
        bankAccount.setDetails(this.createBankAccountDetails(a));
        bankAccount.setId(id);
        bankAccount.setOwnerAddress(this.createAddress(a.getOwnerAddress()));
        bankAccount.setOwnerName(a.getOwnerName());
        bankAccount.setType(BankAccountType.IBAN);

        return bankAccount;
    }


    private BankAccountDetails createBankAccountDetails(CustomerBankAccountEmbeddable a) {
        final BankAccountDetailsIBAN d = new BankAccountDetailsIBAN();

        d.setBic(a.getBic());
        d.setIban(a.getIban());

        return d;
    }

    private CustomerDraftEntity resolveRegistration(AccountEntity account, EnumCustomerType type, UUID key) {
        CustomerDraftEntity registration;
        switch (type) {
            case CONSUMER :
                registration = account.getProfile().getConsumerRegistration();
                break;
            case PROVIDER :
                registration = account.getProfile().getProviderRegistration();
                break;
            default :
                registration = null;
        }

        if (registration != null && registration.getKey().equals(key)) {
            if (registration.getStatus() != EnumCustomerRegistrationStatus.SUBMITTED) {
                throw new PaymentException(String.format(
                    "[MANGOPAY] Invalid registration state [%s] for key [%s]. Expected [SUBMITTED]",
                    registration.getStatus(), key
                ));
            }
            return registration;
        }

        throw new PaymentException(String.format("[MANGOPAY] No active registration found for key [%s]", key));
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

    private String getWalletDescription(AccountEntity a) {
        return String.format("Default wallet");
    }

    private PaymentException wrapException(String operation, Exception ex) {
        return super.wrapException(operation, ex, null, logger);
    }

    protected PaymentException wrapException(String operation, Exception ex, Object command) {
        return super.wrapException(operation, ex, command, logger);
    }

    private void ensureCustomer(CustomerEntity customer, String id) throws PaymentException {
        if (customer == null) {
            throw new PaymentException(
                PaymentMessageCode.PROVIDER_USER_NOT_FOUND,
                String.format("[MANGOPAY] Customer was not found for MANGOPAY user with resource id [%s]", id)
            );
        }
    }

    private void ensureCustomer(CustomerEntity customer, UUID key) throws PaymentException {
        if (customer == null) {
            throw new PaymentException(
                PaymentMessageCode.PLATFORM_CUSTOMER_NOT_FOUND,
                String.format("[MANGOPAY] Customer registration was not found for account with key [%s]", key)
            );
        }
    }

    private void ensureOrderForPayIn(OrderEntity order, UUID key) throws PaymentException {
        if (order == null) {
            throw new PaymentException(
                PaymentMessageCode.ORDER_NOT_FOUND,
                String.format("[MANGOPAY] Order [%s] was not", key)
            );
        }
        final List<EnumOrderStatus> validStatus = Arrays.asList(
            EnumOrderStatus.CREATED,
            EnumOrderStatus.PROVIDER_ACCEPTED,
            EnumOrderStatus.CONTRACT_IS_SIGNED,
            EnumOrderStatus.CHARGED
        );

        if (!validStatus.contains(order.getStatus())) {
            throw new PaymentException(PaymentMessageCode.ORDER_INVALID_STATUS, String.format(
                "[MANGOPAY] Invalid order status [order=%s, status=%s, expected=%s]",
                key, order.getStatus(), validStatus
            ));
        }

        Assert.isTrue(order.getItems().size() == 1, "Expected only a single item in the order");
    }

    private PayInEntity ensurePayIn(String providerPayInId) {
        final PayInEntity payInEntity = this.payInRepository.findOneByPayInId(providerPayInId).orElse(null);

        if(payInEntity == null) {
            throw new PaymentException(
                PaymentMessageCode.RESOURCE_NOT_FOUND,
                String.format("[OpertusMundi] PayIn [%s] was not found", providerPayInId)
            );
        }

        return payInEntity;
    }

    private PayOutEntity ensurePayOut(String providerPayOutId) {
        final PayOutEntity payOutEntity = this.payOutRepository.findOneByPayOutId(providerPayOutId).orElse(null);

        if(payOutEntity == null) {
            throw new PaymentException(
                PaymentMessageCode.RESOURCE_NOT_FOUND,
                String.format("[OpertusMundi] PayOut [%s] was not found", providerPayOutId)
            );
        }

        return payOutEntity;
    }

    private PayInItemEntity ensurePayInItemTransfer(String transferId) {
        final PayInItemEntity payInItemEntity = this.payInRepository.findOnePayInItemByTransferId(transferId).orElse(null);

        if (payInItemEntity == null) {
            throw new PaymentException(
                PaymentMessageCode.RESOURCE_NOT_FOUND,
                String.format("[OpertusMundi] Transfer [%s] was not found", transferId)
            );
        }

        return payInItemEntity;
    }

    private ZonedDateTime timestampToDate(Long timestamp) {
        // MANGOPAY returns dates as integer numbers that represent the
        // number of seconds since the Unix Epoch
        if (timestamp != null) {
            return ZonedDateTime.ofInstant(Instant.ofEpochSecond(timestamp), ZoneOffset.UTC);
        }
        return null;
    }

    private void configureRecurringPayment(OrderEntity order, CardDirectPayInExecutionContext ctx) {
        // Recurring payments are supported only for subscriptions
        if (order.getItems().get(0).getType() != EnumOrderItemType.SUBSCRIPTION) {
            return;
        }
        // The quotation parameters must support recurring payments
        final QuotationParametersDto parameters = order.getItems().get(0).getPricingModel().getUserParameters();
        if (!SubscriptionQuotationParameters.class.isAssignableFrom(parameters.getClass())) {
            return;
        }
        final EnumRecurringPaymentFrequency frequency = ((SubscriptionQuotationParameters) parameters).getFrequency();

        ctx.setRecurring(true);
        ctx.setRecurringPaymentFrequency(frequency);
    }

}
