package eu.opertusmundi.common.domain;

import java.util.UUID;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import eu.opertusmundi.common.model.payment.EnumPaymentItemType;
import eu.opertusmundi.common.model.payment.PayInItemDto;
import eu.opertusmundi.common.model.payment.consumer.ConsumerPayInItemDto;
import eu.opertusmundi.common.model.payment.consumer.ConsumerServiceBillingPayInItemDto;
import eu.opertusmundi.common.model.payment.helpdesk.HelpdeskPayInItemDto;
import eu.opertusmundi.common.model.payment.helpdesk.HelpdeskServiceBillingPayInItemDto;
import eu.opertusmundi.common.model.payment.provider.ProviderPayInItemDto;
import eu.opertusmundi.common.model.payment.provider.ProviderServiceBillingPayInItemDto;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "PayInServiceBillingItem")
@DiscriminatorValue("SERVICE_BILLING")
public class PayInServiceBillingItemEntity extends PayInItemEntity {

    public PayInServiceBillingItemEntity() {
        super();

        this.key  = UUID.randomUUID();
        this.type = EnumPaymentItemType.SERVICE_BILLING;
    }

    @ManyToOne(targetEntity = ServiceBillingEntity.class)
    @JoinColumn(name = "service_billing", nullable = false)
    @Getter
    @Setter
    private ServiceBillingEntity serviceBilling;

    public void updateDto(PayInItemDto i) {
        i.setId(id);
        i.setIndex(index);
        i.setPayIn(payin.getKey());
        i.setType(type);
    }

    @Override
    public ConsumerPayInItemDto toConsumerDto(boolean includeDetails, boolean includePayIn) {
        final ConsumerServiceBillingPayInItemDto i = new ConsumerServiceBillingPayInItemDto();

        this.updateDto(i);

        i.setServiceBilling(this.serviceBilling.toConsumerDto(includeDetails));

        return i;
    }

    @Override
    public ProviderPayInItemDto toProviderDto(boolean includeDetails, boolean includePayIn) {
        final ProviderServiceBillingPayInItemDto i = new ProviderServiceBillingPayInItemDto();

        this.updateDto(i);

        i.setServiceBilling(this.serviceBilling.toProviderDto(includeDetails));
        if (this.getTransfer() != null) {
            i.setTransfer(this.getTransfer().toDto(false));
        }

        return i;
    }

    @Override
    public HelpdeskPayInItemDto toHelpdeskDto(boolean includeDetails, boolean includePayIn) {
        final HelpdeskServiceBillingPayInItemDto i = new HelpdeskServiceBillingPayInItemDto();

        this.updateDto(i);

        i.setServiceBilling(this.serviceBilling.toHelpdeskDto(includeDetails));
        if (this.getTransfer() != null) {
            i.setTransfer(this.getTransfer().toDto(true));
        }

        return i;
    }

}