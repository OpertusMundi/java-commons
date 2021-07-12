package eu.opertusmundi.common.domain;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotEmpty;

import org.hibernate.validator.constraints.Length;

import eu.opertusmundi.common.model.payment.EnumPaymentMethod;
import eu.opertusmundi.common.model.payment.PayInDto;
import eu.opertusmundi.common.model.payment.consumer.ConsumerCardDirectPayInDto;
import eu.opertusmundi.common.model.payment.consumer.ConsumerPayInDto;
import eu.opertusmundi.common.model.payment.helpdesk.HelpdeskCardDirectPayInDto;
import eu.opertusmundi.common.model.payment.helpdesk.HelpdeskPayInDto;
import eu.opertusmundi.common.model.payment.provider.ProviderCardDirectPayInDto;
import eu.opertusmundi.common.model.payment.provider.ProviderPayInDto;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "CardDirectPayIn")
@Table(schema = "billing", name = "`payin_card_direct`")
@DiscriminatorValue(value = "CARD_DIRECT")
public class CardDirectPayInEntity extends PayInEntity {

    public CardDirectPayInEntity() {
        super(EnumPaymentMethod.CARD_DIRECT);
    }

    @Column(name = "`alias`")
    @Getter
    @Setter
    private String alias;

    @NotEmpty
    @Column(name = "`card`")
    @Getter
    @Setter
    private String card;

    @Length(max = 10)
    @NotEmpty
    @Column(name = "`statement_descriptor`", length = 10)
    @Getter
    @Setter
    private String statementDescriptor;

    public void updateDto(PayInDto p) {
        p.setCreatedOn(createdOn);
        p.setCurrency(currency);
        p.setExecutedOn(executedOn);
        p.setId(id);
        p.setKey(key);
        p.setPayIn(payIn);
        p.setPaymentMethod(paymentMethod);
        p.setReferenceNumber(referenceNumber);
        p.setStatus(status);
        p.setStatusUpdatedOn(statusUpdatedOn);
        p.setTotalPrice(totalPrice);
        p.setTotalPriceExcludingTax(totalPriceExcludingTax);
        p.setTotalTax(totalTax);
    }

    @Override
    public ConsumerPayInDto toConsumerDto(boolean includeDetails) {
        final ConsumerCardDirectPayInDto p = new ConsumerCardDirectPayInDto();

        this.updateDto(p);

        p.setAlias(alias);
        p.setCard(card);
        p.setStatementDescriptor(statementDescriptor);

        if (includeDetails) {
            this.items.stream().map(i -> i.toConsumerDto(includeDetails)).forEach(p::addItem);
        }

        return p;
    }

    @Override
    public ProviderPayInDto toProviderDto(boolean includeDetails) {
        final ProviderCardDirectPayInDto p = new ProviderCardDirectPayInDto();

        this.updateDto(p);
        
        p.setAlias(alias);
        p.setCard(card);
        p.setConsumer(consumer.getConsumer().toConsumerDto());
        p.setStatementDescriptor(statementDescriptor);

        if (includeDetails) {
            this.items.stream().map(e -> e.toProviderDto(includeDetails)).forEach(p::addItem);
        }

        return p;
    }

    @Override
    public HelpdeskPayInDto toHelpdeskDto(boolean includeDetails) {
        final HelpdeskCardDirectPayInDto p = new HelpdeskCardDirectPayInDto();

        this.updateDto(p);
        
        p.setAlias(alias);
        p.setCard(card);
        p.setConsumer(consumer.getProfile().getConsumer().toDto());
        p.setProcessDefinition(processDefinition);
        p.setProcessInstance(processInstance);
        p.setProviderPayIn(payIn);
        p.setProviderResultCode(resultCode);
        p.setProviderResultMessage(resultMessage);
        p.setStatementDescriptor(statementDescriptor);

        if (includeDetails) {
            this.items.stream().map(e -> e.toHelpdeskDto(includeDetails)).forEach(p::addItem);
        }

        return p;
    }

}
