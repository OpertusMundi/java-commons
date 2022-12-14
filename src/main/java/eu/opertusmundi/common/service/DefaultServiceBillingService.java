package eu.opertusmundi.common.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.rest.dto.VariableValueDto;
import org.camunda.bpm.engine.rest.dto.runtime.ProcessInstanceDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.icu.text.NumberFormat;

import eu.opertusmundi.common.domain.AccountEntity;
import eu.opertusmundi.common.domain.AccountSubscriptionEntity;
import eu.opertusmundi.common.domain.AccountSubscriptionSkuEntity;
import eu.opertusmundi.common.domain.OrderItemEntity;
import eu.opertusmundi.common.domain.PayInEntity;
import eu.opertusmundi.common.domain.PayInServiceBillingItemEntity;
import eu.opertusmundi.common.domain.ServiceBillingBatchEntity;
import eu.opertusmundi.common.domain.ServiceBillingEntity;
import eu.opertusmundi.common.domain.UserServiceEntity;
import eu.opertusmundi.common.feign.client.MessageServiceFeignClient;
import eu.opertusmundi.common.model.BasicMessageCode;
import eu.opertusmundi.common.model.EnumService;
import eu.opertusmundi.common.model.EnumSetting;
import eu.opertusmundi.common.model.EnumSortingOrder;
import eu.opertusmundi.common.model.EnumView;
import eu.opertusmundi.common.model.PageResultDto;
import eu.opertusmundi.common.model.ServiceException;
import eu.opertusmundi.common.model.SettingUpdateCommandDto;
import eu.opertusmundi.common.model.SettingUpdateDto;
import eu.opertusmundi.common.model.account.EnumPayoffStatus;
import eu.opertusmundi.common.model.account.EnumServiceBillingRecordSortField;
import eu.opertusmundi.common.model.catalogue.client.CatalogueItemDetailsDto;
import eu.opertusmundi.common.model.message.EnumNotificationType;
import eu.opertusmundi.common.model.message.server.ServerNotificationCommandDto;
import eu.opertusmundi.common.model.payment.BillingSubscriptionDates;
import eu.opertusmundi.common.model.payment.EnumBillableServiceType;
import eu.opertusmundi.common.model.payment.EnumPaymentItemType;
import eu.opertusmundi.common.model.payment.EnumServiceBillingBatchStatus;
import eu.opertusmundi.common.model.payment.EnumTransactionStatus;
import eu.opertusmundi.common.model.payment.PaymentException;
import eu.opertusmundi.common.model.payment.PaymentMessageCode;
import eu.opertusmundi.common.model.payment.ServiceBillingDto;
import eu.opertusmundi.common.model.payment.ServiceUseStatsDto;
import eu.opertusmundi.common.model.payment.ServiceBillingBatchCommandDto;
import eu.opertusmundi.common.model.payment.ServiceBillingBatchDto;
import eu.opertusmundi.common.model.payment.helpdesk.HelpdeskPayInDto;
import eu.opertusmundi.common.model.payment.helpdesk.HelpdeskServiceBillingDto;
import eu.opertusmundi.common.model.pricing.EffectivePricingModelDto;
import eu.opertusmundi.common.model.pricing.PerCallPricingModelCommandDto;
import eu.opertusmundi.common.model.pricing.QuotationDto;
import eu.opertusmundi.common.model.pricing.QuotationException;
import eu.opertusmundi.common.model.workflow.EnumProcessInstanceVariable;
import eu.opertusmundi.common.model.workflow.EnumWorkflow;
import eu.opertusmundi.common.repository.AccountRepository;
import eu.opertusmundi.common.repository.AccountSubscriptionRepository;
import eu.opertusmundi.common.repository.PayInRepository;
import eu.opertusmundi.common.repository.ServiceBillingRepository;
import eu.opertusmundi.common.repository.SettingHistoryRepository;
import eu.opertusmundi.common.repository.SettingRepository;
import eu.opertusmundi.common.repository.ServiceBillingBatchRepository;
import eu.opertusmundi.common.repository.UserServiceRepository;
import eu.opertusmundi.common.service.messaging.NotificationMessageHelper;
import eu.opertusmundi.common.util.BpmEngineUtils;
import eu.opertusmundi.common.util.BpmInstanceVariablesBuilder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Service
public class DefaultServiceBillingService implements ServiceBillingService {

    private static final Logger logger = LoggerFactory.getLogger(DefaultServiceBillingService.class);

    private static final String IDEMPOTENT_KEY_PREFIX        = "SUB_BILLING";
    private static final String IDEMPOTENT_KEY_SUFFIX_CHARGE = "CHARGE";
    private static final String IDEMPOTENT_KEY_SUFFIX_PAYOFF = "PAYOFF";

    /**
     * Offset in days after the first day of the current month, at which a
     * quotation may be created
     */
    @Value("${opertusmundi.subscription-billing.quotation-min-offset:5}")
    private int quotationMinOffset;

