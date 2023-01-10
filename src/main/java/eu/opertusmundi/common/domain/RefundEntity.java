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

import eu.opertusmundi.common.model.payment.EnumRefundReasonType;
import eu.opertusmundi.common.model.payment.EnumTransactionNature;
import eu.opertusmundi.common.model.payment.EnumTransactionStatus;
import eu.opertusmundi.common.model.payment.EnumTransactionType;
import eu.opertusmundi.common.model.payment.RefundDto;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "Refund")
@Table(schema = "billing", name = "`refund`", uniqueConstraints = {
    @UniqueConstraint(name = "uq_refund_key", columnNames = {"`key`"}),
})
@Getter
@Setter
public class RefundEntity {

    @Id
    @Column(name = "`id`", updatable = false)
    @SequenceGenerator(sequenceName = "billing.refund_id_seq", name = "refund_id_seq", allocationSize = 1)
    @GeneratedValue(generator = "refund_id_seq", strategy = GenerationType.SEQUENCE)
    @Setter(AccessLevel.PRIVATE)
    private Integer id;

    @NotNull
    @NaturalId
    @Column(name = "key", updatable = false, columnDefinition = "uuid")
    @Setter(AccessLevel.PRIVATE)
    private UUID key = UUID.randomUUID();

    @Column(name = "`reference_number`")
    private String referenceNumber;
    
    @ManyToOne(targetEntity = HelpdeskAccountEntity.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "initiator")
    private HelpdeskAccountEntity initiator;

    @ManyToOne(targetEntity = AccountEntity.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "consumer")
    private AccountEntity consumer;

    @ManyToOne(targetEntity = AccountEntity.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "provider")
    private AccountEntity provider;

    @NotNull
    @Column(name = "`debited_funds`", columnDefinition = "numeric", precision = 20, scale = 6)
    private BigDecimal debitedFunds;

    @NotNull
    @Column(name = "`credited_funds`", columnDefinition = "numeric", precision = 20, scale = 6)
    private BigDecimal creditedFunds;

    @NotNull
    @Column(name = "`fees`", columnDefinition = "numeric", precision = 20, scale = 6)
    private BigDecimal fees;

    @NotNull
    @Column(name = "`currency`")
    private String currency;

    @Column(name = "`debited_wallet_id`")
    private String debitedWalletId;

    @Column(name = "`credited_wallet_id`")
    private String creditedWalletId;

    @Column(name = "`author_id`")
    private String authorId;

    @Column(name = "`credited_user_id`")
    private String creditedUserId;

    @NotNull
    @Column(name = "`creation_date`")
    private ZonedDateTime creationDate;

    @NotNull
    @Column(name = "`execution_date`")
    private ZonedDateTime executionDate;

    @Column(name = "`result_code`")
    private String resultCode;

    @Column(name = "`result_message`")
    private String resultMessage;

    @NotNull
    @Column(name = "`transaction_id`")
    private String transactionId;

    @NotNull
    @Column(name = "`transaction_status`")
    @Enumerated(EnumType.STRING)
    @Getter
    @Setter
    private EnumTransactionStatus transactionStatus;

    @NotNull
    @Column(name = "`transaction_nature`")
    @Enumerated(EnumType.STRING)
    private EnumTransactionNature transactionNature;

    @NotNull
    @Column(name = "`transaction_type`")
    @Enumerated(EnumType.STRING)
    private EnumTransactionType transactionType;

    @NotNull
    @Column(name = "`initial_transaction_id`")
    private String initialTransactionId;

    @NotNull
    @Column(name = "`initial_transaction_type`")
    @Enumerated(EnumType.STRING)
    private EnumTransactionType initialTransactionType;

    @NotNull
    @Column(name = "`refund_reason_type`")
    @Enumerated(EnumType.STRING)
    private EnumRefundReasonType refundReasonType;

    @Column(name = "`refund_reason_message`")
    private String refundReasonMessage;

    public RefundDto toDto() {
        return this.toDto(false);
    }

    public RefundDto toDto(boolean includeHelpdeskDetails) {
        final RefundDto r = new RefundDto();

        if (includeHelpdeskDetails) {
            r.setAuthorId(authorId);
            r.setCreditedUserId(creditedUserId);
            r.setCreditedWalletId(creditedWalletId);
            r.setDebitedWalletId(debitedWalletId);
            r.setId(id);
            r.setInitialTransactionId(initialTransactionId);
            r.setTransactionId(transactionId);
        }

        r.setCreationDate(creationDate);
        r.setCreditedFunds(creditedFunds);
        r.setCurrency(currency);
        r.setDebitedFunds(debitedFunds);
        r.setExecutionDate(executionDate);
        r.setFees(fees);
        r.setInitialTransactionType(initialTransactionType);
        r.setKey(key);
        r.setRefundReasonMessage(refundReasonMessage);
        r.setRefundReasonType(refundReasonType);
        r.setResultCode(resultCode);
        r.setResultMessage(resultMessage);
        r.setTransactionNature(transactionNature);
        r.setTransactionStatus(transactionStatus);
        r.setTransactionType(initialTransactionType);

        return r;
    }

}
