package eu.opertusmundi.common.domain;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.NaturalId;

import eu.opertusmundi.common.model.payment.EnumTransactionStatus;
import eu.opertusmundi.common.model.payment.PayOutDto;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "PayOut")
@Table(schema = "billing", name = "`payout`", uniqueConstraints = {
    @UniqueConstraint(name = "uq_payout_key", columnNames = {"`key`"}),
    @UniqueConstraint(name = "uq_payout_bankwire_ref", columnNames = {"`bankwire_ref`"}),
})
public class PayOutEntity {

    @Id
    @Column(name = "`id`", updatable = false)
    @SequenceGenerator(sequenceName = "billing.payout_id_seq", name = "payout_id_seq", allocationSize = 1)
    @GeneratedValue(generator = "payout_id_seq", strategy = GenerationType.SEQUENCE)
    @Getter
    private Integer id;

    @NotNull
    @NaturalId
    @Column(name = "key", updatable = false, columnDefinition = "uuid")
    @Getter
    private final UUID key = UUID.randomUUID();

    /**
     * Identifier of the workflow definition used for processing this PayOut
     * record
     */
    @Column(name = "`process_definition`")
    @Getter
    @Setter
    protected String processDefinition;

    /**
     * Identifier of the workflow instance processing this PayOut record
     */
    @Column(name = "`process_instance`")
    @Getter
    @Setter
    protected String processInstance;

    @NotNull
    @ManyToOne(targetEntity = AccountEntity.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "provider", nullable = false)
    @Getter
    @Setter
    private AccountEntity provider;

    @OneToMany(
        mappedBy = "payout",
        fetch = FetchType.LAZY,
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    @Getter
    @Setter
    private List<PayOutStatusEntity> statusHistory = new ArrayList<>();

    /**
     * Reference to refund object
     */
    @ManyToOne(targetEntity = RefundEntity.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "refund")
    @Getter
    @Setter
    private RefundEntity refund;

    @Column(name = "`provider_payout`")
    @Getter
    @Setter
    private String payOut;

    @NotNull
    @Column(name = "`debited_funds`", columnDefinition = "numeric", precision = 20, scale = 6)
    @Getter
    @Setter
    private BigDecimal debitedFunds;

    @NotNull
    @Column(name = "`platform_fees`", columnDefinition = "numeric", precision = 20, scale = 6)
    @Getter
    @Setter
    private BigDecimal platformFees;

    @NotNull
    @Column(name = "`currency`")
    @Getter
    @Setter
    private String currency;

    @NotNull
    @Column(name = "`status`")
    @Enumerated(EnumType.STRING)
    @Getter
    @Setter
    private EnumTransactionStatus status;

    @NotNull
    @Column(name = "`status_updated_on`")
    @Getter
    @Setter
    protected ZonedDateTime statusUpdatedOn;

    @Column(name = "`created_on`")
    @Getter
    @Setter
    private ZonedDateTime createdOn;

    @Column(name = "`executed_on`")
    @Getter
    @Setter
    private ZonedDateTime executedOn;

    @NotNull
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "id",                      column = @Column(name = "`bank_account_provider_id`")),
        @AttributeOverride(name = "ownerName",               column = @Column(name = "`bank_account_owner_name`")),
        @AttributeOverride(name = "ownerAddress.line1",      column = @Column(name = "`bank_account_owner_address_line1`")),
        @AttributeOverride(name = "ownerAddress.line2",      column = @Column(name = "`bank_account_owner_address_line2`")),
        @AttributeOverride(name = "ownerAddress.city",       column = @Column(name = "`bank_account_owner_address_city`")),
        @AttributeOverride(name = "ownerAddress.region",     column = @Column(name = "`bank_account_owner_address_region`")),
        @AttributeOverride(name = "ownerAddress.postalCode", column = @Column(name = "`bank_account_owner_address_postal_code`")),
        @AttributeOverride(name = "ownerAddress.country",    column = @Column(name = "`bank_account_owner_address_country`")),
        @AttributeOverride(name = "iban",                    column = @Column(name = "`bank_account_iban`")),
        @AttributeOverride(name = "bic",                     column = @Column(name = "`bank_account_bic`")),
        @AttributeOverride(name = "tag",                     column = @Column(name = "`bank_account_tag`")),
    })
    @Getter
    @Setter
    private CustomerBankAccountEmbeddable bankAccount;

    @NotEmpty
    @Column(name = "`bankwire_ref`")
    @Getter
    @Setter
    private String bankwireRef;

    @Column(name = "`result_code`")
    @Getter
    @Setter
    protected String resultCode;

    @Column(name = "`result_message`")
    @Getter
    @Setter
    protected String resultMessage;

    public PayOutDto toDto() {
        return this.toDto(false);
    }

    public PayOutDto toDto(boolean includeHelpdeskData) {
        final PayOutDto p = new PayOutDto();

        p.setBankAccount(this.bankAccount.toDto(includeHelpdeskData));
        p.setBankwireRef(bankwireRef);
        p.setCreatedOn(createdOn);
        p.setCurrency(currency);
        p.setDebitedFunds(debitedFunds);
        p.setExecutedOn(executedOn);
        p.setFees(platformFees);
        p.setId(id);
        p.setKey(key);
        p.setStatus(status);
        p.setStatusUpdatedOn(statusUpdatedOn);

        if (this.getRefund() != null) {
            p.setRefund(this.getRefund().toDto(includeHelpdeskData));
        }

        if (includeHelpdeskData) {
            p.setProvider(this.provider.getProvider().toDto());

            p.setProcessDefinition(processDefinition);
            p.setProcessInstance(processInstance);

            p.setProviderResultCode(resultCode);
            p.setProviderResultMessage(resultMessage);
            p.setTransactionId(payOut);
        }

        return p;
    }

}
