package eu.opertusmundi.common.domain;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
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

import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.NaturalId;

import eu.opertusmundi.common.model.payment.EnumTransactionStatus;
import eu.opertusmundi.common.model.payment.PayOutDto;
import eu.opertusmundi.common.model.payment.RefundDto;
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

    @Column(name = "`provider_refund`")
    @Getter
    @Setter
    protected String refund;

    @Column(name = "`provider_refund_created_on`")
    @Getter
    @Setter
    private ZonedDateTime refundCreatedOn;

    @Column(name = "`provider_refund_executed_on`")
    @Getter
    @Setter
    private ZonedDateTime refundExecutedOn;

    @Column(name = "`provider_refund_status`")
    @Enumerated(EnumType.STRING)
    @Getter
    @Setter
    protected EnumTransactionStatus refundStatus;

    @Column(name = "`provider_refund_reason_type`")
    @Getter
    @Setter
    protected String refundReasonType;

    @Column(name = "`provider_refund_reason_message`")
    @Getter
    @Setter
    protected String refundReasonMessage;

    public PayOutDto toDto() {
        return this.toDto(false);
    }

    public PayOutDto toDto(boolean includeHelpdeskData) {
        final PayOutDto p = new PayOutDto();

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

        if(!StringUtils.isBlank(refund)) {
            final RefundDto r = new RefundDto();

            if (includeHelpdeskData) {
                r.setRefund(refund);
            }
            r.setRefundCreatedOn(refundCreatedOn);
            r.setRefundExecutedOn(refundExecutedOn);
            r.setRefundReasonMessage(refundReasonMessage);
            r.setRefundReasonType(refundReasonType);
            r.setRefundStatus(refundStatus);

            p.setRefund(r);
        }

        if (includeHelpdeskData) {
            p.setProvider(this.provider.getProvider().toDto());

            p.setProcessDefinition(processDefinition);
            p.setProcessInstance(processInstance);

            p.setProviderPayOut(payOut);
            p.setProviderResultCode(resultCode);
            p.setProviderResultMessage(resultMessage);
        }

        return p;
    }

}