    /**
     * When {@code true}, send a notification for every single subscription
     * billing record
     */
    @Value("${opertusmundi.subscription-billing.notification-per-subscription:true}")
    private boolean notificationPerSubscription;

    private final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final AccountRepository                         accountRepository;
    private final AccountSubscriptionRepository             accountSubscriptionRepository;
    private final BpmEngineUtils                            bpmEngine;
    private final CatalogueService                          catalogueService;
    private final ObjectMapper                              objectMapper;
    private final ObjectProvider<MessageServiceFeignClient> messageClient;
    private final NotificationMessageHelper                 notificationMessageBuilder;
    private final PayInRepository                           payInRepository;
    private final QuotationService                          quotationService;
    private final SettingRepository                         settingRepository;
    private final SettingHistoryRepository                  settingHistoryRepository;
    private final ServiceBillingBatchRepository             ServiceBillingBatchRepository;
    private final ServiceBillingRepository                  serviceBillingRepository;
    private final ServiceUseStatsService                    serviceUseStatsService;
    private final UserServiceRepository                     userServiceRepository;

    @Autowired
    public DefaultServiceBillingService(
        AccountRepository                           accountRepository,
        AccountSubscriptionRepository               accountSubscriptionRepository,
        BpmEngineUtils                              bpmEngine,
        CatalogueService                            catalogueService,
        ObjectMapper                                objectMapper,
        ObjectProvider<MessageServiceFeignClient>   messageClient,
        NotificationMessageHelper                   notificationMessageBuilder,
        PayInRepository                             payInRepository,
        QuotationService                            quotationService,
        SettingRepository                           settingRepository,
        SettingHistoryRepository                    settingHistoryRepository,
        ServiceBillingBatchRepository               ServiceBillingBatchRepository,
        ServiceBillingRepository                    serviceBillingRepository,
        ServiceUseStatsService                      serviceUseStatsService,
        UserServiceRepository                       userServiceRepository
    ) {
        this.accountRepository                  = accountRepository;
        this.accountSubscriptionRepository      = accountSubscriptionRepository;
        this.bpmEngine                          = bpmEngine;
        this.catalogueService                   = catalogueService;
        this.messageClient                      = messageClient;
        this.notificationMessageBuilder         = notificationMessageBuilder;
        this.objectMapper                       = objectMapper;
        this.payInRepository                    = payInRepository;
        this.quotationService                   = quotationService;
        this.settingRepository                  = settingRepository;
        this.settingHistoryRepository           = settingHistoryRepository;
        this.ServiceBillingBatchRepository      = ServiceBillingBatchRepository;
        this.serviceBillingRepository           = serviceBillingRepository;
        this.serviceUseStatsService             = serviceUseStatsService;
        this.userServiceRepository              = userServiceRepository;
    }

    @Override
    public PageResultDto<ServiceBillingDto> findAllServiceBillingRecords(
        EnumView view, EnumBillableServiceType type,
        UUID ownerKey, UUID providerKey, UUID serviceKey, Set<EnumPayoffStatus> status,
        int pageIndex, int pageSize,
        EnumServiceBillingRecordSortField orderBy, EnumSortingOrder order
    ) {
        final var direction   = order == EnumSortingOrder.DESC ? Direction.DESC : Direction.ASC;
        final var pageRequest = PageRequest.of(pageIndex, pageSize, Sort.by(direction, orderBy.getValue()));

        final var page = this.serviceBillingRepository.findAllObjects(
            view, type, ownerKey, providerKey, serviceKey, status, pageRequest, false
        );

        final var count   = page.getTotalElements();
        final var records = page.getContent();

        return PageResultDto.of(pageIndex, pageSize, records, count);
    }

    @Override
    public Optional<ServiceBillingDto> findOneServiceBillingRecord(EnumView view, UUID key) {
        return this.serviceBillingRepository.findOneSubscriptionObjectByKey(view, key, true);
    }

    @Override
    public Optional<ServiceBillingBatchDto> findOneBillingIntervalByKey(UUID key) {
        return this.ServiceBillingBatchRepository.findOneObjectByKey(key);
    }

