package eu.opertusmundi.common.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
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
import eu.opertusmundi.common.model.EnumCustomerRegistrationStatus;
import eu.opertusmundi.common.model.catalogue.client.CatalogueItemDetailsDto;
import eu.opertusmundi.common.model.dto.AccountDto;
import eu.opertusmundi.common.model.dto.BankAccountDto;
import eu.opertusmundi.common.model.dto.EnumLegalPersonType;
import eu.opertusmundi.common.model.dto.EnumMangopayUserType;
import eu.opertusmundi.common.model.order.CartDto;
import eu.opertusmundi.common.model.order.CartItemDto;
import eu.opertusmundi.common.model.order.OrderCommandDto;
import eu.opertusmundi.common.model.order.OrderDto;
import eu.opertusmundi.common.model.payment.BankwirePayInCommand;
import eu.opertusmundi.common.model.payment.CardDirectPayInCommand;
import eu.opertusmundi.common.model.payment.CardDirectPayInDto;
import eu.opertusmundi.common.model.payment.CardDto;
import eu.opertusmundi.common.model.payment.CardRegistrationCommandDto;
import eu.opertusmundi.common.model.payment.CardRegistrationDto;
import eu.opertusmundi.common.model.payment.ClientDto;
import eu.opertusmundi.common.model.payment.EnumTransactionStatus;
import eu.opertusmundi.common.model.payment.PayInDto;
import eu.opertusmundi.common.model.payment.PayInStatusUpdateCommandDto;
import eu.opertusmundi.common.model.payment.PayOutCommandDto;
import eu.opertusmundi.common.model.payment.PayOutDto;
import eu.opertusmundi.common.model.payment.PaymentException;
import eu.opertusmundi.common.model.payment.PaymentMessageCode;
import eu.opertusmundi.common.model.payment.TransferCommandDto;
import eu.opertusmundi.common.model.payment.TransferDto;
import eu.opertusmundi.common.model.payment.UserCardCommandDto;
import eu.opertusmundi.common.model.payment.UserCommandDto;
import eu.opertusmundi.common.model.payment.UserPaginationCommandDto;
import eu.opertusmundi.common.model.payment.UserRegistrationCommandDto;
import eu.opertusmundi.common.model.pricing.BasePricingModelCommandDto;
import eu.opertusmundi.common.model.pricing.EffectivePricingModelDto;
import eu.opertusmundi.common.repository.AccountRepository;
import eu.opertusmundi.common.repository.OrderRepository;
import eu.opertusmundi.common.repository.PayInRepository;

@Service
@Transactional
public class MangoPayPaymentService extends BaseMangoPayService implements PaymentService {

    private static final Logger logger = LoggerFactory.getLogger(MangoPayPaymentService.class);
  
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
    private CatalogueService catalogueService;
    
