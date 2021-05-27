package eu.opertusmundi.common.domain;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotEmpty;

import org.hibernate.validator.constraints.Length;

import eu.opertusmundi.common.model.payment.CardDirectPayInDto;
import eu.opertusmundi.common.model.payment.EnumPaymentMethod;
import eu.opertusmundi.common.model.payment.PayInDto;
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

    @Override
    public PayInDto toDto(boolean includeDetails) {
        final CardDirectPayInDto p = new CardDirectPayInDto();

        p.setAlias(alias);
        p.setCard(card);
        p.setCreatedOn(createdOn);
        p.setCurrency(currency);
        p.setExecutedOn(executedOn);
        p.setId(id);
        p.setKey(key);
        p.setPayIn(payIn);
        p.setPaymentMethod(paymentMethod);
        p.setProcessDefinition(processDefinition);
        p.setProcessInstance(processInstance);
        p.setReferenceNumber(referenceNumber);
        p.setStatementDescriptor(statementDescriptor);
        p.setStatus(status);
        p.setStatusUpdatedOn(statusUpdatedOn);
        p.setTotalPrice(totalPrice);
        p.setTotalPriceExcludingTax(totalPriceExcludingTax);
        p.setTotalTax(totalTax);

        if (includeDetails) {
            this.items.stream().map(PayInItemEntity::toDto).forEach(p::addItem);
        }
        return p;
    }

}
