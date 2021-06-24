package eu.opertusmundi.common.domain;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.NaturalId;

import eu.opertusmundi.common.model.payment.EnumPaymentItemType;
import eu.opertusmundi.common.model.payment.EnumTransactionStatus;
import eu.opertusmundi.common.model.payment.PayInItemDto;
import eu.opertusmundi.common.model.payment.PaymentException;
import eu.opertusmundi.common.model.payment.TransferDto;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "PayInItem")
@Table(schema = "billing", name = "`payin_item`")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "`type`", discriminatorType = DiscriminatorType.STRING)
public abstract class PayInItemEntity {

    @Id
    @Column(name = "`id`", updatable = false)
    @SequenceGenerator(sequenceName = "billing.payin_item_id_seq", name = "payin_item_id_seq", allocationSize = 1)
    @GeneratedValue(generator = "payin_item_id_seq", strategy = GenerationType.SEQUENCE)
    @Getter
    protected Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payin", nullable = false)
    @Getter
    @Setter
    protected PayInEntity payin;

    /**
     * Reference to the provider account for the specific item
     */
    @NotNull
    @ManyToOne(targetEntity = AccountEntity.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "provider", nullable = false)
    @Getter
    @Setter
    protected AccountEntity provider;
    
    @NotNull
    @Column(name = "`index`")
    @Getter
    @Setter
    protected Integer index;

    @NotNull
    @Column(name = "`type`", insertable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    @Getter
    protected EnumPaymentItemType type;

    @Column(name = "`transfer`")
    @Getter
    @Setter
    protected String transfer;

    @NotNull
    @NaturalId
    @Column(name = "transfer_key", updatable = false, columnDefinition = "uuid")
    @Getter
    private final UUID transferKey = UUID.randomUUID();

    @Column(name = "`transfer_credited_funds`", columnDefinition = "numeric", precision = 20, scale = 6)
    @Getter
    @Setter
    protected BigDecimal transferCreditedFunds;

    @Column(name = "`transfer_platform_fees`", columnDefinition = "numeric", precision = 20, scale = 6)
    @Getter
    @Setter
    protected BigDecimal transferFees;

    @Column(name = "`transfer_status`")
    @Enumerated(EnumType.STRING)
    @Getter
    @Setter
    protected EnumTransactionStatus transferStatus;

    @Column(name = "`transfer_created_on`")
    @Getter
    @Setter
    protected ZonedDateTime transferCreatedOn;

    @Column(name = "`transfer_executed_on`")
    @Getter
    @Setter
    protected ZonedDateTime transferExecutedOn;

    @Column(name = "`transfer_result_code`")
    @Getter
    @Setter
    protected String transferResultCode;

    @Column(name = "`transfer_result_message`")
    @Getter
    @Setter
    protected String transferResultMessage;

    public void updateTransfer(TransferDto t) {
        this.transfer              = t.getId();
        this.transferCreatedOn     = t.getCreatedOn();
        this.transferCreditedFunds = t.getCreditedFunds();
        this.transferExecutedOn    = t.getExecutedOn();
        this.transferFees          = t.getFees();
        this.transferResultCode    = t.getResultCode();
        this.transferResultMessage = t.getResultMessage();
        this.transferStatus        = t.getStatus();
    }

    public PayInItemDto toDto() throws PaymentException {
        return this.toDto(false);
    }
    public abstract PayInItemDto toDto(boolean includeHelpdeskData);

    public TransferDto toTransferDto() {
        final TransferDto t = new TransferDto();

        if (StringUtils.isBlank(transfer)) {
            return null;
        }

        t.setCreatedOn(transferCreatedOn);
        t.setCreditedFunds(transferCreditedFunds);
        t.setExecutedOn(transferExecutedOn);
        t.setFees(transferFees);
        t.setId(transfer);
        t.setKey(transferKey);
        t.setResultCode(transferResultCode);
        t.setResultMessage(transferResultMessage);
        t.setStatus(transferStatus);

        return t;
    }

}