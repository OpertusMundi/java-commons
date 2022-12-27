package eu.opertusmundi.common.domain;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.persistence.CascadeType;
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
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.NaturalId;

import eu.opertusmundi.common.model.payment.EnumPaymentMethod;
import eu.opertusmundi.common.model.payment.EnumTransactionStatus;
import eu.opertusmundi.common.model.payment.consumer.ConsumerPayInDto;
import eu.opertusmundi.common.model.payment.helpdesk.HelpdeskPayInDto;
import eu.opertusmundi.common.model.payment.provider.ProviderPayInDto;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "PayIn")
@Table(schema = "billing", name = "`payin`", uniqueConstraints = {
    @UniqueConstraint(name = "uq_payin_key", columnNames = {"`key`"}),
    @UniqueConstraint(name = "uq_payin_reference_number", columnNames = {"`reference_number`"}),
})
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "`payment_method`", discriminatorType = DiscriminatorType.STRING)
public abstract class PayInEntity {

    protected PayInEntity() {
    }

    protected PayInEntity(EnumPaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    @Id
    @Column(name = "`id`", updatable = false)
    @SequenceGenerator(sequenceName = "billing.payin_id_seq", name = "payin_id_seq", allocationSize = 1)
    @GeneratedValue(generator = "payin_id_seq", strategy = GenerationType.SEQUENCE)
    @Getter
    protected Integer id;

    @NotNull
    @NaturalId
    @Column(name = "`key`", updatable = false, columnDefinition = "uuid")
    @Getter
    @Setter
    protected UUID key;

    /**
     * Identifier of the workflow definition used for processing this PayIn
     * record
     */
    @Column(name = "`process_definition`")
    @Getter
    @Setter
    protected String processDefinition;

    /**
     * Identifier of the workflow instance processing this PayIn record
     */
    @Column(name = "`process_instance`")
    @Getter
    @Setter
    protected String processInstance;

    /**
     * Reference to the consumer account that created the PayIn
     */
    @NotNull
    @ManyToOne(targetEntity = AccountEntity.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "consumer", nullable = false)
    @Getter
    @Setter
    protected AccountEntity consumer;

    /**
     * Collection of items paid with this PayIn. An PayIn may contain either a
     * single order or multiple subscription billing records
     */
    @OneToMany(
        mappedBy = "payin",
        fetch = FetchType.EAGER,
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    @Getter
    @Setter
    protected List<PayInItemEntity> items = new ArrayList<>();

    @OneToMany(
        mappedBy = "payin",
        fetch = FetchType.LAZY,
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    @Getter
    @Setter
    protected List<PayInStatusEntity> statusHistory = new ArrayList<>();

    @NotNull
    @Column(name = "`total_price`", columnDefinition = "numeric", precision = 20, scale = 6)
    @Getter
    @Setter
    protected BigDecimal totalPrice;

    @NotNull
    @Column(name = "`total_price_excluding_tax`", columnDefinition = "numeric", precision = 20, scale = 6)
    @Getter
    @Setter
    protected BigDecimal totalPriceExcludingTax;

    @NotNull
    @Column(name = "`total_tax`", columnDefinition = "numeric", precision = 20, scale = 6)
    @Getter
    @Setter
    protected BigDecimal totalTax;

    @NotNull
    @Column(name = "`currency`")
    @Getter
    @Setter
    protected String currency;

    @NotNull
    @Column(name = "`created_on`")
    @Getter
    @Setter
    protected ZonedDateTime createdOn;

    @Column(name = "`executed_on`")
    @Getter
    @Setter
    protected ZonedDateTime executedOn;

    @NotNull
    @Column(name = "`status`")
    @Enumerated(EnumType.STRING)
    @Getter
    @Setter
    protected EnumTransactionStatus status;

    @NotNull
    @Column(name = "`status_updated_on`")
    @Getter
    @Setter
    protected ZonedDateTime statusUpdatedOn;

    @NotNull
    @Column(name = "`payment_method`", nullable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    @Getter
    protected EnumPaymentMethod paymentMethod;

    @Column(name = "`provider_payin`")
    @Getter
    @Setter
    protected String payIn;

    @Column(name = "`reference_number`")
    @Getter
    @Setter
    protected String referenceNumber;

    @Column(name = "`result_code`")
    @Getter
    @Setter
    protected String resultCode;

    @Column(name = "`result_message`")
    @Getter
    @Setter
    protected String resultMessage;
    
    @Column(name = "`invoice_printed_on`")
    @Getter
    @Setter
    protected ZonedDateTime invoicePrintedOn;

    public abstract ConsumerPayInDto toConsumerDto(boolean includeDetails);

    public abstract ProviderPayInDto toProviderDto(boolean includeDetails);

    public abstract HelpdeskPayInDto toHelpdeskDto(boolean includeDetails);

}
