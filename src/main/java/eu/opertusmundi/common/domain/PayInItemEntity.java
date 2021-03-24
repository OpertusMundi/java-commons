package eu.opertusmundi.common.domain;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

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
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;

import eu.opertusmundi.common.model.payment.EnumPaymentItemType;
import eu.opertusmundi.common.model.payment.EnumTransactionStatus;
import eu.opertusmundi.common.model.payment.OrderPayInItemDto;
import eu.opertusmundi.common.model.payment.PayInItemDto;
import eu.opertusmundi.common.model.payment.PaymentException;
import eu.opertusmundi.common.model.payment.PaymentMessageCode;
import eu.opertusmundi.common.model.payment.SubscriptionBillingPayInItemDto;
import eu.opertusmundi.common.model.payment.TransferDto;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "PayInItem")
@Table(schema = "billing", name = "`payin_item`")
public class PayInItemEntity {

    @Id
    @Column(name = "`id`", updatable = false)
    @SequenceGenerator(sequenceName = "billing.payin_item_id_seq", name = "payin_item_id_seq", allocationSize = 1)
    @GeneratedValue(generator = "payin_item_id_seq", strategy = GenerationType.SEQUENCE)
    @Getter
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payin", nullable = false)
    @Getter
    @Setter
    private PayInEntity payin;

    @NotNull
    @Column(name = "`index`")
    @Getter
    @Setter
    private Integer index;

    @NotNull
    @Column(name = "`type`")
    @Enumerated(EnumType.STRING)
    @Getter
    @Setter
    private EnumPaymentItemType type;

    @ManyToOne(targetEntity = OrderEntity.class)
    @JoinColumn(name = "`order`", nullable = false)
    @Getter
    @Setter
    private OrderEntity order;

    @ManyToOne(targetEntity = SubscriptionBillingEntity.class)
    @JoinColumn(name = "subscription_billing", nullable = false)
    @Getter
    @Setter
    private SubscriptionBillingEntity subscriptionBilling;
    
    @Column(name = "`tranfer`")
    @Getter
    @Setter
    private String tranfer;
    
    @Column(name = "`tranfer_credited_funds`", columnDefinition = "numeric", precision = 20, scale = 6)
    @Getter
    @Setter
    private BigDecimal tranferCreditedFunds;
    
    @Column(name = "`tranfer_platform_fees`", columnDefinition = "numeric", precision = 20, scale = 6)
    @Getter
    @Setter
    private BigDecimal tranferFees;
    
    @Column(name = "`tranfer_status`")
    @Enumerated(EnumType.STRING)
    @Getter
    @Setter
    private EnumTransactionStatus tranferStatus;
    
    @Column(name = "`tranfer_created_on`")
    @Getter
    @Setter
    private ZonedDateTime tranferCreatedOn;

    @Column(name = "`tranfer_executed_on`")
    @Getter
    @Setter
    private ZonedDateTime tranferExecutedOn;

    public PayInItemDto toDto() throws PaymentException {
        switch (type) {
            case ORDER :
                return this.toOrderDto();
            case SUBSCRIPTION_BILLING :
                return this.toSubscriptionBillingDto();
        }

        throw new PaymentException(PaymentMessageCode.SERVER_ERROR, String.format("PayIn item type [%s] is not supported", type));
    }

    private OrderPayInItemDto toOrderDto() {
        final OrderPayInItemDto i = new OrderPayInItemDto();
    
        i.setId(id);
        i.setIndex(index);
        i.setOrder(this.order.toDto());
        i.setTransfer(this.toTransferDto());
        i.setType(type);
        
        return i;
    }
    
    private SubscriptionBillingPayInItemDto toSubscriptionBillingDto() {
        final SubscriptionBillingPayInItemDto i = new SubscriptionBillingPayInItemDto();
    
        i.setId(id);
        i.setIndex(index);
        i.setSubscriptionBilling(this.subscriptionBilling.toDto());
        i.setTransfer(this.toTransferDto());
        i.setType(type);
        
        return i;
    }

    private TransferDto toTransferDto() {
        final TransferDto t = new TransferDto();

        if (StringUtils.isBlank(tranfer)) {
            return null;
        }
        
        t.setCreatedOn(tranferCreatedOn);
        t.setCreditedFunds(tranferCreditedFunds);
        t.setExecutedOn(tranferExecutedOn);
        t.setFees(tranferFees);
        t.setId(tranfer);
        t.setStatus(tranferStatus);

        return t;
    }

}