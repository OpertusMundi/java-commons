package eu.opertusmundi.common.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.UUID;

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
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.NaturalId;

import eu.opertusmundi.common.model.payment.EnumServiceBillingBatchStatus;
import eu.opertusmundi.common.model.payment.ServiceBillingBatchDto;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity(name = "ServiceBillingBatch")
@Table(schema = "billing", name = "`service_billing_batch`")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class ServiceBillingBatchEntity {

    @Id
    @Column(name = "`id`", updatable = false)
    @SequenceGenerator(sequenceName = "billing.service_billing_batch_id_seq", name = "service_billing_batch_id_seq", allocationSize = 1)
    @GeneratedValue(generator = "service_billing_batch_id_seq", strategy = GenerationType.SEQUENCE)
    @Setter(AccessLevel.PRIVATE)
    private Integer id;

    @NotNull
    @NaturalId
    @Column(name = "key", updatable = false, columnDefinition = "uuid")
    @Getter
    private final UUID key = UUID.randomUUID();

    @ManyToOne(targetEntity = HelpdeskAccountEntity.class)
    @JoinColumn(name = "created_by")
    private HelpdeskAccountEntity createdBy;

    @NotNull
    @Column(name = "`created_on`")
    private ZonedDateTime createdOn;

    @NotNull
    @Column(name = "`updated_on`")
    private ZonedDateTime updatedOn;

    @NotNull
    @Column(name = "`status`")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private EnumServiceBillingBatchStatus status = EnumServiceBillingBatchStatus.RUNNING;

    @NotNull
    @Column(name = "`from_date`")
    private LocalDate fromDate;

    @NotNull
    @Column(name = "`to_date`")
    private LocalDate toDate;

    @NotNull
    @Column(name = "`due_date`")
    private LocalDate dueDate;

    @Column(name = "`total_subscriptions`")
    private Integer totalSubscriptions;

    @Column(name = "`total_price`", columnDefinition = "numeric", precision = 20, scale = 6)
    private BigDecimal totalPrice;

    @Column(name = "`total_price_excluding_tax`", columnDefinition = "numeric", precision = 20, scale = 6)
    private BigDecimal totalPriceExcludingTax;

    @Column(name = "`total_tax`", columnDefinition = "numeric", precision = 20, scale = 6)
    private BigDecimal totalTax;

    @Column(name = "`process_definition`")
    private String processDefinition;

    @Column(name = "`process_instance`")
    private String processInstance;

    @Transient
    public boolean isWorkflowInitialized() {
        return !StringUtils.isBlank(this.processInstance);
    }

    public ServiceBillingBatchDto toDto() {
        final ServiceBillingBatchDto s = new ServiceBillingBatchDto();

        s.setCreatedBy(this.createdBy.toSimpleDto());
        s.setCreatedOn(createdOn);
        s.setDueDate(dueDate);
        s.setFromDate(fromDate);
        s.setId(id);
        s.setKey(key);
        s.setProcessDefinition(processDefinition);
        s.setProcessInstance(processInstance);
        s.setStatus(status);
        s.setToDate(toDate);
        s.setTotalPrice(totalPrice);
        s.setTotalPriceExcludingTax(totalPriceExcludingTax);
        s.setTotalSubscriptions(totalSubscriptions);
        s.setTotalTax(totalTax);
        s.setUpdatedOn(updatedOn);

        return s;
    }


}