package eu.opertusmundi.common.domain;

import java.util.stream.Collectors;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Table;

import eu.opertusmundi.common.model.payment.EnumPaymentMethod;
import eu.opertusmundi.common.model.payment.PayInDto;
import eu.opertusmundi.common.model.payment.consumer.ConsumerFreePayInDto;
import eu.opertusmundi.common.model.payment.consumer.ConsumerPayInDto;
import eu.opertusmundi.common.model.payment.helpdesk.HelpdeskFreePayInDto;
import eu.opertusmundi.common.model.payment.helpdesk.HelpdeskPayInDto;
import eu.opertusmundi.common.model.payment.provider.ProviderFreePayInDto;
import eu.opertusmundi.common.model.payment.provider.ProviderPayInDto;

@Entity(name = "FreePayIn")
@Table(schema = "billing", name = "`payin_free`")
@DiscriminatorValue(value = "FREE")
public class FreePayInEntity extends PayInEntity {

    public FreePayInEntity() {
        super(EnumPaymentMethod.FREE);
    }

    public void updateDto(PayInDto p) {
        p.setConsumerId(consumer.getId());
        p.setConsumerKey(consumer.getKey());
        p.setCreatedOn(createdOn);
        p.setCurrency(currency);
        p.setExecutedOn(executedOn);
        p.setId(id);
        p.setInvoicePrinted(this.invoicePrintedOn != null);
        p.setInvoicePrintedOn(this.invoicePrintedOn);
        p.setKey(key);
        p.setPaymentMethod(paymentMethod);
        p.setProviderKey(items.stream().map(i -> i.getProvider().getKey()).distinct().collect(Collectors.toList()));
        p.setReferenceNumber(referenceNumber);
        p.setStatus(status);
        p.setStatusUpdatedOn(statusUpdatedOn);
        p.setTotalPrice(totalPrice);
        p.setTotalPriceExcludingTax(totalPriceExcludingTax);
        p.setTotalTax(totalTax);
        p.setTransactionId(payIn);
    }

    @Override
    public ConsumerPayInDto toConsumerDto(boolean includeDetails) {
        final ConsumerFreePayInDto p = new ConsumerFreePayInDto();

        this.updateDto(p);

        if (includeDetails) {
            this.items.stream().map(e -> e.toConsumerDto(includeDetails, false)).forEach(p::addItem);
        }

        return p;
    }

    @Override
    public ProviderPayInDto toProviderDto(boolean includeDetails) {
        final ProviderFreePayInDto p = new ProviderFreePayInDto();

        this.updateDto(p);

        p.setConsumer(consumer.getConsumer().toConsumerDto());

        if (includeDetails) {
            this.items.stream().map(e -> e.toProviderDto(includeDetails, false)).forEach(p::addItem);
        }

        return p;
    }

    @Override
    public HelpdeskPayInDto toHelpdeskDto(boolean includeDetails) {
        final HelpdeskFreePayInDto p = new HelpdeskFreePayInDto();

        this.updateDto(p);

        p.setConsumer(consumer.getConsumer().toDto());
        p.setProcessDefinition(processDefinition);
        p.setProcessInstance(processInstance);
        p.setProviderPayIn(payIn);
        p.setProviderResultCode(resultCode);
        p.setProviderResultMessage(resultMessage);

        if (includeDetails) {
            this.items.stream().map(e -> e.toHelpdeskDto(includeDetails, false)).forEach(p::addItem);
            this.statusHistory.stream().map(PayInStatusEntity::toDto).forEach(p::addStatusHistory);
        }

        return p;
    }

}
