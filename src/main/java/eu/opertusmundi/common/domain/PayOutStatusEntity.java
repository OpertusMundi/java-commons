package eu.opertusmundi.common.domain;

import java.time.ZonedDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import eu.opertusmundi.common.model.payment.EnumTransactionStatus;
import eu.opertusmundi.common.model.payment.PayOutStatusDto;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "PayOutStatus")
@Table(schema = "billing", name = "`payout_status_hist`")
public class PayOutStatusEntity {

    @Id
    @Column(name = "`id`", updatable = false)
    @SequenceGenerator(sequenceName = "billing.payout_status_hist_id_seq", name = "payout_status_hist_id_seq", allocationSize = 1)
    @GeneratedValue(generator = "payout_status_hist_id_seq", strategy = GenerationType.SEQUENCE)
    @Getter
    private Integer id;

    @NotNull
    @ManyToOne(targetEntity = PayOutEntity.class)
    @JoinColumn(name = "payout", nullable = false)
    @Getter
    @Setter
    private PayOutEntity payout;

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
    private ZonedDateTime statusUpdatedOn;

    public PayOutStatusDto toDto() {
        final PayOutStatusDto s = new PayOutStatusDto();

        s.setId(id);
        s.setPayout(payout.getId());
        s.setStatus(status);
        s.setUpdatedOn(statusUpdatedOn);

        return s;
    }

}