    @Override
    @Transactional
    public ServiceBillingBatchDto start(ServiceBillingBatchCommandDto command) throws PaymentException {
        // Compute dates
        final BillingSubscriptionDates dates = computeInterval(command);

        final ServiceBillingBatchEntity batch = ServiceBillingBatchRepository.findOneOrCreate(command, dates);

        final var runningInstances = this.bpmEngine.findInstancesByProcessDefinitionKey(EnumWorkflow.SERVICE_BILLING.getKey());
        if (!runningInstances.isEmpty()) {
            throw new PaymentException(PaymentMessageCode.SUBSCRIPTION_BILLING_RUNNING, "A subscription billing task is already running");
        }

        ProcessInstanceDto instance = this.bpmEngine.findInstance(batch.getKey());

        if (instance == null) {
            // Set business key
            final String businessKey = batch.getKey().toString();

            // Set variables
            final Map<String, VariableValueDto> variables = BpmInstanceVariablesBuilder.builder()
                .variableAsString(EnumProcessInstanceVariable.START_USER_KEY.getValue(), batch.getCreatedBy().getKey().toString())
                .variableAsString("batchId", batch.getId().toString())
                .variableAsString("batchKey", businessKey.toString())
                .variableAsString("dateDue", dates.getDateDue().toString())
                .variableAsString("dateFrom", dates.getDateFrom().toString())
                .variableAsString("dateTo", dates.getDateTo().toString())
                .variableAsInteger("year", command.getYear())
                .variableAsInteger("month", command.getMonth())
                .build();

            instance = this.bpmEngine.startProcessDefinitionByKey(EnumWorkflow.SERVICE_BILLING, businessKey, variables);
        }

        batch.setProcessDefinition(instance.getDefinitionId());
        batch.setProcessInstance(instance.getId());

        this.ServiceBillingBatchRepository.saveAndFlush(batch);

        return batch.toDto();
    }

    @Override
    @Transactional
    public List<ServiceBillingDto> create(UUID userKey, int year, int month, boolean quotationOnly) throws PaymentException {
        try {
            final var dates  = computeInterval(year, month);
            final var result = new ArrayList<ServiceBillingDto>();

            final var account = this.accountRepository.findOneByKey(userKey).orElse(null);
            if (account == null) {
                throw new PaymentException(PaymentMessageCode.ACCOUNT_NOT_FOUND, String.format("Account was not found [userKey=%s]", userKey));
            }

            final var subscriptions = this.accountSubscriptionRepository.findAllEntitiesByConsumer(userKey, null);
            final var userServices  = this.userServiceRepository.findAllByParent(userKey);
            final var userStats     = this.serviceUseStatsService.getUseStats(userKey, year, month);

            final var ctx = OperationContext.builder()
                .account(account)
                .userKey(userKey)
                .month(month)
                .year(year)
                .subscriptions(subscriptions)
                .userServices(userServices)
                .useStats(userStats)
                .dates(dates)
                .build();

            for (final ServiceUseStatsDto stats : userStats) {
                if (stats.getCalls() == 0 && stats.getRows() == 0) {
                    continue;
                }

                final var record = switch (stats.getType()) {
                    case SUBSCRIPTION -> this.createForSubscription(ctx, stats);
                    case PRIVATE_OGC_SERVICE ->  this.createForUserService(ctx, stats);
                };

                result.add(record);
            }

            if(!notificationPerSubscription) {
                final BigDecimal total = result.stream().reduce(
                    BigDecimal.ZERO,
                    (subtotal, record) -> subtotal.add(record.getTotalPrice()),
                    BigDecimal::add
                );
                if (total.compareTo(BigDecimal.ZERO) > 0) {
                    this.sendChargeNotification(userKey, dates, total);
                }
            }

            return result;
        } catch(final PaymentException ex) {
            throw ex;
        } catch(final QuotationException ex) {
            throw new PaymentException(PaymentMessageCode.QUOTATION_ERROR, String.format(
                "Failed to create quotation [userKey=%s, year=%d, month=%d]", userKey, year, month
            ));
        }
    }

    private HelpdeskServiceBillingDto createForSubscription(OperationContext ctx, ServiceUseStatsDto stats) throws PaymentException {
        Assert.isTrue(stats.getType() == EnumBillableServiceType.SUBSCRIPTION, "Expected use stats to be of type SUBSCRIPTION");
        try {
            // Find subscription
            final var subscription = ctx.subscriptions.stream()
                .filter(s -> s.getKey().equals(stats.getServiceKey()))
                .findFirst()
                .orElse(null);
            if (subscription == null) {
                throw new PaymentException(PaymentMessageCode.SUBSCRIPTION_NOT_FOUND, String.format(
                    "Subscription was not found [subscriptionKey=%s]", stats.getServiceKey()
                ));
            }
            // Check if billing has already been computed
            var record = this.serviceBillingRepository
                .findOneBySubscriptionIdAndInterval(ctx.dates.getDateFrom(), ctx.dates.getDateTo(), subscription.getId())
                .map(b -> b.toHelpdeskDto(true))
                .orElse(null);

            if (record == null) {
                record = this.compute(ctx, subscription, stats);
            } else {
                this.validateBillingStats(record, stats);
            }

            if (notificationPerSubscription && record.getTotalPrice().compareTo(BigDecimal.ZERO) > 0) {
                this.sendSubscriptionChargeNotification(ctx.dates, subscription, record);
            }

            return record;
        } catch(final PaymentException ex) {
            throw ex;
        } catch(final QuotationException ex) {
            throw new PaymentException(PaymentMessageCode.QUOTATION_ERROR, String.format(
                "Failed to create quotation [userKey=%s, year=%d, month=%d]", ctx.userKey, ctx.year, ctx.month
            ));
        }
    }

