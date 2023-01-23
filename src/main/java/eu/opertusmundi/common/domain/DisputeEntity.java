package eu.opertusmundi.common.domain;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

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
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.NaturalId;

import eu.opertusmundi.common.model.payment.DisputeDto;
import eu.opertusmundi.common.model.payment.EnumDisputeReasonType;
import eu.opertusmundi.common.model.payment.EnumDisputeStatus;
import eu.opertusmundi.common.model.payment.EnumDisputeType;
import eu.opertusmundi.common.model.payment.EnumTransactionType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "Dispute")
@Table(schema = "billing", name = "`dispute`", uniqueConstraints = {
    @UniqueConstraint(name = "uq_dispute_key", columnNames = {"`key`"}),
})
@Getter
@Setter
public class DisputeEntity {

    @Id
    @Column(name = "`id`", updatable = false)
    @SequenceGenerator(sequenceName = "billing.dispute_id_seq", name = "dispute_id_seq", allocationSize = 1)
    @GeneratedValue(generator = "dispute_id_seq", strategy = GenerationType.SEQUENCE)
    @Setter(AccessLevel.PRIVATE)
    private Integer id;

    @NotNull
    @NaturalId
    @Column(name = "key", updatable = false, columnDefinition = "uuid")
    @Setter(AccessLevel.PRIVATE)
    private UUID key = UUID.randomUUID();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payin")
    private PayInEntity payin;

    @NotNull
    @Column(name = "`creation_date`")
    private ZonedDateTime creationDate;

    @NotNull
    @Column(name = "`transaction_id`")
    private String transactionId;

    @NotNull
    @Column(name = "`repudiation_id`")
    private String repudiationId;

    @NotNull
    @Column(name = "`type`")
    @Enumerated(EnumType.STRING)
    private EnumDisputeType type;

    @NotNull
    @Column(name = "`status`")
    @Enumerated(EnumType.STRING)
    private EnumDisputeStatus status;

    @Column(name = "`status_message`")
    private String statusMessage;

    @Column(name = "`contest_deadline_date`")
    private ZonedDateTime contestDeadlineDate;

    @NotNull
    @Column(name = "`disputed_funds`", columnDefinition = "numeric", precision = 20, scale = 6)
    private BigDecimal disputedFunds;

    @NotNull
    @Column(name = "`contested_funds`", columnDefinition = "numeric", precision = 20, scale = 6)
    private BigDecimal contestedFunds;

    @NotNull
    @Column(name = "`reason_type`")
    @Enumerated(EnumType.STRING)
    private EnumDisputeReasonType reasonType;

    @Column(name = "`reason_message`")
    private String reasonMessage;

    @Column(name = "`result_code`")
    private String resultCode;

    @Column(name = "`result_message`")
    private String resultMessage;

    @NotNull
    @Column(name = "`initial_transaction_id`")
    private String initialTransactionId;

    @NotNull
    @Column(name = "`initial_transaction_key`")
    private UUID initialTransactionKey;

    @Column(name = "`initial_transaction_ref_number`")
    private String initialTransactionRefNumber;

    @NotNull
    @Column(name = "`initial_transaction_type`")
    @Enumerated(EnumType.STRING)
    private EnumTransactionType initialTransactionType;

    public DisputeDto toDto() {
        return this.toDto(false);
    }

    public DisputeDto toDto(boolean includeHelpdeskDetails) {
        final DisputeDto d = new DisputeDto();

        d.setContestDeadlineDate(contestDeadlineDate);
        d.setContestedFunds(contestedFunds);
        d.setCreationDate(creationDate);
        d.setDisputedFunds(disputedFunds);
        d.setId(id);
        d.setInitialTransactionKey(initialTransactionKey);
        d.setInitialTransactionRefNumber(initialTransactionRefNumber);
        d.setInitialTransactionType(initialTransactionType);
        d.setKey(key);
        d.setReasonMessage(reasonMessage);
        d.setReasonType(reasonType);
        d.setStatus(status);
        d.setStatusMessage(statusMessage);
        d.setType(type);

        if (includeHelpdeskDetails) {
            d.setInitialTransactionId(initialTransactionId);
            d.setPayin(this.getPayin().toHelpdeskDto(includeHelpdeskDetails));
            d.setRepudiationId(repudiationId);
            d.setResultCode(resultCode);
            d.setResultMessage(resultMessage);
            d.setTransactionId(transactionId);
        } else {
            d.setPayin(this.getPayin().toConsumerDto(includeHelpdeskDetails));
        }

        return d;
    }

}
