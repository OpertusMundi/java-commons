package eu.opertusmundi.common.domain;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import eu.opertusmundi.common.model.payment.EnumPaymentItemType;
import eu.opertusmundi.common.model.payment.PayInItemDto;
import eu.opertusmundi.common.model.payment.consumer.ConsumerOrderPayInItemDto;
import eu.opertusmundi.common.model.payment.consumer.ConsumerPayInItemDto;
import eu.opertusmundi.common.model.payment.helpdesk.HelpdeskOrderPayInItemDto;
import eu.opertusmundi.common.model.payment.helpdesk.HelpdeskPayInItemDto;
import eu.opertusmundi.common.model.payment.provider.ProviderOrderPayInItemDto;
import eu.opertusmundi.common.model.payment.provider.ProviderPayInItemDto;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "PayInOrderItem")
@DiscriminatorValue("ORDER")
public class PayInOrderItemEntity extends PayInItemEntity {

    public PayInOrderItemEntity() {
        super();

        this.type = EnumPaymentItemType.ORDER;
    }

    @ManyToOne(targetEntity = OrderEntity.class)
    @JoinColumn(name = "`order`", nullable = false)
    @Getter
    @Setter
    private OrderEntity order;

    public void updateDto(PayInItemDto i) {
        i.setId(id);
        i.setIndex(index);
        i.setPayIn(payin.getKey());
        i.setType(type);
    }

    @Override
    public ConsumerPayInItemDto toConsumerDto(boolean includeDetails) {
        final ConsumerOrderPayInItemDto i = new ConsumerOrderPayInItemDto();

        this.updateDto(i);

        i.setOrder(this.order.toConsumerDto(includeDetails));

        return i;
    }

    @Override
    public ProviderPayInItemDto toProviderDto(boolean includeDetails) {
        final ProviderOrderPayInItemDto i = new ProviderOrderPayInItemDto();

        this.updateDto(i);

        i.setOrder(this.order.toProviderDto(includeDetails));

        if (includeDetails) {
            i.setTransfer(this.toTransferDto(false));
        }

        return i;
    }

    @Override
    public HelpdeskPayInItemDto toHelpdeskDto(boolean includeDetails) {
        final HelpdeskOrderPayInItemDto i = new HelpdeskOrderPayInItemDto();

        this.updateDto(i);

        i.setOrder(this.order.toHelpdeskDto(includeDetails));
        i.setTransfer(this.toTransferDto(true));

        return i;
    }

}