    private HelpdeskServiceBillingDto createForUserService(OperationContext ctx, ServiceUseStatsDto stats) throws PaymentException {
        Assert.isTrue(stats.getType() == EnumBillableServiceType.PRIVATE_OGC_SERVICE, "Expected use stats to be of type PRIVATE_OGC_SERVICE");
        try {
            // Find user service
            final var service = ctx.userServices.stream()
                .filter(s -> s.getKey().equals(stats.getServiceKey()))
                .findFirst()
                .orElse(null);
            if (service == null) {
                throw new PaymentException(PaymentMessageCode.USER_SERVICE_NOT_FOUND, String.format(
                    "User services was not found [serviceKey=%s]", stats.getServiceKey()
                ));
            }
            // Check if billing has already been computed
            var record = this.serviceBillingRepository
                .findOneByUserServiceIdAndInterval(ctx.dates.getDateFrom(), ctx.dates.getDateTo(), service.getId())
                .map(b-> b.toHelpdeskDto(true))
                .orElse(null);

            if (record == null) {
                record = this.compute(ctx, service, stats);
            } else {
                this.validateBillingStats(record, stats);
            }

            if (notificationPerSubscription && record.getTotalPrice().compareTo(BigDecimal.ZERO) > 0) {
                this.sendUserServiceChargeNotification(ctx.dates, service, record);
            }

            return record;
        } catch(final PaymentException ex) {
            throw ex;
        } catch(final QuotationException ex) {
            throw new PaymentException(PaymentMessageCode.QUOTATION_ERROR, String.format(
                "Failed to create quotation [userKey=%s, year=%d, month=%d]", ctx.userKey, ctx.year, ctx.month
            ));
        }
    }

    private HelpdeskServiceBillingDto compute(OperationContext ctx, UserServiceEntity service, ServiceUseStatsDto subStats) {
        Assert.notNull(ctx, "Expected a non-null operation context");
        Assert.notNull(ctx.dates, "Expected a non-null date interval");
        Assert.notNull(service, "Expected a non-null service");
        Assert.isTrue(service.getKey().equals(subStats.getServiceKey()), "Service and use statistics object key mismatch");
        Assert.notNull(subStats, "Expected a non-null use statistics object");
        Assert.isTrue(subStats.getType() == EnumBillableServiceType.PRIVATE_OGC_SERVICE, "Invalid use stats type");

        // Get pricing model. Create quotation based on the pricing
        // model of the asset
        final PerCallPricingModelCommandDto pricingModel = this.getPrivateServicePricingModel();

        // Compute quotation (exclude prepaid rows/calls)
        final QuotationDto quotation = this.quotationService.createQuotation(pricingModel, subStats);

        // Add billing record
        final ZonedDateTime now = ZonedDateTime.now();

        ServiceBillingEntity subBilling = ServiceBillingEntity.builder()
            .billedAccount(ctx.account)
            .createdOn(now)
            .dueDate(ctx.dates.getDateDue())
            .fromDate(ctx.dates.getDateFrom())
            .pricingModel(pricingModel)
            .skuTotalCalls(0)
            .skuTotalRows(0)
            .userService(service)
            .toDate(ctx.dates.getDateTo())
            .totalCalls(subStats.getCalls())
            .totalRows(subStats.getRows())
            .totalPriceExcludingTax(quotation.getTotalPriceExcludingTax())
            .totalPrice(quotation.getTotalPrice())
            .totalTax(quotation.getTax())
            .stats(subStats)
            .status(quotation.getTotalPrice().compareTo(BigDecimal.ZERO) == 0
                ? EnumPayoffStatus.NO_CHARGE
                : EnumPayoffStatus.DUE)
            .updatedOn(now)
            .type(EnumBillableServiceType.PRIVATE_OGC_SERVICE)
            .build();

        if (!ctx.quotationOnly) {
            subBilling = serviceBillingRepository.saveAndFlush(subBilling);
        }

        return subBilling.toHelpdeskDto(true);
    }

