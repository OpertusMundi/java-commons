package eu.opertusmundi.common.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.mangopay.core.Address;
import com.mangopay.core.Money;
import com.mangopay.core.Pagination;
import com.mangopay.core.ResponseException;
import com.mangopay.core.enumerations.BankAccountType;
import com.mangopay.core.enumerations.CardType;
import com.mangopay.core.enumerations.CountryIso;
import com.mangopay.core.enumerations.CurrencyIso;
import com.mangopay.core.enumerations.FundsType;
import com.mangopay.core.enumerations.KycLevel;
import com.mangopay.core.enumerations.LegalPersonType;
import com.mangopay.core.enumerations.NaturalUserCapacity;
import com.mangopay.core.enumerations.PayInExecutionType;
import com.mangopay.core.enumerations.PayInPaymentType;
import com.mangopay.core.enumerations.PersonType;
import com.mangopay.core.enumerations.SecureMode;
import com.mangopay.core.interfaces.BankAccountDetails;
import com.mangopay.entities.BankAccount;
import com.mangopay.entities.Card;
import com.mangopay.entities.CardRegistration;
import com.mangopay.entities.Client;
import com.mangopay.entities.IdempotencyResponse;
import com.mangopay.entities.PayIn;
import com.mangopay.entities.PayOut;
import com.mangopay.entities.Refund;
import com.mangopay.entities.Transfer;
import com.mangopay.entities.User;
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
import eu.opertusmundi.common.domain.CustomerBankAccountEmbeddable;
import eu.opertusmundi.common.domain.CustomerDraftEntity;
import eu.opertusmundi.common.domain.CustomerDraftIndividualEntity;
import eu.opertusmundi.common.domain.CustomerDraftProfessionalEntity;
import eu.opertusmundi.common.domain.CustomerEntity;
import eu.opertusmundi.common.domain.CustomerProfessionalEntity;
import eu.opertusmundi.common.domain.CustomerRrepresentativeEmbeddable;
import eu.opertusmundi.common.domain.OrderEntity;
import eu.opertusmundi.common.domain.PayInEntity;
import eu.opertusmundi.common.domain.PayInItemEntity;
import eu.opertusmundi.common.domain.PayInOrderItemEntity;
import eu.opertusmundi.common.domain.PayInSubscriptionBillingItemEntity;
import eu.opertusmundi.common.domain.PayOutEntity;
import eu.opertusmundi.common.model.EnumSortingOrder;
import eu.opertusmundi.common.model.PageResultDto;
import eu.opertusmundi.common.model.account.AccountDto;
import eu.opertusmundi.common.model.account.BankAccountDto;
import eu.opertusmundi.common.model.account.EnumCustomerRegistrationStatus;
import eu.opertusmundi.common.model.account.EnumCustomerType;
import eu.opertusmundi.common.model.account.EnumLegalPersonType;
import eu.opertusmundi.common.model.account.EnumMangopayUserType;
import eu.opertusmundi.common.model.catalogue.client.CatalogueItemDetailsDto;
import eu.opertusmundi.common.model.location.Location;
import eu.opertusmundi.common.model.order.CartDto;
import eu.opertusmundi.common.model.order.CartItemDto;
import eu.opertusmundi.common.model.order.EnumOrderStatus;
import eu.opertusmundi.common.model.order.HelpdeskOrderDto;
import eu.opertusmundi.common.model.order.OrderCommand;
import eu.opertusmundi.common.model.order.OrderDto;
import eu.opertusmundi.common.model.payment.BankwirePayInCommand;
import eu.opertusmundi.common.model.payment.CardDirectPayInCommand;
import eu.opertusmundi.common.model.payment.CardDto;
import eu.opertusmundi.common.model.payment.CardRegistrationCommandDto;
import eu.opertusmundi.common.model.payment.CardRegistrationDto;
import eu.opertusmundi.common.model.payment.ClientDto;
import eu.opertusmundi.common.model.payment.EnumPayInItemSortField;
import eu.opertusmundi.common.model.payment.EnumPayInSortField;
import eu.opertusmundi.common.model.payment.EnumPayOutSortField;
import eu.opertusmundi.common.model.payment.EnumPaymentItemType;
import eu.opertusmundi.common.model.payment.EnumTransactionStatus;
import eu.opertusmundi.common.model.payment.FreePayInCommand;
import eu.opertusmundi.common.model.payment.PayInDto;
import eu.opertusmundi.common.model.payment.PayInItemDto;
import eu.opertusmundi.common.model.payment.PayInStatusUpdateCommand;
import eu.opertusmundi.common.model.payment.PayOutCommandDto;
import eu.opertusmundi.common.model.payment.PayOutDto;
import eu.opertusmundi.common.model.payment.PayOutStatusUpdateCommand;
import eu.opertusmundi.common.model.payment.PaymentException;
import eu.opertusmundi.common.model.payment.PaymentMessageCode;
import eu.opertusmundi.common.model.payment.TransferDto;
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
import eu.opertusmundi.common.repository.AccountRepository;
import eu.opertusmundi.common.repository.OrderRepository;
import eu.opertusmundi.common.repository.PayInItemHistoryRepository;
import eu.opertusmundi.common.repository.PayInRepository;
import eu.opertusmundi.common.repository.PayOutRepository;

