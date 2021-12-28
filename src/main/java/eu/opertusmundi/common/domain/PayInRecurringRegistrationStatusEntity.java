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

import eu.opertusmundi.common.model.payment.EnumRecurringPaymentStatus;
import eu.opertusmundi.common.model.payment.PayInRecurringRegistrationStatusDto;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "PayInRecurringRegistrationStatus")
@Table(schema = "billing", name = "`payin_recurring_registration_status_hist`")
public class PayInRecurringRegistrationStatusEntity {

    @Id
    @Column(name = "`id`", updatable = false)
    @SequenceGenerator(sequenceName = "billing.payin_recurring_registration_status_hist_id_seq", name = "payin_recurring_registration_status_hist_id_seq", allocationSize = 1)
    @GeneratedValue(generator = "payin_recurring_registration_status_hist_id_seq", strategy = GenerationType.SEQUENCE)
    @Getter
    private Integer id;

    @NotNull
    @ManyToOne(targetEntity = PayInEntity.class)
    @JoinColumn(name = "registration", nullable = false)
    @Getter
    @Setter
    private PayInRecurringRegistrationEntity registration;

    @NotNull
    @Column(name = "`status`")
    @Enumerated(EnumType.STRING)
    @Getter
    @Setter
    private EnumRecurringPaymentStatus status;

    @NotNull
    @Column(name = "`status_updated_on`")
    @Getter
    @Setter
    private ZonedDateTime statusUpdatedOn;

    public PayInRecurringRegistrationStatusDto toDto() {
        final PayInRecurringRegistrationStatusDto s = new PayInRecurringRegistrationStatusDto();

        s.setId(id);
        s.setRegistration(registration.getId());
        s.setStatus(status);
        s.setUpdatedOn(statusUpdatedOn);

        return s;
    }

}
