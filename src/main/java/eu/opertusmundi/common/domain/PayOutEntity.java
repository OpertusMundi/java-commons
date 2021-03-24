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
    
    @NotNull
    @ManyToOne(targetEntity = AccountEntity.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "account", nullable = false)
    @Getter
    @Setter
    private AccountEntity account;
    
    @Column(name = "`provider_payout`")
    @Getter
    @Setter
    private String payOut;
    
    @NotNull
    @Column(name = "`credited_funds`", columnDefinition = "numeric", precision = 20, scale = 6)
    @Getter
    @Setter
    private BigDecimal creditedFunds;
    
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
    @Column(name = "`created_on`")
    @Getter
    @Setter
    private ZonedDateTime createdOn;

    @NotNull
    @Column(name = "`executed_on`")
    @Getter
    @Setter
    private ZonedDateTime executedOn;

    @NotEmpty
    @Column(name = "`bankwire_ref`")
    @Getter
    @Setter
    private String bankwireRef;

    public PayOutDto toDto() {
        final PayOutDto p = new PayOutDto();

        p.setBankwireRef(bankwireRef);
        p.setCreatedOn(createdOn);
        p.setCreditedFunds(creditedFunds);
        p.setCurrency(currency);
        p.setExecutedOn(executedOn);
        p.setFees(platformFees);
        p.setId(id);
        p.setKey(key);
        p.setPayOut(payOut);
        p.setStatus(status);

        return p;
    }

}