@Service
@Transactional
public class MangoPayPaymentService extends BaseMangoPayService implements PaymentService {

    private static final Logger logger = LoggerFactory.getLogger(MangoPayPaymentService.class);

    // TODO: Set from configuration
    private final BigDecimal feePercent = new BigDecimal(5);

    /**
     * This is the URL where users are automatically redirected after 3D secure
     * validation (if activated)
     *
     * See: https://docs.mangopay.com/endpoints/v2.01/payins#e278_create-a-card-direct-payin
     */
    @Value("${opertusmundi.payments.mangopay.secure-mode-return-url:}")
    private String secureModeReturnUrl;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PayInRepository payInRepository;

    @Autowired
    private PayOutRepository payOutRepository;

    @Autowired
    private PayInItemHistoryRepository payInItemHistoryRepository;

    @Autowired
    private CatalogueService catalogueService;

    @Autowired
    private QuotationService quotationService;

    @Autowired
    private OrderFulfillmentService orderFulfillmentService;

    @Autowired
    private PayOutService payOutService;

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
                        user = this.createUserLegal(account, (CustomerDraftProfessionalEntity) registration);
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
                    user = this.createUserLegal(account, (CustomerDraftProfessionalEntity) registration,user.getId());
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
                    String.format("[MANGOPAY] Cannot create bank account for user [%s] of type [%s]", registration.getType(), command.getUserKey())
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
                this.deactivateAccount(mangoPayUserId, currentBankAccount);
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

