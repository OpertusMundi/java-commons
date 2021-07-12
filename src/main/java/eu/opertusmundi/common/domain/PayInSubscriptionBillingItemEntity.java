package eu.opertusmundi.common.domain;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import eu.opertusmundi.common.model.payment.EnumPaymentItemType;
import eu.opertusmundi.common.model.payment.PayInItemDto;
import eu.opertusmundi.common.model.payment.consumer.ConsumerPayInItemDto;
import eu.opertusmundi.common.model.payment.consumer.ConsumerSubscriptionBillingPayInItemDto;
import eu.opertusmundi.common.model.payment.helpdesk.HelpdeskPayInItemDto;
import eu.opertusmundi.common.model.payment.helpdesk.HelpdeskSubscriptionBillingPayInItemDto;
import eu.opertusmundi.common.model.payment.provider.ProviderPayInItemDto;
import eu.opertusmundi.common.model.payment.provider.ProviderSubscriptionBillingPayInItemDto;
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

    public void updateDto(PayInItemDto i) {
        i.setId(id);
        i.setIndex(index);
        i.setPayIn(payin.getKey());
        i.setType(type);
    }

    @Override
    public ConsumerPayInItemDto toConsumerDto(boolean includeDetails) {
        final ConsumerSubscriptionBillingPayInItemDto i = new ConsumerSubscriptionBillingPayInItemDto();

        this.updateDto(i);

        i.setSubscriptionBilling(this.subscriptionBilling.toConsumerDto(includeDetails));

        return i;
    }

    @Override
    public ProviderPayInItemDto toProviderDto(boolean includeDetails) {
        final ProviderSubscriptionBillingPayInItemDto i = new ProviderSubscriptionBillingPayInItemDto();

        this.updateDto(i);

        i.setSubscriptionBilling(this.subscriptionBilling.toProviderDto(includeDetails));
        i.setTransfer(this.toTransferDto(false));

        return i;
    }

    @Override
    public HelpdeskPayInItemDto toHelpdeskDto(boolean includeDetails) {
        final HelpdeskSubscriptionBillingPayInItemDto i = new HelpdeskSubscriptionBillingPayInItemDto();

        this.updateDto(i);

        i.setSubscriptionBilling(this.subscriptionBilling.toHelpdeskDto(includeDetails));
        i.setTransfer(this.toTransferDto(true));

        return i;
    }

}