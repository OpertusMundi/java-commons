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

import com.fasterxml.jackson.databind.JsonNode;
import com.ibm.icu.text.NumberFormat;

import eu.opertusmundi.common.domain.AccountSubscriptionEntity;
import eu.opertusmundi.common.domain.AccountSubscriptionSkuEntity;
import eu.opertusmundi.common.domain.OrderItemEntity;
import eu.opertusmundi.common.domain.PayInEntity;
import eu.opertusmundi.common.domain.PayInSubscriptionBillingItemEntity;
import eu.opertusmundi.common.domain.SubscriptionBillingBatchEntity;
import eu.opertusmundi.common.domain.SubscriptionBillingEntity;
import eu.opertusmundi.common.feign.client.MessageServiceFeignClient;
import eu.opertusmundi.common.model.EnumSortingOrder;
import eu.opertusmundi.common.model.EnumView;
import eu.opertusmundi.common.model.PageResultDto;
import eu.opertusmundi.common.model.account.EnumSubscriptionBillingSortField;
import eu.opertusmundi.common.model.account.EnumSubscriptionBillingStatus;
import eu.opertusmundi.common.model.catalogue.client.CatalogueItemDetailsDto;
import eu.opertusmundi.common.model.message.EnumNotificationType;
import eu.opertusmundi.common.model.message.server.ServerNotificationCommandDto;
import eu.opertusmundi.common.model.payment.BillingSubscriptionDates;
import eu.opertusmundi.common.model.payment.EnumPaymentItemType;
import eu.opertusmundi.common.model.payment.EnumSubscriptionBillingBatchStatus;
import eu.opertusmundi.common.model.payment.EnumTransactionStatus;
import eu.opertusmundi.common.model.payment.PaymentException;
import eu.opertusmundi.common.model.payment.PaymentMessageCode;
import eu.opertusmundi.common.model.payment.ServiceUseStatsDto;
import eu.opertusmundi.common.model.payment.SubscriptionBillingBatchCommandDto;
import eu.opertusmundi.common.model.payment.SubscriptionBillingBatchDto;
import eu.opertusmundi.common.model.payment.SubscriptionBillingDto;
import eu.opertusmundi.common.model.payment.helpdesk.HelpdeskPayInDto;
import eu.opertusmundi.common.model.payment.helpdesk.HelpdeskSubscriptionBillingDto;
import eu.opertusmundi.common.model.pricing.EffectivePricingModelDto;
import eu.opertusmundi.common.model.pricing.QuotationDto;
import eu.opertusmundi.common.model.pricing.QuotationException;
import eu.opertusmundi.common.model.workflow.EnumProcessInstanceVariable;
import eu.opertusmundi.common.model.workflow.EnumWorkflow;
import eu.opertusmundi.common.repository.AccountRepository;
import eu.opertusmundi.common.repository.AccountSubscriptionRepository;
import eu.opertusmundi.common.repository.PayInRepository;
import eu.opertusmundi.common.repository.SubscriptionBillingBatchRepository;
import eu.opertusmundi.common.repository.SubscriptionBillingRepository;
import eu.opertusmundi.common.service.messaging.NotificationMessageHelper;
import eu.opertusmundi.common.util.BpmEngineUtils;
import eu.opertusmundi.common.util.BpmInstanceVariablesBuilder;

@Service
public class DefaultSubscriptionBillingService implements SubscriptionBillingService {

    private static final Logger logger = LoggerFactory.getLogger(DefaultSubscriptionBillingService.class);

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
    private final ObjectProvider<MessageServiceFeignClient> messageClient;
    private final NotificationMessageHelper                 notificationMessageBuilder;
    private final PayInRepository                           payInRepository;
    private final QuotationService                          quotationService;
    private final SubscriptionBillingBatchRepository        subscriptionBillingBatchRepository;
    private final SubscriptionBillingRepository             subscriptionBillingRepository;
    private final SubscriptionUseStatsService               subscriptionUseStatsService;