    private HelpdeskServiceBillingDto compute(OperationContext ctx, AccountSubscriptionEntity subscription, ServiceUseStatsDto subStats) {
        Assert.notNull(ctx, "Expected a non-null operation context");
        Assert.notNull(ctx.dates, "Expected a non-null date interval");
        Assert.notNull(subscription, "Expected a non-null subscription");
        Assert.isTrue(subscription.getKey().equals(subStats.getServiceKey()), "Subscription and use statistics object key mismatch");
        Assert.notNull(subStats, "Expected a non-null use statistics object");
        Assert.isTrue(subStats.getType() == EnumBillableServiceType.SUBSCRIPTION, "Invalid use stats type");

        final ServiceUseStatsDto initialStats = subStats.shallowCopy();

        // Get pricing model. Create quotation based on the pricing
        // model of the asset
        final OrderItemEntity          orderItem    = subscription.getOrder().getItems().get(0);
        final EffectivePricingModelDto pricingModel = orderItem.getPricingModel();

        // Adjust billed rows/calls based on available SKUs
        final int totalRows     = subStats.getRows();
        final int totalCalls    = subStats.getCalls();
        int       totalSkuRows  = 0;
        int       totalSkuCalls = 0;

        subscription.getSkus().sort((s1, s2) -> s1.getId().compareTo(s2.getId()));

        for (final AccountSubscriptionSkuEntity sku : subscription.getSkus()) {
            final int availableRows  = sku.getAvailableRows();
            final int availableCalls = sku.getAvailableCalls();
            final int chargedRows    = subStats.getRows();
            final int chargedCalls   = subStats.getCalls();

            if (chargedRows > 0 && availableRows > 0) {
                final int prepaidRows = availableRows >= chargedRows ? chargedRows : availableRows;
                totalSkuRows += prepaidRows;
                sku.usePrepaidRows(prepaidRows);
                subStats.decreaseRows(prepaidRows);
            }
            if (chargedCalls > 0 && availableCalls > 0) {
                final int prepaidCalls = availableCalls >= chargedCalls ? chargedCalls : availableCalls;
                totalSkuCalls += prepaidCalls;
                sku.usePrepaidCalls(prepaidCalls);
                subStats.decreaseCalls(prepaidCalls);
            }
        }
        this.accountSubscriptionRepository.saveAndFlush(subscription);

        // Compute quotation (exclude prepaid rows/calls)
        final QuotationDto quotation = this.quotationService.createQuotation(pricingModel.getModel(), subStats);

        // Add billing record
        final ZonedDateTime now = ZonedDateTime.now();

        ServiceBillingEntity subBilling = ServiceBillingEntity.builder()
            .billedAccount(ctx.account)
            .createdOn(now)
            .dueDate(ctx.dates.getDateDue())
            .fromDate(ctx.dates.getDateFrom())
            .pricingModel(pricingModel.getModel())
            .skuTotalCalls(totalSkuCalls)
            .skuTotalRows(totalSkuRows)
            .subscription(subscription)
            .toDate(ctx.dates.getDateTo())
            .totalCalls(totalCalls)
            .totalRows(totalRows)
            .totalPriceExcludingTax(quotation.getTotalPriceExcludingTax())
            .totalPrice(quotation.getTotalPrice())
            .totalTax(quotation.getTax())
            .stats(initialStats)
            .status(quotation.getTotalPrice().compareTo(BigDecimal.ZERO) == 0
                ? EnumPayoffStatus.NO_CHARGE
                : EnumPayoffStatus.DUE)
            .type(EnumBillableServiceType.SUBSCRIPTION)
            .updatedOn(now)
            .build();

        if (!ctx.quotationOnly) {
            subBilling = serviceBillingRepository.saveAndFlush(subBilling);
        }

        return subBilling.toHelpdeskDto(true);
    }

    private void sendSubscriptionChargeNotification(
        BillingSubscriptionDates dates, AccountSubscriptionEntity subscription, HelpdeskServiceBillingDto record
    ) {
        final String                  idempotentKey = buildIdempotentKey(subscription.getKey(), dates, IDEMPOTENT_KEY_SUFFIX_CHARGE);
        final EnumNotificationType    type          = EnumNotificationType.SUBSCRIPTION_BILLING_SINGLE_CHARGE;
        final CatalogueItemDetailsDto asset         = this.catalogueService.findOne(null, subscription.getAssetId(), null, false);

        final Map<String, Object> variables = new HashMap<>();
        variables.put("intervalFrom", dates.getDateFrom().format(dateFormat));
        variables.put("intervalTo", dates.getDateTo().format(dateFormat));
        variables.put("dueDate", dates.getDateDue().format(dateFormat));
        variables.put("asset_id", subscription.getAssetId());
        variables.put("asset_title", asset.getTitle());
        variables.put("asset_version", asset.getVersion());
        variables.put("amount", this.formatCurrency(record.getTotalPrice()));

        final JsonNode data = this.notificationMessageBuilder.collectNotificationData(type, variables);

        final ServerNotificationCommandDto notification = ServerNotificationCommandDto.builder()
            .data(data)
            .eventType(type.toString())
            .idempotentKey(idempotentKey)
            .recipient(subscription.getConsumer().getKey())
            .text(this.notificationMessageBuilder.composeNotificationText(type, data))
            .build();

        messageClient.getObject().sendNotification(notification);
    }

