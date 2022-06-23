package eu.opertusmundi.common.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.opertusmundi.common.domain.AccountSubscriptionEntity;
import eu.opertusmundi.common.domain.AccountSubscriptionSkuEntity;
import eu.opertusmundi.common.domain.OrderItemEntity;
import eu.opertusmundi.common.domain.SubscriptionBillingEntity;
import eu.opertusmundi.common.model.account.AccountDto;
import eu.opertusmundi.common.model.account.EnumSubscriptionBillingStatus;
import eu.opertusmundi.common.model.payment.PaymentException;
import eu.opertusmundi.common.model.payment.PaymentMessageCode;
import eu.opertusmundi.common.model.payment.ServiceUseStatsDto;
import eu.opertusmundi.common.model.payment.SubscriptionBillingDto;
import eu.opertusmundi.common.model.pricing.EffectivePricingModelDto;
import eu.opertusmundi.common.model.pricing.QuotationDto;
import eu.opertusmundi.common.model.pricing.QuotationException;
import eu.opertusmundi.common.repository.AccountRepository;
import eu.opertusmundi.common.repository.AccountSubscriptionRepository;
import eu.opertusmundi.common.repository.SubscriptionBillingRepository;

@Service
public class DefaultSubscriptionBillingService implements SubscriptionBillingService {

    private static final Logger logger = LoggerFactory.getLogger(DefaultSubscriptionBillingService.class);

    /**
     * Offset in days after the first day of the current month, at which a
     * quotation may be created
     */
    @Value("${opertusmundi.subscription-billing.quotation-min-offset:5}")
    private int quotationMinOffset;

    private final AccountRepository accountRepository;

    private final AccountSubscriptionRepository accountSubscriptionRepository;

    private final SubscriptionUseStatsService subscriptionUseStatsService;

    private final SubscriptionBillingRepository subscriptionBillingRepository;

    private final QuotationService quotationService;

    @Autowired
    public DefaultSubscriptionBillingService(
        AccountRepository accountRepository,
        AccountSubscriptionRepository accountSubscriptionRepository,
        SubscriptionUseStatsService subscriptionUseStatsService,
        SubscriptionBillingRepository subscriptionBillingRepository,
        QuotationService quotationService
    ) {
        this.accountRepository             = accountRepository;
        this.accountSubscriptionRepository = accountSubscriptionRepository;
        this.subscriptionUseStatsService   = subscriptionUseStatsService;
        this.subscriptionBillingRepository = subscriptionBillingRepository;
        this.quotationService              = quotationService;
    }

    @Override
    @Transactional
    public List<SubscriptionBillingDto> create(UUID userKey, int year, int month) throws PaymentException {
        try {
            // Validate interval
            this.validateInterval(year, month);

            // Compute dates
            final LocalDate fromDate       = LocalDate.now()
                .withYear(year)
                .withMonth(month)
                .with(TemporalAdjusters.firstDayOfMonth());
            final LocalDate toDate         = fromDate
                .with(TemporalAdjusters.lastDayOfMonth());
            final LocalDate dueDate        = LocalDate.now()
                .with(TemporalAdjusters.firstDayOfMonth())
                .plusMonths(1);
            final LocalDate statsReadyDate = LocalDate.now().with(TemporalAdjusters.firstDayOfMonth()).plusDays(this.quotationMinOffset);

            // Check quotation date
            if (statsReadyDate.isAfter(LocalDate.now())) {
                throw new PaymentException(PaymentMessageCode.USE_STATS_NOT_READY, String.format(
                    "Use statistics are not available for the selected interval was not found [statsReadyDate=%s]", statsReadyDate.toString()
                ));
            }

            final List<SubscriptionBillingDto> result = new ArrayList<>();

            final AccountDto account = this.accountRepository.findOneByKeyObject(userKey).orElse(null);
            if (account == null) {
                throw new PaymentException(PaymentMessageCode.ACCOUNT_NOT_FOUND, String.format("Account was not found [userKey=%s]", userKey));
            }

            final List<AccountSubscriptionEntity> subscriptions = this.accountSubscriptionRepository.findAllEntitiesByConsumer(userKey);
            final List<ServiceUseStatsDto>        stats         = this.subscriptionUseStatsService.getUseStats(userKey, year, month);

            for (final ServiceUseStatsDto subStats : stats) {
                // Ignore unused subscriptions
                if (subStats.getCalls() == 0 && subStats.getRows() == 0) {
                    continue;
                }
                final ServiceUseStatsDto initialStats = subStats.shallowCopy();

                // Find subscription
                final AccountSubscriptionEntity subscription = subscriptions.stream()
                    .filter(s -> s.getOrder().getKey().equals(subStats.getSubscriptionKey()))
                    .findFirst()
                    .orElse(null);
                if (subscription == null) {
                    throw new PaymentException(PaymentMessageCode.SUBSCRIPTION_NOT_FOUND, String.format(
                        "Subscription was not found [subscriptionKey=%s]", subStats.getSubscriptionKey()
                    ));
                }
                // Check if billing has already been computed
                final SubscriptionBillingEntity existing = this.subscriptionBillingRepository
                    .findOneBySubscriptionAndInterval(fromDate, toDate, subscription.getId())
                    .orElse(null);

                if (existing != null) {
                    // Validate use statistics
                    this.validateBillingStats(existing, subStats);
                    // Skip subscription
                    continue;
                }

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
                    .dueDate(dueDate)
                    .fromDate(fromDate)
                    .pricingModel(pricingModel.getModel())
                    .skuTotalCalls(totalSkuCalls)
                    .skuTotalRows(totalSkuRows)
                    .subscription(subscription)
                    .toDate(toDate)
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

                subBilling = subscriptionBillingRepository.saveAndFlush(subBilling);

                result.add(subBilling.toHelpdeskDto(true));
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

    private void validateBillingStats(SubscriptionBillingEntity record, ServiceUseStatsDto newStats) {
        final ServiceUseStatsDto prevStats = record.getStats();

        if (prevStats.getCalls() != newStats.getCalls() || prevStats.getRows() != newStats.getRows()) {
            logger.warn("Use statistics mismatch found [prevStats={}, newStats={}]", prevStats, newStats);
        }
    }

}