    @Autowired
    public DefaultSubscriptionBillingService(
        AccountRepository                           accountRepository,
        AccountSubscriptionRepository               accountSubscriptionRepository,
        BpmEngineUtils                              bpmEngine,
        CatalogueService                            catalogueService,
        ObjectProvider<MessageServiceFeignClient>   messageClient,
        NotificationMessageHelper                   notificationMessageBuilder,
        PayInRepository                             payInRepository,
        QuotationService                            quotationService,
        SubscriptionBillingBatchRepository          subscriptionBillingBatchRepository,
        SubscriptionBillingRepository               subscriptionBillingRepository,
        SubscriptionUseStatsService                 subscriptionUseStatsService
    ) {
        this.accountRepository                  = accountRepository;
        this.accountSubscriptionRepository      = accountSubscriptionRepository;
        this.bpmEngine                          = bpmEngine;
        this.catalogueService                   = catalogueService;
        this.messageClient                      = messageClient;
        this.notificationMessageBuilder         = notificationMessageBuilder;
        this.payInRepository                    = payInRepository;
        this.quotationService                   = quotationService;
        this.subscriptionBillingBatchRepository = subscriptionBillingBatchRepository;
        this.subscriptionBillingRepository      = subscriptionBillingRepository;
        this.subscriptionUseStatsService        = subscriptionUseStatsService;
    }

    @Override
    public PageResultDto<SubscriptionBillingDto> findAllSubscriptionBillingRecords(
        EnumView view, UUID consumerKey, UUID providerKey, UUID subscriptionKey, Set<EnumSubscriptionBillingStatus> status,
        int pageIndex, int pageSize,
        EnumSubscriptionBillingSortField orderBy, EnumSortingOrder order
    ) {
        final var direction   = order == EnumSortingOrder.DESC ? Direction.DESC : Direction.ASC;
        final var pageRequest = PageRequest.of(pageIndex, pageSize, Sort.by(direction, orderBy.getValue()));

        final var page = this.subscriptionBillingRepository .findAllObjects(
            view, true, consumerKey, providerKey, subscriptionKey, status, pageRequest
        );

        final var count   = page.getTotalElements();
        final var records = page.getContent();

        return PageResultDto.of(pageIndex, pageSize, records, count);
    }

    @Override
    public Optional<SubscriptionBillingDto> findOneSubscriptionBillingRecord(EnumView view, UUID key) {
        return this.subscriptionBillingRepository.findOneObject(view, key, true);
    }

    @Override
    public Optional<SubscriptionBillingBatchDto> findOneBillingIntervalByKey(UUID key) {
        return this.subscriptionBillingBatchRepository.findOneObjectByKey(key);
    }

    @Override
    @Transactional
    public SubscriptionBillingBatchDto start(SubscriptionBillingBatchCommandDto command) throws PaymentException {
        // Compute dates
        final BillingSubscriptionDates dates = computeInterval(command);

        final SubscriptionBillingBatchEntity batch = subscriptionBillingBatchRepository.findOneOrCreate(command, dates);

        final var runningInstances = this.bpmEngine.findInstancesByProcessDefinitionKey(EnumWorkflow.SUBSCRIPTION_BILLING.getKey());
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

            instance = this.bpmEngine.startProcessDefinitionByKey(EnumWorkflow.SUBSCRIPTION_BILLING, businessKey, variables);
        }

        batch.setProcessDefinition(instance.getDefinitionId());
        batch.setProcessInstance(instance.getId());

        this.subscriptionBillingBatchRepository.saveAndFlush(batch);

