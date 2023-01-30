package eu.opertusmundi.common.domain;

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

import org.hibernate.annotations.NaturalId;

import eu.opertusmundi.common.model.payment.EnumPaymentItemType;
import eu.opertusmundi.common.model.payment.consumer.ConsumerPayInItemDto;
import eu.opertusmundi.common.model.payment.helpdesk.HelpdeskPayInItemDto;
import eu.opertusmundi.common.model.payment.provider.ProviderPayInItemDto;
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
    @NaturalId
    @Column(name = "`key`", updatable = false, columnDefinition = "uuid")
    @Getter
    @Setter
    protected UUID key;
    
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payin", nullable = false)
    @Getter
    @Setter
    protected PayInEntity payin;

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

    @ManyToOne(targetEntity = TransferEntity.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "transfer")
    @Getter
    @Setter
    protected TransferEntity transfer;

    public abstract ConsumerPayInItemDto toConsumerDto(boolean includeDetails, boolean includePayIn);

    public abstract ProviderPayInItemDto toProviderDto(boolean includeDetails, boolean includePayIn);

    public abstract HelpdeskPayInItemDto toHelpdeskDto(boolean includeDetails, boolean includePayIn);

}