package eu.opertusmundi.common.service;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.opertusmundi.common.domain.AccountSubscriptionEntity;
import eu.opertusmundi.common.domain.AccountSubscriptionSkuEntity;
import eu.opertusmundi.common.domain.OrderItemEntity;
import eu.opertusmundi.common.domain.SubscriptionBillingEntity;
import eu.opertusmundi.common.model.account.AccountDto;
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
            final List<SubscriptionBillingDto> result = new ArrayList<>();

            final AccountDto account = this.accountRepository.findOneByKeyObject(userKey).orElse(null);
            if (account == null) {
                throw new PaymentException(PaymentMessageCode.ACCOUNT_NOT_FOUND, String.format("Account was not found [userKey=%s]", userKey));
            }

            final List<AccountSubscriptionEntity> subsciptions = this.accountSubscriptionRepository.findAllEntitiesByConsumer(userKey);
            final List<ServiceUseStatsDto>        stats        = this.subscriptionUseStatsService.getUseStats(userKey, year, month);

            for (final ServiceUseStatsDto subStats : stats) {
                // Ignore unused subscriptions
                if (subStats.getCalls() == 0 && subStats.getRows() == 0) {
                    continue;
                }
                // Find subscription
                final AccountSubscriptionEntity subscription = subsciptions.stream()
                    .filter(s -> s.getOrder().getKey().equals(subStats.getSubscriptionKey()))
                    .findFirst()
                    .orElse(null);
                if (subscription == null) {
                    throw new PaymentException(PaymentMessageCode.SUBSCRIPTION_NOT_FOUND, String.format(
                        "Subscription was not found [subscriptionKey=%s]", subStats.getSubscriptionKey()
                    ));
                }

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
                    final int usedRows       = subStats.getRows();
                    final int usedCalls      = subStats.getCalls();

                    if (usedRows > 0 && availableRows > 0) {
                        final int prepaidRows = availableRows >= usedRows ? usedRows : availableRows;
                        totalSkuRows += prepaidRows;
                        sku.usePrepaidRows(prepaidRows);
                        subStats.decreaseRows(prepaidRows);
                    }
                    if (usedCalls > 0 && availableCalls > 0) {
                        final int prepaidCalls = availableCalls >= usedCalls ? usedCalls : availableCalls;
                        totalSkuCalls += prepaidCalls;
                        sku.usePrepaidCalls(prepaidCalls);
                        subStats.decreaseCalls(prepaidCalls);
                    }
                }
                this.accountSubscriptionRepository.saveAndFlush(subscription);

                // Compute quotation (exclude prepaid rows/calls)
                final QuotationDto quotation = this.quotationService.createQuotation(pricingModel.getModel(), subStats);

                // Add billing record
                final ZonedDateTime fromDate = ZonedDateTime.now()
                    .withYear(year)
                    .withMonth(month)
                    .truncatedTo(ChronoUnit.DAYS)
                    .with(TemporalAdjusters.firstDayOfMonth());
                final ZonedDateTime toDate   = fromDate.with(TemporalAdjusters.lastDayOfMonth());

                SubscriptionBillingEntity subBilling = SubscriptionBillingEntity.builder()
                    .fromDate(fromDate)
                    .toDate(toDate)
                    .skuTotalCalls(totalSkuCalls)
                    .skuTotalRows(totalSkuRows)
                    .subscription(subscription)
                    .totalCalls(totalCalls)
                    .totalRows(totalRows)
                    .totalPriceExcludingTax(quotation.getTotalPriceExcludingTax())
                    .totalPrice(quotation.getTotalPrice())
                    .totalTax(quotation.getTax())
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

}
