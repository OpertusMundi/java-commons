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

import eu.opertusmundi.common.model.payment.EnumTransactionNature;
import eu.opertusmundi.common.model.payment.EnumTransactionStatus;
import eu.opertusmundi.common.model.payment.EnumTransactionType;
import eu.opertusmundi.common.model.payment.TransferDto;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "Transfer")
@Table(schema = "billing", name = "`transfer`", uniqueConstraints = {
    @UniqueConstraint(name = "uq_transfer_key", columnNames = {"`key`"}),
    @UniqueConstraint(name = "uq_transfer_transaction_id", columnNames = {"`transaction_id`"}),
})
@Getter
@Setter
public class TransferEntity {

    @Id
    @Column(name = "`id`", updatable = false)
    @SequenceGenerator(sequenceName = "billing.transfer_id_seq", name = "transfer_id_seq", allocationSize = 1)
    @GeneratedValue(generator = "transfer_id_seq", strategy = GenerationType.SEQUENCE)
    @Setter(AccessLevel.PRIVATE)
    private Integer id;

    @NotNull
    @NaturalId
    @Column(name = "key", updatable = false, columnDefinition = "uuid")
    @Setter(AccessLevel.PRIVATE)
    private UUID key = UUID.randomUUID();

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

    @ManyToOne(targetEntity = RefundEntity.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "refund")
    @Getter
    @Setter
    protected RefundEntity refund;

    public TransferDto toDto() {
        return this.toDto(false);
    }

    public TransferDto toDto(boolean includeHelpdeskDetails) {
        final TransferDto t = new TransferDto();

        if (includeHelpdeskDetails) {
            t.setAuthorId(authorId);
            t.setCreditedUserId(creditedUserId);
            t.setCreditedWalletId(creditedWalletId);
            t.setDebitedWalletId(debitedWalletId);
            t.setId(id);
            t.setTransactionId(transactionId);
        }

        t.setCreationDate(creationDate);
        t.setCreditedFunds(creditedFunds);
        t.setCurrency(currency);
        t.setDebitedFunds(debitedFunds);
        t.setExecutionDate(executionDate);
        t.setFees(fees);
        t.setKey(key);
        t.setResultCode(resultCode);
        t.setResultMessage(resultMessage);
        t.setTransactionNature(transactionNature);
        t.setTransactionStatus(transactionStatus);

        if (this.getRefund() != null) {
            t.setRefund(this.getRefund().toDto(includeHelpdeskDetails));
        }

        return t;
    }

}
