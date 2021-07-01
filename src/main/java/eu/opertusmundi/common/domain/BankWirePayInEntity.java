package eu.opertusmundi.common.domain;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import eu.opertusmundi.common.model.payment.BankwirePayInDto;
import eu.opertusmundi.common.model.payment.EnumPaymentMethod;
import eu.opertusmundi.common.model.payment.PayInDto;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "BankWirePayIn")
@Table(schema = "billing", name = "`payin_bankwire`")
@DiscriminatorValue(value = "BANKWIRE")
public class BankWirePayInEntity extends PayInEntity {

    public BankWirePayInEntity() {
        super(EnumPaymentMethod.BANKWIRE);
    }

    @NotEmpty
    @Column(name ="`wire_reference`")
    @Getter
    @Setter
    private String wireReference;

    @NotNull
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "ownerName",               column = @Column(name = "`bank_account_owner_name`",                nullable = false)),
        @AttributeOverride(name = "ownerAddress.line1",      column = @Column(name = "`bank_account_owner_address_line1`",       nullable = false)),
        @AttributeOverride(name = "ownerAddress.line2",      column = @Column(name = "`bank_account_owner_address_line2`")),
        @AttributeOverride(name = "ownerAddress.city",       column = @Column(name = "`bank_account_owner_address_city`",        nullable = false)),
        @AttributeOverride(name = "ownerAddress.region",     column = @Column(name = "`bank_account_owner_address_region`")),
        @AttributeOverride(name = "ownerAddress.postalCode", column = @Column(name = "`bank_account_owner_address_postal_code`", nullable = false)),
        @AttributeOverride(name = "ownerAddress.country",    column = @Column(name = "`bank_account_owner_address_country`",     nullable = false)),
        @AttributeOverride(name = "iban",                    column = @Column(name = "`bank_account_iban`",                      nullable = false)),
        @AttributeOverride(name = "bic",                     column = @Column(name = "`bank_account_bic`",                       nullable = false)),
    })
    @Getter
    @Setter
    private BankAccountEmbeddable bankAccount;

    @Override
    public PayInDto toDto(boolean includeDetails, boolean includeHelpdeskData) {
        final BankwirePayInDto p = new BankwirePayInDto();

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
        p.setWireReference(wireReference);

        if (includeDetails) {
            p.setBankAccount(bankAccount.toDto());

            this.items.stream().map(e -> e.toDto(includeDetails, includeHelpdeskData, includeHelpdeskData)).forEach(p::addItem);
        }
        if (includeHelpdeskData) {
            p.setConsumer(consumer.getProfile().getConsumer().toDto());
            p.setProcessDefinition(processDefinition);
            p.setProcessInstance(processInstance);
            p.setProviderPayIn(payIn);
            p.setProviderResultCode(resultCode);
            p.setProviderResultMessage(resultMessage);
        }

        return p;
    }

}
