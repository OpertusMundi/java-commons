package eu.opertusmundi.common.domain;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import eu.opertusmundi.common.model.payment.EnumPaymentItemType;
import eu.opertusmundi.common.model.payment.PayInItemDto;
import eu.opertusmundi.common.model.payment.PaymentException;
import eu.opertusmundi.common.model.payment.SubscriptionBillingPayInItemDto;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "PayInSubscriptionBillingItem")
@DiscriminatorValue("SUBSCRIPTION_BILLING")
public class PayInSubscriptionBillingItemEntity extends PayInItemEntity {

    public PayInSubscriptionBillingItemEntity() {
        super();

        this.type = EnumPaymentItemType.SUBSCRIPTION_BILLING;
    }

    @ManyToOne(targetEntity = SubscriptionBillingEntity.class)
    @JoinColumn(name = "subscription_billing", nullable = false)
    @Getter
    @Setter
    private SubscriptionBillingEntity subscriptionBilling;

    @Override
    public PayInItemDto toDto() throws PaymentException {
        final SubscriptionBillingPayInItemDto i = new SubscriptionBillingPayInItemDto();

        i.setId(id);
        i.setIndex(index);
        i.setSubscriptionBilling(this.subscriptionBilling.toDto());
        i.setTransfer(this.toTransferDto());
        i.setType(type);

        return i;
    }

}