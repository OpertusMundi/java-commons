package eu.opertusmundi.common.service.mangopay;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import com.mangopay.core.FilterEvents;
import com.mangopay.core.Money;
import com.mangopay.core.Pagination;
import com.mangopay.core.ResponseException;
import com.mangopay.core.Sorting;
import com.mangopay.core.enumerations.CardType;
import com.mangopay.core.enumerations.CurrencyIso;
import com.mangopay.core.enumerations.EventType;
import com.mangopay.core.enumerations.PayInExecutionType;
import com.mangopay.core.enumerations.PayInPaymentType;
import com.mangopay.core.enumerations.SecureMode;
import com.mangopay.core.enumerations.SortDirection;
import com.mangopay.entities.Card;
import com.mangopay.entities.CardRegistration;
import com.mangopay.entities.Client;
import com.mangopay.entities.Event;
import com.mangopay.entities.PayIn;
import com.mangopay.entities.subentities.PayInExecutionDetailsDirect;
import com.mangopay.entities.subentities.PayInPaymentDetailsBankWire;
import com.mangopay.entities.subentities.PayInPaymentDetailsCard;

import eu.opertusmundi.common.domain.AccountEntity;
import eu.opertusmundi.common.domain.CardDirectPayInEntity;
import eu.opertusmundi.common.domain.CustomerEntity;
import eu.opertusmundi.common.domain.OrderEntity;
import eu.opertusmundi.common.domain.PayInEntity;
import eu.opertusmundi.common.domain.PayInItemEntity;
import eu.opertusmundi.common.domain.PayInRecurringRegistrationEntity;
import eu.opertusmundi.common.model.EnumSortingOrder;
import eu.opertusmundi.common.model.EnumView;
import eu.opertusmundi.common.model.PageResultDto;
import eu.opertusmundi.common.model.account.BankAccountDto;
import eu.opertusmundi.common.model.account.EnumCustomerType;
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
import eu.opertusmundi.common.model.payment.CardDirectPayInCommand;
import eu.opertusmundi.common.model.payment.CardDirectPayInExecutionContext;
import eu.opertusmundi.common.model.payment.CardDto;
import eu.opertusmundi.common.model.payment.CardRegistrationCommandDto;
import eu.opertusmundi.common.model.payment.CardRegistrationDto;
import eu.opertusmundi.common.model.payment.CheckoutServiceBillingCommandDto;
import eu.opertusmundi.common.model.payment.ClientDto;
import eu.opertusmundi.common.model.payment.EnumPayInItemSortField;
import eu.opertusmundi.common.model.payment.EnumPayInSortField;
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
import eu.opertusmundi.common.model.payment.PaymentException;
import eu.opertusmundi.common.model.payment.PaymentMessageCode;
import eu.opertusmundi.common.model.payment.RecurringRegistrationCreateCommand;
import eu.opertusmundi.common.model.payment.RecurringRegistrationDto;
import eu.opertusmundi.common.model.payment.ServiceBillingDto;
import eu.opertusmundi.common.model.payment.UserCardCommand;
import eu.opertusmundi.common.model.payment.UserCommand;
import eu.opertusmundi.common.model.payment.UserPaginationCommand;
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
import eu.opertusmundi.common.repository.OrderRepository;
import eu.opertusmundi.common.repository.PayInItemHistoryRepository;
import eu.opertusmundi.common.repository.PayInRepository;
import eu.opertusmundi.common.repository.PayOutRepository;
import eu.opertusmundi.common.repository.ServiceBillingRepository;
import eu.opertusmundi.common.service.CatalogueService;
import eu.opertusmundi.common.service.OrderFulfillmentService;
import eu.opertusmundi.common.service.QuotationService;
import eu.opertusmundi.common.util.MangopayUtils;
import eu.opertusmundi.common.util.StreamUtils;

@Service
@Transactional
public class MangoPayPayInService extends BaseMangoPayService implements PayInService {

    private static final Logger logger = LoggerFactory.getLogger(MangoPayPayInService.class);

    private final OrderRepository            orderRepository;
    private final PayInRepository            payInRepository;
    private final PayInItemHistoryRepository payInItemHistoryRepository;
    private final CatalogueService           catalogueService;
    private final QuotationService           quotationService;
    private final OrderFulfillmentService    orderFulfillmentService;
    private final RecurringPayInService    recurringPaymentService;
    private final ServiceBillingRepository   serviceBillingRepository;
    private final WalletService              walletService;

    @Autowired
    public MangoPayPayInService(
        AccountRepository             accountRepository,
        OrderRepository               orderRepository,
        PayInRepository               payInRepository,
        PayOutRepository              payOutRepository,
        PayInItemHistoryRepository    payInItemHistoryRepository,
        CatalogueService              catalogueService,
        QuotationService              quotationService,
        OrderFulfillmentService       orderFulfillmentService,
        RecurringPayInService       recurringPaymentService,
        ServiceBillingRepository      serviceBillingRepository,
        WalletService                 walletService
    ) {
        super(accountRepository);

        this.orderRepository            = orderRepository;
        this.payInRepository            = payInRepository;
        this.payInItemHistoryRepository = payInItemHistoryRepository;
        this.catalogueService           = catalogueService;
        this.quotationService           = quotationService;
        this.orderFulfillmentService    = orderFulfillmentService;
        this.recurringPaymentService    = recurringPaymentService;
        this.serviceBillingRepository   = serviceBillingRepository;
        this.walletService              = walletService;
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

        return item == null ? null : item.toProviderDto(true, true);
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
            .map(p -> p.toProviderDto(false, true))
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
            this.orderRepository.setPayIn(command.getKey(), result.getTransactionId(), account.getKey());

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
            this.orderRepository.setPayIn(command.getKey(), result.getTransactionId(), account.getKey());

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
                this.orderRepository.setPayIn(command.getKey(), result.getTransactionId(), command.getUserKey());

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
                this.walletService.refreshUserWallets(payInEntity.getConsumer().getKey(), EnumCustomerType.CONSUMER);
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

    private PaymentException wrapException(String operation, Exception ex) {
        return super.wrapException(operation, ex, null, logger);
    }

    private PaymentException wrapException(String operation, Exception ex, Object command) {
        return super.wrapException(operation, ex, command, logger);
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
        final PayInEntity payInEntity = this.payInRepository.findOneByTransactionIdForUpdate(providerPayInId).orElse(null);

        if(payInEntity == null) {
            throw new PaymentException(
                PaymentMessageCode.RESOURCE_NOT_FOUND,
                String.format("[OpertusMundi] PayIn [%s] was not found", providerPayInId)
            );
        }

        return payInEntity;
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
