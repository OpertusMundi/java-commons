package eu.opertusmundi.common.repository;

import java.time.temporal.WeekFields;
import java.util.Locale;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.opertusmundi.common.domain.OrderEntity;
import eu.opertusmundi.common.domain.OrderItemEntity;
import eu.opertusmundi.common.domain.PayInEntity;
import eu.opertusmundi.common.domain.PayInItemEntity;
import eu.opertusmundi.common.domain.PayInItemHistoryEntity;
import eu.opertusmundi.common.domain.PayInOrderItemEntity;
import eu.opertusmundi.common.domain.PayInSubscriptionBillingItemEntity;
import eu.opertusmundi.common.domain.SubscriptionBillingEntity;
import eu.opertusmundi.common.model.order.EnumOrderItemType;
import eu.opertusmundi.common.model.payment.TransferDto;

@Repository
@Transactional(readOnly = false)
public interface PayInItemHistoryRepository extends JpaRepository<PayInItemHistoryEntity, Integer> {

    Optional<PayInItemHistoryEntity> findOneById(Integer id);

    default void createIfNotExists(PayInItemEntity item) {
        // Item identifier is equal to the history item identifier
        final PayInItemHistoryEntity existing = this.findOneById(item.getId()).orElse(null);
        if (existing != null) {
            // Item already created
            return;
        }

        final PayInEntity            payIn      = item.getPayin();
        final PayInItemHistoryEntity e          = new PayInItemHistoryEntity();
        OrderEntity                  order      = null;
        SubscriptionBillingEntity    subBilling = null;

        switch (item.getType()) {
            case ORDER :
                order = ((PayInOrderItemEntity) item).getOrder();
                break;
            case SUBSCRIPTION_BILLING :
                subBilling = ((PayInSubscriptionBillingItemEntity) item).getSubscriptionBilling();
                break;
        }

        // A PayIn item may have a reference to either an order or a
        // subscription billing. An order should only have a single order item.
        if (order != null) {
            Assert.isTrue(order.getItems().size() == 1, "An order must have only one order item");

            final OrderItemEntity orderItem = order.getItems().get(0);

            e.setAssetId(orderItem.getAssetId());
            e.setAssetType(orderItem.getType());
            e.setPayInCountry(order.getCountry());
            e.setPayInTotalPrice(orderItem.getTotalPrice());
            e.setPayInTotalPriceExcludingTax(orderItem.getTotalPriceExcludingTax());
            e.setPayInTotalTax(orderItem.getTotalTax());
            e.setProvider(orderItem.getProvider().getId());
            e.setProviderKey(orderItem.getProvider().getKey());
            e.setSegment(orderItem.getSegment());
        }
        if (subBilling != null) {
            // TODO: Implement
            e.setAssetId(subBilling.getSubscription().getAssetId());
            e.setAssetType(EnumOrderItemType.SUBSCRIPTION);
            e.setPayInCountry(subBilling.getSubscription().getConsumer().getCountry());
            e.setPayInTotalPrice(subBilling.getTotalPrice());
            e.setPayInTotalPriceExcludingTax(subBilling.getTotalPriceExcludingTax());
            e.setPayInTotalTax(subBilling.getTotalTax());
            e.setProvider(subBilling.getSubscription().getProvider().getId());
            e.setProviderKey(subBilling.getSubscription().getProvider().getKey());
            e.setSegment(subBilling.getSubscription().getSegment());
        }

        e.setConsumer(payIn.getConsumer().getId());
        e.setId(item.getId());
        e.setPayInDay(payIn.getExecutedOn().getDayOfMonth());
        e.setPayInExecutedOn(payIn.getExecutedOn());
        e.setPayInId(payIn.getId());
        e.setPayInMonth(payIn.getExecutedOn().getMonthValue());
        e.setPayInProviderId(payIn.getPayIn());
        e.setPayInWeek(payIn.getExecutedOn().get(WeekFields.of(Locale.getDefault()).weekOfYear()));
        e.setPayInYear(payIn.getExecutedOn().getYear());

        this.saveAndFlush(e);
    }

    default void updateTransfer(Integer id, TransferDto transfer) {
        Assert.notNull(transfer, "Expected a non-null transfer");

        final PayInItemHistoryEntity history = this.findOneById(id).orElse(null);

        Assert.notNull(history, "Expected a non-null history record");

        history.setTransferCreditedFunds(transfer.getCreditedFunds());
        history.setTransferDay(transfer.getExecutedOn().getDayOfMonth());
        history.setTransferExecutedOn(transfer.getExecutedOn());
        history.setTransferMonth(transfer.getExecutedOn().getMonthValue());
        history.setTransferPlatformFees(transfer.getFees());
        history.setTransferProviderId(transfer.getId());
        history.setTransferWeek(transfer.getExecutedOn().get(WeekFields.of(Locale.getDefault()).weekOfYear()));
        history.setTransferYear(transfer.getExecutedOn().getYear());

        this.saveAndFlush(history);
    }

}