    private void sendUserServiceChargeNotification(
        BillingSubscriptionDates dates, UserServiceEntity service, HelpdeskServiceBillingDto record
    ) {
        final String                  idempotentKey = buildIdempotentKey(service.getKey(), dates, IDEMPOTENT_KEY_SUFFIX_CHARGE);
        final EnumNotificationType    type          = EnumNotificationType.USER_SERVICE_BILLING_SINGLE_CHARGE;

        final Map<String, Object> variables = new HashMap<>();
        variables.put("intervalFrom", dates.getDateFrom().format(dateFormat));
        variables.put("intervalTo", dates.getDateTo().format(dateFormat));
        variables.put("dueDate", dates.getDateDue().format(dateFormat));
        variables.put("service_title", service.getTitle());
        variables.put("service_version", service.getVersion());
        variables.put("amount", this.formatCurrency(record.getTotalPrice()));

        final JsonNode data = this.notificationMessageBuilder.collectNotificationData(type, variables);

        final ServerNotificationCommandDto notification = ServerNotificationCommandDto.builder()
            .data(data)
            .eventType(type.toString())
            .idempotentKey(idempotentKey)
            .recipient(service.getAccount().getParentKey())
            .text(this.notificationMessageBuilder.composeNotificationText(type, data))
            .build();

        messageClient.getObject().sendNotification(notification);
    }

    private void sendChargeNotification(
        UUID userKey, BillingSubscriptionDates dates, BigDecimal total
    ) {
        final String               idempotentKey = buildIdempotentKey(userKey, dates, IDEMPOTENT_KEY_SUFFIX_CHARGE);
        final EnumNotificationType type          = EnumNotificationType.SERVICE_BILLING_TOTAL_CHARGE;
        final Map<String, Object>  variables     = new HashMap<>();

        variables.put("intervalFrom", dates.getDateFrom().format(dateFormat));
        variables.put("intervalTo", dates.getDateTo().format(dateFormat));
        variables.put("dueDate", dates.getDateDue().format(dateFormat));
        variables.put("amount", this.formatCurrency(total));

        final JsonNode data = this.notificationMessageBuilder.collectNotificationData(type, variables);

        final ServerNotificationCommandDto notification = ServerNotificationCommandDto.builder()
            .data(data)
            .eventType(type.toString())
            .idempotentKey(idempotentKey)
            .recipient(userKey)
            .text(this.notificationMessageBuilder.composeNotificationText(type, data))
            .build();

        messageClient.getObject().sendNotification(notification);
    }

    private String buildIdempotentKey(UUID key, BillingSubscriptionDates dates, String suffix) {
        return this.buildIdempotentKey(key, dates.getDateFrom().getYear(), dates.getDateFrom().getMonthValue(), suffix);
    }

    private String buildIdempotentKey(UUID key, int year, int month, String suffix) {
        return String.format("%s:%d:%d:%s:%s", IDEMPOTENT_KEY_PREFIX, year, month, key, suffix);
    }

    private String formatCurrency(BigDecimal value) {
        final var locale       = new Locale("el", "GR");
        final var numberFormat = NumberFormat.getCurrencyInstance(locale);

        return numberFormat.format(value);
    }

    @Override
    @Transactional
    public void complete(UUID key, int totalSubscriptions, BigDecimal totalPrice, BigDecimal totalPriceExcludingTax, BigDecimal totalTax) {
        final ServiceBillingBatchEntity e = this.ServiceBillingBatchRepository.findOneByKey(key).get();

        e.setStatus(EnumServiceBillingBatchStatus.SUCCEEDED);
        e.setTotalPrice(totalPrice);
        e.setTotalPriceExcludingTax(totalPriceExcludingTax);
        e.setTotalSubscriptions(totalSubscriptions);
        e.setTotalTax(totalTax);

        this.ServiceBillingBatchRepository.saveAndFlush(e);
    }

    @Override
    @Transactional
    public void fail(UUID key) {
        this.ServiceBillingBatchRepository.setStatus(key, EnumServiceBillingBatchStatus.FAILED);
    }

    private BillingSubscriptionDates computeInterval(ServiceBillingBatchCommandDto command) {
        return this.computeInterval(command.getYear(), command.getMonth());
    }

    private BillingSubscriptionDates computeInterval(int year, int month) throws PaymentException {
        this.validateInterval(year, month);

        // Compute dates
        final LocalDate dateFrom       = LocalDate.now()
            .withYear(year)
            .withMonth(month)
            .with(TemporalAdjusters.firstDayOfMonth());
        final LocalDate dateTo         = dateFrom
            .with(TemporalAdjusters.lastDayOfMonth());
        final LocalDate dateDue        = LocalDate.now()
            .with(TemporalAdjusters.firstDayOfMonth())
            .plusMonths(1);
        final LocalDate dateStatsReady = dateTo.plusMonths(1).with(TemporalAdjusters.firstDayOfMonth()).plusDays(this.quotationMinOffset);

        // Check quotation date
        if (dateStatsReady.isAfter(LocalDate.now())) {
            throw new PaymentException(PaymentMessageCode.USE_STATS_NOT_READY, String.format(
                "Use statistics will not be available for the selected interval until %s", dateStatsReady.format(dateFormat)
            ));
        }

        return BillingSubscriptionDates.builder()
            .dateDue(dateDue)
            .dateFrom(dateFrom)
            .dateTo(dateTo)
            .dateStatsReady(dateStatsReady)
            .build();
    }