            this.ensureCostumer(customer, command.getUserKey());

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
    public List<CardDto> getRegisteredCards(UserPaginationCommand command) {
        try {
            final AccountEntity  account  = this.getAccount(command.getUserKey());
            final CustomerEntity customer = account.getProfile().getConsumer();

            this.ensureCostumer(customer, command.getUserKey());

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
    public CardDto getRegisteredCard(UserCardCommand command) {
        try {
            final AccountEntity  account  = this.getAccount(command.getUserKey());
            final CustomerEntity customer = account.getProfile().getConsumer();

            this.ensureCostumer(customer, command.getUserKey());

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

            this.ensureCostumer(customer, command.getUserKey());

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

            this.ensureCostumer(customer, command.getUserKey());

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

            this.ensureCostumer(customer, command.getUserKey());

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
            final CatalogueItemDetailsDto asset = this.catalogueService.findOne(null, cartItem.getAssetId(), null,  false);
            if (asset == null) {
                throw new PaymentException(PaymentMessageCode.ASSET_NOT_FOUND, "Asset not found");
            }
            final boolean vettingRequired = BooleanUtils.isTrue(asset.getVettingRequired());

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
                asset, cartItemPricingModel.getModel().getKey(), cartItemPricingModel.getParameters(), false
            );

            // Create command
            final OrderCommand orderCommand = OrderCommand.builder()
                .asset(asset)
                .cartId(cart.getId())
                .deliveryMethod(asset.getDeliveryMethod())
                .location(location)
                .quotation(quotation)
                .userId(cart.getAccountId())
                .vettingRequired(vettingRequired)
                .build();

            final OrderDto order = this.orderRepository.create(orderCommand);

            return order;
        } catch (final Exception ex) {
            throw this.wrapException("Create Order", ex, cart == null ? "" : cart.getKey());
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
                this.ensureCostumer(consumer, userKey);

                final String    walletId = consumer.getPaymentProviderWallet();
                final Wallet    wallet   = this.api.getWalletApi().get(walletId);
                final WalletDto result   = WalletDto.from(wallet);

                consumer.setWalletFunds(result.getAmount());
                consumer.setWalletFundsUpdatedOn(ZonedDateTime.now());
            }
            if (provider != null) {
                this.ensureCostumer(provider, userKey);

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

            this.ensureCostumer(customer, userKey);

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
        final Page<PayInEntity> page        = this.payInRepository.findAllConsumerPayIns(userKey, status, pageRequest);

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
        final Page<PayInItemEntity> page        = this.payInRepository.findAllProviderPayInItems(userKey, status, pageRequest);

        final long                       count   = page.getTotalElements();
        final List<ProviderPayInItemDto> records = page.getContent().stream()
            .map(p -> p.toProviderDto(false))
            .collect(Collectors.toList());

        return PageResultDto.of(pageIndex, pageSize, records, count);
    }
    @Override
    public PayInDto createPayInBankwireForOrder(BankwirePayInCommand command) throws PaymentException {
        try {
            final AccountEntity  account        = this.getAccount(command.getUserKey());
            final CustomerEntity customer       = account.getProfile().getConsumer();
            final OrderEntity    order          = this.orderRepository.findOrderEntityByKey(command.getOrderKey()).orElse(null);

            // Check customer
            this.ensureCostumer(customer, command.getUserKey());

            // Check order
            this.ensureOrderForPayIn(order, command.getOrderKey());

            final String idempotencyKey = order.getKey().toString();

            // Check payment
            final PayInEntity payIn = order.getPayin();
            if (payIn != null) {
                return payIn.toConsumerDto(true);
            }

            // Update command with order properties

            // MANGOPAY expects not decimal values e.g. 100,50 is formatted as a
            // integer 10050
            command.setDebitedFunds(order.getTotalPrice().multiply(BigDecimal.valueOf(100L)).intValue());
            command.setReferenceNumber(this.createReferenceNumber());

            // Funds must be greater than 0
            if (command.getDebitedFunds() <= 0) {
                throw new PaymentException(PaymentMessageCode.ZERO_AMOUNT, "[TOPIO] PayIn amount must be greater than 0");
            }

            // Check if this is a retry operation
            PayIn payInResponse = this.<PayIn>getResponse(idempotencyKey);

            // Create a new PayIn if needed
            if (payInResponse == null) {
                final PayIn payInRequest = this.createBankWirePayIn(customer, command);

                payInResponse = this.api.getPayInApi().create(idempotencyKey, payInRequest);
            } else {
                // Override PayIn key with existing one from the payment
                // provider
                command.setPayInKey(UUID.fromString(payInResponse.getTag()));
            }

            // Update command with payment information
            final PayInPaymentDetailsBankWire paymentDetails = (PayInPaymentDetailsBankWire) payInResponse.getPaymentDetails();

            command.setPayIn(payInResponse.getId());
            command.setWireReference(paymentDetails.getWireReference());
            command.setBankAccount(BankAccountDto.from(paymentDetails.getBankAccount()));
            // MANGOPAY returns dates as integer numbers that represent the
            // number of seconds since the Unix Epoch
            command.setCreatedOn(ZonedDateTime.ofInstant(Instant.ofEpochSecond(payInResponse.getCreationDate()), ZoneOffset.UTC));

            // Create database record
            final PayInDto result = this.payInRepository.createBankwirePayInForOrder(command);
            // Link PayIn record to order
            this.orderRepository.setPayIn(command.getOrderKey(), result.getPayIn(), account.getKey());

            return result;
        } catch (final Exception ex) {
            throw this.wrapException("Create PayIn BankWire", ex, command);
        }
    }

    @Override
    public PayInDto createPayInFreeForOrder(FreePayInCommand command) throws PaymentException {
        try {
            final AccountEntity  account        = this.getAccount(command.getUserKey());
            final CustomerEntity customer       = account.getProfile().getConsumer();
            final OrderEntity    order          = this.orderRepository.findOrderEntityByKey(command.getOrderKey()).orElse(null);

            // Check customer
            this.ensureCostumer(customer, command.getUserKey());

            // Check order
            this.ensureOrderForPayIn(order, command.getOrderKey());

            // Funds must be greater than 0
            if (order.getTotalPrice().longValue() > 0) {
                throw new PaymentException(PaymentMessageCode.NON_ZERO_AMOUNT, "[TOPIO] PayIn amount must be equal to 0");
            }

            // Check payment
            final PayInEntity payIn = order.getPayin();
            if (payIn != null) {
                return payIn.toConsumerDto(true);
            }

            command.setReferenceNumber(this.createReferenceNumber());


            // Create database record
            final PayInDto result = this.payInRepository.createFreePayInForOrder(command);
            // Link PayIn record to order
            this.orderRepository.setPayIn(command.getOrderKey(), result.getPayIn(), account.getKey());

            return result;
        } catch (final Exception ex) {
            throw this.wrapException("Create PayIn Free", ex, command);
        }
    }

    @Override
    public PayInDto createPayInCardDirectForOrder(CardDirectPayInCommand command) throws PaymentException {
        try {
            final AccountEntity  account        = this.getAccount(command.getUserKey());
            final CustomerEntity customer       = account.getProfile().getConsumer();
            final OrderEntity    order          = this.orderRepository.findOrderEntityByKey(command.getOrderKey()).orElse(null);

            // Check customer
            this.ensureCostumer(customer, command.getUserKey());

            // Check order
            this.ensureOrderForPayIn(order, command.getOrderKey());

            final String idempotencyKey = order.getKey().toString();

            // Get card
            final CardDto card = this.getRegisteredCard(UserCardCommand.of(command.getUserKey(), command.getCardId()));

            // Update command with order properties

            // MANGOPAY expects not decimal values e.g. 100,50 is formatted as a
            // integer 10050
            command.setDebitedFunds(order.getTotalPrice().multiply(BigDecimal.valueOf(100L)).intValue());
            command.setReferenceNumber(this.createReferenceNumber());
            command.setStatementDescriptor(this.createStatementDescriptor(command));

            // Funds must be greater than 0
            if (command.getDebitedFunds() <= 0) {
                throw new PaymentException(PaymentMessageCode.ZERO_AMOUNT, "[TOPIO] PayIn amount must be greater than 0");
            }

            // Check if this is a retry operation
            PayIn payInResponse = this.<PayIn>getResponse(idempotencyKey);

            // Create a new PayIn if needed
            if (payInResponse == null) {
                final PayIn payInRequest = this.createCardDirectPayIn(customer, command);

                payInResponse = this.api.getPayInApi().create(idempotencyKey, payInRequest);
            } else {
                // Override PayIn key with existing one from the payment
                // provider
                command.setPayInKey(UUID.fromString(payInResponse.getTag()));
            }

            // Update command with payment information
            final PayInExecutionDetailsDirect executionDetails = (PayInExecutionDetailsDirect) payInResponse.getExecutionDetails();

            command.setPayIn(payInResponse.getId());
            command.setStatus(EnumTransactionStatus.from(payInResponse.getStatus()));
            command.setResultCode(payInResponse.getResultCode());
            command.setResultMessage(payInResponse.getResultMessage());
            // MANGOPAY returns dates as integer numbers that represent the
            // number of seconds since the Unix Epoch
            command.setCreatedOn(ZonedDateTime.ofInstant(Instant.ofEpochSecond(payInResponse.getCreationDate()), ZoneOffset.UTC));
            // For Card Direct PayIns, if no 3-D Secure validation is required,
            // the transaction may be executed immediately
            if (payInResponse.getExecutionDate() != null) {
                command.setExecutedOn(ZonedDateTime.ofInstant(Instant.ofEpochSecond(payInResponse.getExecutionDate()), ZoneOffset.UTC));
            }

            // Create database record
            final PayInEntity          payIn  = order.getPayin();
            ConsumerCardDirectPayInDto result = null;
            if (payIn != null) {
                result = (ConsumerCardDirectPayInDto) payIn.toConsumerDto(true);
            } else {
                result = (ConsumerCardDirectPayInDto) this.payInRepository.createCardDirectPayInForOrder(command);

                // Link PayIn record to order
                this.orderRepository.setPayIn(command.getOrderKey(), result.getPayIn(), account.getKey());

                // Update order status if we have a valid response i.e.
                // 3D-Secure validation was skipped
                if (result.getStatus() != EnumTransactionStatus.CREATED) {
                    this.orderRepository.setStatus(
                        order.getKey(),
                        result.getStatus().toOrderStatus(order.getDeliveryMethod())
                    );
                }
            }

            // Add client-only information (card alias is never saved in our
            // database)
            result.setAlias(card.getAlias());
            result.setSecureModeRedirectURL(executionDetails.getSecureModeRedirectUrl());

            return result;
        } catch (final Exception ex) {
            throw this.wrapException("Create PayIn Card Direct", ex, command);
        }
    }

    @Override
    public void sendPayInStatusUpdateMessage(UUID payInKey, String payInId) throws PaymentException {
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
                            idempotencyKey, payIn, (PayInOrderItemEntity) item, debitCustomer
                        );
                        break;
                    case SUBSCRIPTION_BILLING :
                        transfer = this.createTransferForSubscriptionBilling(
                            idempotencyKey, payIn, (PayInSubscriptionBillingItemEntity) item, debitCustomer
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

                    this.payInRepository.saveAndFlush(payIn);

                    // If transfer is successful, update item history record
                    if (transfer.getStatus() == EnumTransactionStatus.SUCCEEDED) {
                        this.payInItemHistoryRepository.updateTransfer(item.getId(), transfer);
                    }

                    transfer.setKey(item.getTransferKey());
                    transfers.add(transfer);

                    // Update provider wallet
                    if (transfer.getStatus() == EnumTransactionStatus.SUCCEEDED) {
                        this.updateCustomerWalletFunds(item.getProvider().getKey(), EnumCustomerType.PROVIDER);
                    }
                }
            }

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
        String idempotencyKey, PayInEntity payIn, PayInOrderItemEntity item, CustomerEntity debitCustomer
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
                payIn.getKey(), item.getIndex()
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

    private TransferDto createTransferForSubscriptionBilling(
        String idempotencyKey, PayInEntity payIn, PayInSubscriptionBillingItemEntity item, CustomerEntity debitCustomer
    ) throws Exception {
        Assert.isTrue(item.getSubscriptionBilling() != null, "Expected a non-null subscription billing record");

        // Get credit customer
        final AccountEntity  creditAccount  = item.getSubscriptionBilling().getSubscription().getProvider();
        final CustomerEntity creditCustomer = creditAccount.getProfile().getProvider();
        final BigDecimal     amount         = item.getSubscriptionBilling().getTotalPrice().multiply(BigDecimal.valueOf(100L));
        final BigDecimal     fees           = item.getSubscriptionBilling().getTotalPrice()
            .multiply(this.feePercent)
            .divide(BigDecimal.valueOf(100L))
            .setScale(2, RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(100L));

        if (creditCustomer == null) {
            throw new PaymentException(PaymentMessageCode.SERVER_ERROR, String.format(
                "[MANGOPAY] Credit customer for PayIn item was not found [key=%s, index=%d]",
                payIn.getKey(), item.getIndex()
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
            final CustomerProfessionalEntity customer = account.getProfile().getProvider();

            if (customer == null) {
                throw new PaymentException(
                    PaymentMessageCode.SERVER_ERROR,
                    String.format("[MANGOPAY] Customer was not found for account [key=%s]", command.getProviderKey())
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

            // Funds must exist
            if (customer.getWalletFunds().compareTo(command.getDebitedFunds()) < 0) {
                throw new PaymentException(PaymentMessageCode.VALIDATION_ERROR, "Not enough funds. Check wallet balance");
            }
            // Fees are applied in Transfers.
            command.setFees(BigDecimal.ZERO);

            // Generate unique reference number and create PayOut locally
            final String bankWireRef = this.createReferenceNumber();

            command.setBankWireRef(bankWireRef);
            final PayOutDto payout = this.payOutRepository.createPayOut(command);

            // Start PayOut workflow instance
            this.payOutService.start(command.getAdminUserKey(), payout.getKey());

            // Refresh provider's wallet from the payment provider
            this.updateCustomerWalletFunds(command.getProviderKey(), EnumCustomerType.PROVIDER);

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
        final String bankAccountId = customer.getBankAccount().getId();

        final PayOutPaymentDetailsBankWire details = new PayOutPaymentDetailsBankWire();
        details.setBankAccountId(bankAccountId);
        details.setBankWireRef(payOut.getBankwireRef());

        final PayOut result = new PayOut();
        result.setAuthorId(userId);
        result.setDebitedFunds(new Money(CurrencyIso.EUR, payOut.getDebitedFunds().multiply(BigDecimal.valueOf(100L)).intValue()));
        result.setDebitedWalletId(walletId);
        result.setFees(new Money(CurrencyIso.EUR, payOut.getPlatformFees().multiply(BigDecimal.valueOf(100L)).intValue()));
        result.setMeanOfPaymentDetails(details);
        result.setTag(payOut.getKey().toString());

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

    private PayIn createBankWirePayIn(CustomerEntity customer, BankwirePayInCommand command) {
        final String mangoPayUserId   = customer.getPaymentProviderUser();
        final String mangoPayWalletId = customer.getPaymentProviderWallet();

        final PayInPaymentDetailsBankWire paymentDetails = new PayInPaymentDetailsBankWire();
        paymentDetails.setDeclaredDebitedFunds(new Money(CurrencyIso.EUR, command.getDebitedFunds()));
        paymentDetails.setDeclaredFees(new Money(CurrencyIso.EUR, 0));

        final PayInExecutionDetailsDirect executionDetails = new PayInExecutionDetailsDirect();

        final PayIn result = new PayIn();
        result.setAuthorId(mangoPayUserId);
        result.setCreditedUserId(mangoPayUserId);
        result.setCreditedWalletId(mangoPayWalletId);
        result.setExecutionDetails(executionDetails);
        result.setExecutionType(PayInExecutionType.DIRECT);
        result.setPaymentDetails(paymentDetails);
        result.setTag(command.getPayInKey().toString());
        result.setPaymentType(PayInPaymentType.BANK_WIRE);

        return result;
    }

    private PayIn createCardDirectPayIn(CustomerEntity customer, CardDirectPayInCommand command) {
        final String mangoPayUserId   = customer.getPaymentProviderUser();
        final String mangoPayWalletId = customer.getPaymentProviderWallet();

        final PayInPaymentDetailsCard paymentDetails = new PayInPaymentDetailsCard();
        paymentDetails.setCardType(CardType.CB_VISA_MASTERCARD);
        paymentDetails.setCardId(command.getCardId());
        paymentDetails.setStatementDescriptor(command.getStatementDescriptor());

        final PayInExecutionDetailsDirect executionDetails = new PayInExecutionDetailsDirect();
        executionDetails.setCardId(command.getCardId());
        executionDetails.setSecureMode(SecureMode.DEFAULT);
        executionDetails.setSecureModeReturnUrl(this.buildSecureModeReturnUrl(command));

        final PayIn result = new PayIn();
        result.setAuthorId(mangoPayUserId);
        result.setCreditedUserId(mangoPayUserId);
        result.setCreditedWalletId(mangoPayWalletId);
        result.setDebitedFunds(new Money(CurrencyIso.EUR, command.getDebitedFunds()));
        result.setExecutionDetails(executionDetails);
        result.setExecutionType(PayInExecutionType.DIRECT);
        result.setFees(new Money(CurrencyIso.EUR, 0));
        result.setPaymentDetails(paymentDetails);
        result.setTag(command.getPayInKey().toString());
        result.setPaymentType(PayInPaymentType.CARD);

        return result;
    }

    private String buildSecureModeReturnUrl(CardDirectPayInCommand command) {
        String baseUrl = this.secureModeReturnUrl;
        if (!baseUrl.endsWith("/")) {
            baseUrl += "/";
        }

        return baseUrl + "webhooks/payins/" + command.getPayInKey().toString();
    }

    private void deactivateAccount(String userId, BankAccount bankAccount) throws PaymentException {
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

        final CustomerRrepresentativeEmbeddable lr = r.getRepresentative();

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

        throw new PaymentException(String.format("[MANGOPAY] Legal person type [%s] is not supported", t));
    }

    private String getWalletDescription(AccountEntity a) {
        return String.format("Default wallet");
    }

    /**
     * Get existing response for an idempotency key
     *
     * See: https://docs.mangopay.com/guide/idempotency-support
     *
     * @param <T>
     * @param idempotencyKey
     * @return
     * @throws Exception
     */
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

    private PaymentException wrapException(String operation, Exception ex) {
        return super.wrapException(operation, ex, null, logger);
    }

    protected PaymentException wrapException(String operation, Exception ex, Object command) {
        return super.wrapException(operation, ex, command, logger);
    }

    // TODO: Move to new service
    // TODO: Review reference number generation algorithm

    private String createReferenceNumber() {
        final String digits     = "123456789ABCDEFGHIJKLMNPQRSTUVWXYZ";
        final int    targetBase = digits.length();
        long         value      = ZonedDateTime.now().toInstant().toEpochMilli();
        String       result     = "";

        do {
            result = digits.charAt((int) (value % targetBase)) + result;
            value  = value / targetBase;
        } while (value > 0);

        return result;
    }

    // TODO: Move to new service (?)
    // TODO: Review statement descriptor generation algorithm

    private String createStatementDescriptor(CardDirectPayInCommand command) {
        // Use reference number as the statement descriptor
        final String result = command.getReferenceNumber();

        Assert.isTrue(result.length() < 11, "Statement descriptor can be up to 10 characters long");

        return result;
    }

    private void ensureCostumer(CustomerEntity consumer, UUID key) throws PaymentException {
        if (consumer == null) {
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
            EnumOrderStatus.CHARGED
        );

        if (!validStatus.contains(order.getStatus())) {
            throw new PaymentException(PaymentMessageCode.ORDER_INVALID_STATUS, String.format(
                "[MANGOPAY] Invalid order status [order=%s, status=%s, expected=%s]",
                key, order.getStatus(), validStatus
            ));
        }
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

}