    @Autowired
    private QuotationService quotationService;

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
    public AccountDto createUser(UserRegistrationCommandDto command) {
        try {
            User user;

            // Get account
            final AccountEntity account = this.getAccount(command.getUserKey());

            // Resolve registration
            final CustomerDraftEntity registration   = this.resolveRegistration(account, command.getRegistrationKey());
            final EnumMangopayUserType    type           = registration.getType();
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
    public AccountDto updateUser(UserRegistrationCommandDto command) {
        try {
            User user;

            // Get account
            final AccountEntity account = this.getAccount(command.getUserKey());

            // Resolve registration
            final CustomerDraftEntity registration = this.resolveRegistration(account, command.getRegistrationKey());
            final EnumMangopayUserType    type         = registration.getType();

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
    public AccountDto createWallet(UserRegistrationCommandDto command) {
        try {
            Wallet wallet;

            // Get account
            final AccountEntity account = this.getAccount(command.getUserKey());

            // Resolve registration
            final CustomerDraftEntity registration   = this.resolveRegistration(account, command.getRegistrationKey());
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
    public AccountDto createBankAccount(UserRegistrationCommandDto command) {
        try {
            BankAccount bankAccount;

            // Get account
            final AccountEntity account = this.getAccount(command.getUserKey());

            // Resolve registration
            final CustomerDraftEntity registration   = this.resolveRegistration(account, command.getRegistrationKey());
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
    public AccountDto updateBankAccount(UserRegistrationCommandDto command) {
        try {
            final AccountEntity                   account               = this.getAccount(command.getUserKey());
            final CustomerProfessionalEntity      customer              = account.getProfile().getProvider();
            final CustomerDraftProfessionalEntity registration          = this.getProviderRegistration(account, command.getRegistrationKey());

            if(registration == null) {
                throw new PaymentException(String.format(
                    "[MANGOPAY] Provider registration was not found for account with key [%s]",
                    command.getUserKey()
                ));
            }

            if (registration.getStatus() != EnumCustomerRegistrationStatus.SUBMITTED) {
                throw new PaymentException(String.format(
                    "[MANGOPAY] Invalid registration state [%s] for key [%s]. Expected [SUBMITTED]",
                    registration.getStatus(), command.getRegistrationKey()
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
            final AccountDto result = this.createBankAccount(command);

            return result;
        } catch (final Exception ex) {
            throw this.wrapException("Update Bank Account", ex, command);
        }
    }

    @Override
    public List<BankAccountDto> getBankAccounts(UserPaginationCommandDto command) throws PaymentException {
        try {
            final AccountEntity              account  = this.getAccount(command.getUserKey());
            final CustomerProfessionalEntity customer = account.getProfile().getProvider();

            this.ensureConsumer(customer, command.getUserKey());
            
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
    public List<CardDto> getRegisteredCards(UserPaginationCommandDto command) {
        try {
            final AccountEntity  account  = this.getAccount(command.getUserKey());
            final CustomerEntity customer = account.getProfile().getConsumer();

            this.ensureConsumer(customer, command.getUserKey());

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
    public CardDto getRegisteredCard(UserCardCommandDto command) {
        try {
            final AccountEntity  account  = this.getAccount(command.getUserKey());
            final CustomerEntity customer = account.getProfile().getConsumer();

            this.ensureConsumer(customer, command.getUserKey());

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
    public void deactivateCard(UserCardCommandDto command) {
        try {
            final AccountEntity  account  = this.getAccount(command.getUserKey());
            final CustomerEntity customer = account.getProfile().getConsumer();

            this.ensureConsumer(customer, command.getUserKey());

            final Card card = this.api.getCardApi().get(command.getCardId());

            Assert.isTrue(card.getUserId().equals(customer.getPaymentProviderUser()), "Card user id must be equal to the customer id");
            
            this.api.getCardApi().disable(card);
        } catch (final Exception ex) {
            throw this.wrapException("Deactivate Card", ex, command);
        }
    }

    // TODO: Consider using idempotency key for this method ...
    
    @Override
    public CardRegistrationDto createCardRegistration(UserCommandDto command) {
        try {
            final AccountEntity  account  = this.getAccount(command.getUserKey());
            final CustomerEntity customer = account.getProfile().getConsumer();

            this.ensureConsumer(customer, command.getUserKey());

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

            this.ensureConsumer(customer, command.getUserKey());

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
    public OrderDto createOrderFromCart(CartDto cart) throws PaymentException {
        try {
   
            if (cart == null || cart.getItems().size() == 0) {
                throw new PaymentException(PaymentMessageCode.CART_IS_EMPTY, "Cart is empty");
            }
            if (cart.getItems().size() != 1) {
                throw new PaymentException(PaymentMessageCode.CART_MAX_SIZE, "Cart must contain only one item");
            }
            
            final CartItemDto cartItem = cart.getItems().get(0);
            
            // Cart item must be a catalogue published item
            final CatalogueItemDetailsDto asset = this.catalogueService.findOne(cartItem.getAssetId(), null,  false);
            if (asset == null) {
                throw new PaymentException(PaymentMessageCode.ASSET_NOT_FOUND, "Asset not found");
            }
            
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
                asset, cartItemPricingModel.getModel().getKey(), cartItemPricingModel.getParameters()
            );
    
            // Create command
            final OrderCommandDto orderCommand = OrderCommandDto.builder()
                .asset(asset)
                .cartId(cart.getId())
                .deliveryMethod(asset.getDeliveryMethod())
                .quotation(quotation)
                .referenceNumber(this.createReferenceNumber())
                .userId(cart.getAccountId())
                .build();
            
            final OrderDto order = this.orderRepository.create(orderCommand);

            return order;
        } catch (final Exception ex) {
            throw this.wrapException("Create Order", ex, cart == null ? "" : cart.getKey());
        }
    }
       
    @Override
    public PayInDto getPayIn(Integer userId, UUID payInKey) {
        final PayInEntity payIn = this.payInRepository.findOneByAccountIdAndKey(userId, payInKey).orElse(null);

        return payIn == null ? null : payIn.toDto();
    }

    @Override
    public PayInDto createPayInBankwireForOrder(BankwirePayInCommand command) throws PaymentException {
        try {
            final AccountEntity  account        = this.getAccount(command.getUserKey());
            final CustomerEntity customer       = account.getProfile().getConsumer();
            final OrderEntity    order          = this.orderRepository.findOneByKey(command.getOrderKey()).orElse(null);
           
            // Check customer
            this.ensureConsumer(customer, command.getUserKey());

            // Check order
            this.ensureOrder(order, command.getOrderKey());
            
            final String idempotencyKey = order.getKey().toString();

            // Check payment
            final PayInEntity payIn = order.getPayin();
            if (payIn != null) {
                return payIn.toDto();
            }
            
            // Update command with order properties
            
            // MANGOPAY expects not decimal values e.g. 100,50 is formatted as a
            // integer 10050
            command.setDebitedFunds(order.getTotalPrice().multiply(BigDecimal.valueOf(100L)).intValue());
            command.setReferenceNumber(this.createReferenceNumber());
            
            // Check if this is a retry operation
            PayIn payInResponse = this.<PayIn>getResponse(idempotencyKey);

            if (payInResponse == null) {
                // Create a new PayIn if needed
                final PayIn payInRequest = this.createBankWirePayIn(customer, command);

                payInResponse = this.api.getPayInApi().create(idempotencyKey, payInRequest);
            } else {
                // Override PayIn key
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

            return result;
        } catch (final Exception ex) {
            throw this.wrapException("Create PayIn BankWire", ex, command);
        }
    }
    
    @Override
    public PayInDto createPayInCardDirectForOrder(CardDirectPayInCommand command) throws PaymentException {
        try {
            final AccountEntity  account        = this.getAccount(command.getUserKey());
            final CustomerEntity customer       = account.getProfile().getConsumer();
            final OrderEntity    order          = this.orderRepository.findOneByKey(command.getOrderKey()).orElse(null);
           
            // Check customer
            this.ensureConsumer(customer, command.getUserKey());

            // Check order
            this.ensureOrder(order, command.getOrderKey());
            
            final String idempotencyKey = order.getKey().toString();

            // Get card
            final CardDto card = this.getRegisteredCard(UserCardCommandDto.of(command.getUserKey(), command.getCardId()));
            
            // Check payment
            final PayInEntity payIn = order.getPayin();
            if (payIn != null) {
                final PayInDto result = payIn.toDto();
                ((CardDirectPayInDto) result).setAlias(card.getAlias());
                return result;
            }
            
            // Update command with order properties
            
            // MANGOPAY expects not decimal values e.g. 100,50 is formatted as a
            // integer 10050
            command.setDebitedFunds(order.getTotalPrice().multiply(BigDecimal.valueOf(100L)).intValue());
            command.setReferenceNumber(this.createReferenceNumber());
            command.setStatementDescriptor(this.createStatementDescriptor(command));
            
            // Check if this is a retry operation
            PayIn payInResponse = this.<PayIn>getResponse(idempotencyKey);

            // Create a new PayIn if needed
            if (payInResponse == null) {
                final PayIn payInRequest = this.createCardDirectPayIn(customer, command);

                payInResponse = this.api.getPayInApi().create(idempotencyKey, payInRequest);
            } else {
                // Override PayIn key
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
            final CardDirectPayInDto result = (CardDirectPayInDto) this.payInRepository.createCardDirectPayInForOrder(command);
            
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
    public PayInDto updatePayIn(String providerPayInId) throws PaymentException {
        try {
            final PayInEntity payInEntity = this.ensurePayIn(providerPayInId);

            final PayIn payInObject = this.api.getPayInApi().get(providerPayInId);
            
            // Handle redundant updates
            if (payInEntity.getStatus() == EnumTransactionStatus.from(payInObject.getStatus())) {
                return payInEntity.toDto();
            }

            final PayInStatusUpdateCommandDto command = PayInStatusUpdateCommandDto.builder()
                .providerPayInId(providerPayInId)
                .executedOn(payInObject.getExecutionDate())
                .status(EnumTransactionStatus.from(payInObject.getStatus()))
                .resultCode(payInObject.getResultCode())
                .resultMessage(payInObject.getResultMessage())
                .build();

            final PayInDto result = this.payInRepository.updatePayInStatus(command);

            return result;
        } catch (final Exception ex) {
            throw this.wrapException("Update PayIn", ex, providerPayInId);
        }
    }
    
    @Override
    public PayInDto updatePayIn(UUID payInKey, String providerPayInId) throws PaymentException {
        return this.updatePayIn(providerPayInId);
    }
    
    @Override
    public TransferDto createTransfer(TransferCommandDto command) throws PaymentException {
        try {
            final AccountEntity  debitAccount   = this.getAccount(command.getDebitedUserKey());
            final CustomerEntity debitCustomer  = debitAccount.getProfile().getConsumer();
            final AccountEntity  creditAccount  = this.getAccount(command.getCreditedUserKey());
            final CustomerEntity creditCustomer = creditAccount.getProfile().getConsumer();
            final String         idempotencyKey = command.getPaymentKey().toString();

            if (debitCustomer == null) {
                throw new PaymentException(
                    PaymentMessageCode.SERVER_ERROR,
                    String.format("[MANGOPAY] Debit customer was not found for account with key [%s]", command.getDebitedUserKey())
                );
            }
            if (creditCustomer == null) {
                throw new PaymentException(
                    PaymentMessageCode.SERVER_ERROR,
                    String.format("[MANGOPAY] Credit customer was not found for account with key [%s]", command.getCreditedUserKey())
                );
            }
            
            // Check if this is a retry operation
            Transfer transferResponse = this.<Transfer>getResponse(idempotencyKey);

            // Create a new transfer if needed
            if (transferResponse == null) {
                final Transfer transferRequest = this.createTransfer(debitCustomer, creditCustomer, command);

                transferResponse = this.api.getTransferApi().create(idempotencyKey, transferRequest);
            }

            // TODO: Create result
            return null;
        } catch (final Exception ex) {
            throw this.wrapException("Create Transfer", ex, command);
        }
    }
    
    @Override
    public PayOutDto createPayOut(PayOutCommandDto command) throws PaymentException {
        try {
            final AccountEntity              account        = this.getAccount(command.getUserKey());
            final CustomerProfessionalEntity customer       = account.getProfile().getProvider();
            final String                     idempotencyKey = command.getPayOutKey().toString();

            if (customer == null) {
                throw new PaymentException(
                    PaymentMessageCode.SERVER_ERROR,
                    String.format("[MANGOPAY] Customer was not found for account with key [%s]", command.getUserKey())
                );
            }
            
            // Check if this is a retry operation
            PayOut payoutResponse = this.<PayOut>getResponse(idempotencyKey);

            // Create a new PayPout if needed
            if (payoutResponse == null) {
                final PayOut payOutRequest = this.createPayOut(customer, command);

                payoutResponse = this.api.getPayOutApi().create(idempotencyKey, payOutRequest);
            }

            // TODO: Create result
            return null;
        } catch (final Exception ex) {
            throw this.wrapException("Create PayOut", ex, command);
        }
    }

    private PayOut createPayOut(CustomerProfessionalEntity customer, PayOutCommandDto command) {
        final String userId        = customer.getPaymentProviderUser();
        final String walletId      = customer.getPaymentProviderWallet();
        final String bankAccountId = customer.getBankAccount().getId();

        final PayOutPaymentDetailsBankWire details = new PayOutPaymentDetailsBankWire();
        details.setBankAccountId(bankAccountId);
        details.setBankWireRef(command.getBankWireRef());
        
        final PayOut result = new PayOut();
        result.setAuthorId(userId);
        result.setDebitedFunds(new Money(CurrencyIso.EUR, command.getDebitedFunds()));
        result.setDebitedWalletId(walletId);
        result.setFees(new Money(CurrencyIso.EUR, command.getFees()));
        result.setMeanOfPaymentDetails(details);
        result.setTag(command.getPayOutKey().toString());
      
        return result;
    }
    
    private Transfer createTransfer(CustomerEntity debitCustomer, CustomerEntity creditCustomer, TransferCommandDto command) {
        final String debitUserId    = debitCustomer.getPaymentProviderUser();
        final String debitWalletId  = debitCustomer.getPaymentProviderWallet();
        final String creditUserId   = creditCustomer.getPaymentProviderUser();
        final String creditWalletId = creditCustomer.getPaymentProviderWallet();

        final Transfer result = new Transfer();
        result.setAuthorId(debitUserId);
        result.setCreditedUserId(creditUserId);
        result.setDebitedFunds(new Money(CurrencyIso.EUR, command.getDebitedFunds()));
        result.setDebitedWalletId(debitWalletId);
        result.setFees(new Money(CurrencyIso.EUR, command.getFees()));
        result.setCreditedUserId(creditUserId);
        result.setCreditedWalletId(creditWalletId);
        result.setTag(command.getPaymentKey().toString());

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
            bankAccount = this.api.getUserApi().updateBankAccount(userId, bankAccount, bankAccount.getId());
        } catch (final ResponseException ex) {
            logger.error("[MANGOPAY] API operation has failed", ex);

            throw new PaymentException("[MANGOPAY] Error: " + ex.getApiMessage(), ex);
        } catch (final PaymentException ex) {
            throw ex;
        } catch (final Exception ex) {
            logger.error("[MANGOPAY] Bank account update has failed", ex);

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

    private CustomerDraftProfessionalEntity getProviderRegistration(AccountEntity account, UUID key) {
        return (CustomerDraftProfessionalEntity) this.resolveRegistration(account, key);
    }

    private CustomerDraftEntity resolveRegistration(AccountEntity account, UUID key) {
        // Lookup for consumers
        CustomerDraftEntity registration = account.getProfile().getConsumerRegistration();

        if (registration != null && registration.getKey().equals(key)) {
            if (registration.getStatus() != EnumCustomerRegistrationStatus.SUBMITTED) {
                throw new PaymentException(String.format(
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
        return this.wrapException(operation, ex, null);
    }
    
    /**
     * Wraps an exception with {@link PaymentException}
     * 
     * @param operation
     * @param ex
     * @return
     */
    private PaymentException wrapException(String operation, Exception ex, Object command) {
        final String commandText = command == null ? "-" : command.toString();
        
        // Ignore service exceptions
        if (ex instanceof PaymentException) {
            return (PaymentException) ex;
        }

        // MANGOPAY exceptions
        if (ex instanceof ResponseException) {
            final String message = String.format(
                "[MANGOPAY] %s : %s [%s]", operation, ((ResponseException) ex).getApiMessage(),commandText
            );

            logger.error(message, ex);

            return new PaymentException(PaymentMessageCode.API_ERROR, message, ex);
        }

        // Global exception handler
        final String message = String.format("[MANGOPAY] %s [%s]", operation, commandText);

        logger.error(message, ex);

        return new PaymentException(PaymentMessageCode.SERVER_ERROR, message, ex);
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
    
    private void ensureConsumer(CustomerEntity consumer, UUID key) throws PaymentException {
        if (consumer == null) {
            throw new PaymentException(
                PaymentMessageCode.PLATFORM_CUSTOMER_NOT_FOUND,
                String.format("[MANGOPAY] Consumer registration was not found for account with key [%s]", key)
            );
        }
    }

    private void ensureOrder(OrderEntity order, UUID key) throws PaymentException {
        if (order == null) {
            throw new PaymentException(
                PaymentMessageCode.ORDER_NOT_FOUND,
                String.format("[MANGOPAY] Order [%s] was not", key)
            );
        }
    }
    
    private PayInEntity ensurePayIn(String providerPayInId) {
        final PayInEntity payInEntity = this.payInRepository.findOneByPayInId(providerPayInId).orElse(null);
        
        if(payInEntity == null) {
            throw new PaymentException(
                PaymentMessageCode.SERVER_ERROR,
                String.format("[OpertusMundi] PayIn [%s] was not found", providerPayInId)
            );  
        }
        
        return payInEntity;
    }
    
}