    private void validateInterval(int year, int month) throws PaymentException {
        final ZonedDateTime now      = ZonedDateTime.now();
        final int           nowYear  = now.getYear();
        final int           nowMonth = now.getMonthValue();
        final int           maxYear  = nowMonth == 1 ? nowYear - 1 : nowYear;
        final int           maxMonth = nowMonth == 1 ? 12 : nowMonth - 1;
        // Minimum year (project first year)
        if (year < 2020 || year > maxYear) {
            throw new PaymentException(PaymentMessageCode.QUOTATION_INTERVAL_YEAR, "Quotation interval year is out of range");
        }
        if (month < 1 || (year == maxYear && month > maxMonth) || (year < maxYear && month > 12)) {
            throw new PaymentException(PaymentMessageCode.QUOTATION_INTERVAL_MONTH, "Quotation interval month is out of range");
        }
    }

    private void validateBillingStats(ServiceBillingDto record, ServiceUseStatsDto newStats) {
        final ServiceUseStatsDto prevStats = record.getStats();

        if (prevStats.getCalls() != newStats.getCalls() || prevStats.getRows() != newStats.getRows()) {
            logger.warn("Use statistics mismatch found [prevStats={}, newStats={}]", prevStats, newStats);
        }
    }

    /**
     * Initializes a workflow instance to process the referenced PayIn
     *
     * The operation may fail because of (a) a network error, (b) BPM engine
     * service error or (c) database command error. The operation is retried for
     * at most 3 times, with a maximum latency due to attempt delays of 9
     * seconds.
     */
    @Override
    @Retryable(include = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 2000, maxDelay = 3000))
    @Transactional
    public String startPayInWorkflow(UUID payInKey, String payInId, EnumTransactionStatus payInStatus) {
        final EnumWorkflow workflow = EnumWorkflow.CONSUMER_SERVICE_BILLING_PAYOFF;

        try {
            final HelpdeskPayInDto payIn = this.ensureServiceBillingPayin(payInKey).toHelpdeskDto(true);
            if (!StringUtils.isBlank(payIn.getProcessInstance())) {
                // Workflow instance already exists
                return payIn.getProcessInstance();
            }

            ProcessInstanceDto instance = this.bpmEngine.findInstance(payInKey.toString());
            if (instance == null) {
                // Set business key
                final String businessKey= payInKey.toString();

                // Set variables
                final Map<String, VariableValueDto> variables = BpmInstanceVariablesBuilder.builder()
                    .variableAsString(EnumProcessInstanceVariable.START_USER_KEY.getValue(), "")
                    .variableAsString("consumerKey", payIn.getConsumerKey().toString())
                    .variableAsString("payInId", payInId)
                    .variableAsString("payInKey", payInKey.toString())
                    .variableAsString("payInReferenceNumber", payIn.getReferenceNumber())
                    .variableAsString("payInStatus", payInStatus.toString())
                    .variableAsString("providerKey", payIn.getProviderKey().get(0).toString())
                    .build();

                instance = this.bpmEngine.startProcessDefinitionByKey(workflow, businessKey, variables);
            }

            payInRepository.setPayInWorkflowInstance(payIn.getId(), instance.getDefinitionId(), instance.getId());

            return instance.getId();
        } catch(final Exception ex) {
            logger.error(
                String.format("Failed to start workflow instance [workflow=%s, businessKey=%s]", workflow, payInKey), ex
            );
        }

        return null;
    }

    @Override
    @Transactional
    public void updatePayoff(UUID payInKey) {
        this.updatePayoff(payInKey, EnumPayoffStatus.PAID);
    }

    @Override
    @Transactional
    public void cancelPayoff(UUID payInKey) {
        this.updatePayoff(payInKey, EnumPayoffStatus.DUE);
    }

    private void updatePayoff(UUID payInKey, EnumPayoffStatus status) {
        try {
            final PayInEntity payIn = ensureServiceBillingPayin(payInKey);

            payIn.getItems().stream().map(i -> (PayInServiceBillingItemEntity) i).forEach(i -> {
                i.getServiceBilling().setStatus(status);
                i.getServiceBilling().setPayin(payIn);
            });

            this.payInRepository.saveAndFlush(payIn);

            if (status == EnumPayoffStatus.PAID) {
                payIn.getItems().stream().map(i -> (PayInServiceBillingItemEntity) i).forEach(i -> {
                    this.sendPayoffNotification(i);
                });
            }
        } catch (final PaymentException pEx) {
            throw pEx;
        } catch (final Exception ex) {
            throw new PaymentException(PaymentMessageCode.SERVICE_BILLING_RECORD_PAYOFF_ERROR, "Subscription billing payoff failed", ex);
        }
    }

    private PayInEntity ensureServiceBillingPayin(UUID payInKey) throws PaymentException {
        final PayInEntity payIn = payInRepository.findOneEntityByKey(payInKey).orElse(null);
        if (payIn == null) {
            throw new PaymentException(PaymentMessageCode.PAYIN_NOT_FOUND, "Payin record was not found");
        }

        final List<EnumPaymentItemType> types = payIn.getItems().stream().map(i -> i.getType()).distinct().toList();
        Assert.isTrue(types.size() == 1, "Expected items of the same type");

        if (types.get(0) != EnumPaymentItemType.SERVICE_BILLING) {
            throw new PaymentException(
                PaymentMessageCode.SERVICE_BILLING_RECORD_INVALID_ITEM_TYPE,
                "Expected items for subscription billing records"
            );
        }

        return payIn;
    }

    private void sendPayoffNotification(
        PayInServiceBillingItemEntity item
    ) {
        final var serviceBilling = item.getServiceBilling();
        final var subscription   = serviceBilling.getSubscription();
        final var service        = serviceBilling.getUserService();

        Assert.isTrue(subscription != null || service != null, "Expected either a non-null subscription or user service");
        
        final var year  = serviceBilling.getFromDate().getYear();
        final var month = serviceBilling.getFromDate().getMonthValue();
        final var type  = subscription == null
            ? EnumNotificationType.USER_SERVICE_BILLING_PAYOFF
            : EnumNotificationType.SUBSCRIPTION_BILLING_PAYOFF;  
        
        final var serviceKey    = subscription == null ? service.getKey() : subscription.getKey();
        final var idempotentKey = buildIdempotentKey(serviceKey, year, month, IDEMPOTENT_KEY_SUFFIX_PAYOFF);

        final var recipientKey = subscription == null
            ? service.getAccount().getParent() == null ? service.getAccount().getKey() : service.getAccount().getParent().getKey()
            : subscription.getConsumer().getKey();
        
        final Map<String, Object> variables    = new HashMap<>();
        variables.put("intervalFrom", serviceBilling.getFromDate().format(dateFormat));
        variables.put("intervalTo", serviceBilling.getToDate().format(dateFormat));
        variables.put("amount", this.formatCurrency(serviceBilling.getTotalPrice()));
        
        if (subscription != null) {
            final var assetId = subscription.getAssetId();
            final var asset   = this.catalogueService.findOne(null, assetId, null, false);

            variables.put("asset_id", assetId);
            variables.put("asset_title", asset.getTitle());
            variables.put("asset_version", asset.getVersion());

        }
        if (service != null) {
            variables.put("service_title", service.getTitle());
            variables.put("service_version", service.getVersion());
        }

        final JsonNode data = this.notificationMessageBuilder.collectNotificationData(type, variables);

        final ServerNotificationCommandDto notification = ServerNotificationCommandDto.builder()
            .data(data)
            .eventType(type.toString())
            .idempotentKey(idempotentKey)
            .recipient(recipientKey)
            .text(this.notificationMessageBuilder.composeNotificationText(type, data))
            .build();

        messageClient.getObject().sendNotification(notification);
    }

    @Override
    public PerCallPricingModelCommandDto getPrivateServicePricingModel() {
        try {
            final var setting = this.settingRepository.findOne(EnumSetting.USER_SERVICE_PRICE_PER_CALL);
            if (setting == null || StringUtils.isBlank(setting.getValue())) {
                return null;
            }
            final var pricingModel = this.objectMapper.readValue(setting.getValue(), new TypeReference<PerCallPricingModelCommandDto>() { });

            return pricingModel;
        } catch (final JacksonException ex) {
            throw new ServiceException(BasicMessageCode.SerializationError, "Failed to parse the pricing model for private OGC services", ex);
        }
    }

    @Transactional
    @Override
    public void setPrivateServicePricingModel(int userId, PerCallPricingModelCommandDto model) {
        try {
            final var setting = this.settingRepository.findOne(EnumSetting.USER_SERVICE_PRICE_PER_CALL);
            this.settingHistoryRepository.create(setting);

            final var value   = this.objectMapper.writeValueAsString(model);
            final var update  = SettingUpdateDto.of(EnumSetting.USER_SERVICE_PRICE_PER_CALL.getKey(), EnumService.ADMIN_GATEWAY, value);
            final var command = SettingUpdateCommandDto.of(userId, List.of(update));
            this.settingRepository.update(command);
        } catch (final JacksonException ex) {
            throw new ServiceException(BasicMessageCode.SerializationError, "Failed to serialize the pricing model for private OGC services",
                    ex);
        }
    }

    @AllArgsConstructor
    @Builder
    @Getter
    private static class OperationContext {

        private final UUID    userKey;
        private final int     year;
        private final int     month;
        private final boolean quotationOnly;

        private final AccountEntity                   account;
        private final BillingSubscriptionDates        dates;
        private final List<AccountSubscriptionEntity> subscriptions;
        private final List<UserServiceEntity>         userServices;
        private final List<ServiceUseStatsDto>        useStats;

    }

}