        return batch.toDto();
    }

    @Override
    @Transactional
    public List<SubscriptionBillingDto> create(UUID userKey, int year, int month, boolean quotationOnly) throws PaymentException {
        try {
            final var dates  = computeInterval(year, month);
            final var result = new ArrayList<SubscriptionBillingDto>();

            final var account = this.accountRepository.findOneByKeyObject(userKey).orElse(null);
            if (account == null) {
                throw new PaymentException(PaymentMessageCode.ACCOUNT_NOT_FOUND, String.format("Account was not found [userKey=%s]", userKey));
            }

            final var subscriptions = this.accountSubscriptionRepository.findAllEntitiesByConsumer(userKey, null);
            final var stats         = this.subscriptionUseStatsService.getUseStats(userKey, year, month);

            for (final ServiceUseStatsDto subStats : stats) {
                // Ignore unused subscriptions
                if (subStats.getCalls() == 0 && subStats.getRows() == 0) {
                    continue;
                }

                // Find subscription
                final var subscription = subscriptions.stream()
                    .filter(s -> s.getKey().equals(subStats.getSubscriptionKey()))
                    .findFirst()
                    .orElse(null);
                if (subscription == null) {
                    throw new PaymentException(PaymentMessageCode.SUBSCRIPTION_NOT_FOUND, String.format(
                        "Subscription was not found [subscriptionKey=%s]", subStats.getSubscriptionKey()
                    ));
                }
                // Check if billing has already been computed
                var record = this.subscriptionBillingRepository
                    .findOneBySubscriptionAndInterval(dates.getDateFrom(), dates.getDateTo(), subscription.getId())
                    .map(b-> b.toHelpdeskDto(true))
                    .orElse(null);

                if (record == null) {
                    record = this.compute(dates, subscription, subStats, quotationOnly);
                } else {
                    this.validateBillingStats(record, subStats);
                }
                result.add(record);

                if (notificationPerSubscription && record.getTotalPrice().compareTo(BigDecimal.ZERO) > 0) {
                    this.sendChargeNotification(dates, subscription, record);
                }
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

    private HelpdeskSubscriptionBillingDto compute(
        BillingSubscriptionDates dates, AccountSubscriptionEntity subscription, ServiceUseStatsDto subStats, boolean quotationOnly
    ) {
        Assert.notNull(dates, "Expected a non-null date interval");
        Assert.notNull(subscription, "Expected a non-null subscription");
        Assert.notNull(subStats, "Expected a non-null use statistics object");
        Assert.isTrue(subscription.getKey().equals(subStats.getSubscriptionKey()), "Subscription and ust statistics object key mismatch");

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

        SubscriptionBillingEntity subBilling = SubscriptionBillingEntity.builder()
            .createdOn(now)
            .dueDate(dates.getDateDue())
            .fromDate(dates.getDateFrom())
            .pricingModel(pricingModel.getModel())
            .skuTotalCalls(totalSkuCalls)
            .skuTotalRows(totalSkuRows)
            .subscription(subscription)
            .toDate(dates.getDateTo())
            .totalCalls(totalCalls)
            .totalRows(totalRows)
            .totalPriceExcludingTax(quotation.getTotalPriceExcludingTax())
            .totalPrice(quotation.getTotalPrice())
            .totalTax(quotation.getTax())
            .stats(initialStats)
            .status(quotation.getTotalPrice().compareTo(BigDecimal.ZERO) == 0
                ? EnumSubscriptionBillingStatus.NO_CHARGE
                : EnumSubscriptionBillingStatus.DUE)
            .updatedOn(now)
            .build();

        if (!quotationOnly) {
            subBilling = subscriptionBillingRepository.saveAndFlush(subBilling);
        }

        return subBilling.toHelpdeskDto(true);
    }

    private void sendChargeNotification(
        BillingSubscriptionDates dates, AccountSubscriptionEntity subscription, HelpdeskSubscriptionBillingDto record
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

    private void sendChargeNotification(
        UUID userKey, BillingSubscriptionDates dates, BigDecimal total
    ) {
        final String               idempotentKey = buildIdempotentKey(userKey, dates, IDEMPOTENT_KEY_SUFFIX_CHARGE);
        final EnumNotificationType type          = EnumNotificationType.SUBSCRIPTION_BILLING_TOTAL_CHARGE;
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
        final SubscriptionBillingBatchEntity e = this.subscriptionBillingBatchRepository.findOneByKey(key).get();

        e.setStatus(EnumSubscriptionBillingBatchStatus.SUCCEEDED);
        e.setTotalPrice(totalPrice);
        e.setTotalPriceExcludingTax(totalPriceExcludingTax);
        e.setTotalSubscriptions(totalSubscriptions);
        e.setTotalTax(totalTax);

        this.subscriptionBillingBatchRepository.saveAndFlush(e);
    }

    @Override
    @Transactional
    public void fail(UUID key) {
        this.subscriptionBillingBatchRepository.setStatus(key, EnumSubscriptionBillingBatchStatus.FAILED);
    }

    private BillingSubscriptionDates computeInterval(SubscriptionBillingBatchCommandDto command) {
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

    private void validateBillingStats(SubscriptionBillingDto record, ServiceUseStatsDto newStats) {
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
        final EnumWorkflow workflow = EnumWorkflow.SUBSCRIPTION_BILLING_CONSUMER_PAYIN;

        try {
            final HelpdeskPayInDto payIn = this.ensureSubscriptionBillingPayin(payInKey).toHelpdeskDto(true);
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
        this.updatePayoff(payInKey, EnumSubscriptionBillingStatus.PAID);
    }

    @Override
    @Transactional
    public void cancelPayoff(UUID payInKey) {
        this.updatePayoff(payInKey, EnumSubscriptionBillingStatus.DUE);
    }

    private void updatePayoff(UUID payInKey, EnumSubscriptionBillingStatus status) {
        try {
            final PayInEntity payIn = ensureSubscriptionBillingPayin(payInKey);

            payIn.getItems().stream().map(i -> (PayInSubscriptionBillingItemEntity) i).forEach(i -> {
                i.getSubscriptionBilling().setStatus(status);
                i.getSubscriptionBilling().setPayin(payIn);
            });

            this.payInRepository.saveAndFlush(payIn);

            if (status == EnumSubscriptionBillingStatus.PAID) {
                payIn.getItems().stream().map(i -> (PayInSubscriptionBillingItemEntity) i).forEach(i -> {
                    this.sendPayoffNotification(i);
                });
            }
        } catch (final PaymentException pEx) {
            throw pEx;
        } catch (final Exception ex) {
            throw new PaymentException(PaymentMessageCode.SUBSCRIPTION_BILLING_PAYOFF_ERROR, "Subscription billing payoff failed", ex);
        }
    }

    private PayInEntity ensureSubscriptionBillingPayin(UUID payInKey) throws PaymentException {
        final PayInEntity payIn = payInRepository.findOneEntityByKey(payInKey).orElse(null);
        if (payIn == null) {
            throw new PaymentException(PaymentMessageCode.PAYIN_NOT_FOUND, "Payin record was not found");
        }

        final List<EnumPaymentItemType> types = payIn.getItems().stream().map(i -> i.getType()).distinct().toList();
        Assert.isTrue(types.size() == 1, "Expected items of the same type");

        if (types.get(0) != EnumPaymentItemType.SUBSCRIPTION_BILLING) {
            throw new PaymentException(
                PaymentMessageCode.SUBSCRIPTION_BILLING_INVALID_ITEM_TYPE,
                "Expected items for subscription billing records"
            );
        }

        return payIn;
    }

    private void sendPayoffNotification(
        PayInSubscriptionBillingItemEntity item
    ) {
        final SubscriptionBillingEntity subscriptionBilling = item.getSubscriptionBilling();
        final AccountSubscriptionEntity subscription        = subscriptionBilling.getSubscription();
        final UUID                      subscriptionKey     = subscription.getKey();
        final String                    assetId             = subscription.getAssetId();
        final int                       year                = subscriptionBilling.getFromDate().getYear();
        final int                       month               = subscriptionBilling.getFromDate().getMonthValue();
        final String                    idempotentKey       = buildIdempotentKey(subscriptionKey, year, month, IDEMPOTENT_KEY_SUFFIX_PAYOFF);
        final EnumNotificationType      type                = EnumNotificationType.SUBSCRIPTION_BILLING_SINGLE_CHARGE;
        final CatalogueItemDetailsDto   asset               = this.catalogueService.findOne(null, assetId, null, false);

        final Map<String, Object> variables = new HashMap<>();
        variables.put("intervalFrom", subscriptionBilling.getFromDate().format(dateFormat));
        variables.put("intervalTo", subscriptionBilling.getToDate().format(dateFormat));
        variables.put("asset_id", assetId);
        variables.put("asset_title", asset.getTitle());
        variables.put("asset_version", asset.getVersion());
        variables.put("amount", this.formatCurrency(subscriptionBilling.getTotalPrice()));

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

}
