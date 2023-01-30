package eu.opertusmundi.common.domain;

import java.util.UUID;

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

        this.key  = UUID.randomUUID();
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
    public ConsumerPayInItemDto toConsumerDto(boolean includeDetails, boolean includePayIn) {
        final ConsumerOrderPayInItemDto i = new ConsumerOrderPayInItemDto();

        this.updateDto(i);

        i.setOrder(this.order.toConsumerDto(includeDetails, true, includePayIn));

        return i;
    }

    @Override
    public ProviderPayInItemDto toProviderDto(boolean includeDetails, boolean includePayIn) {
        final ProviderOrderPayInItemDto i = new ProviderOrderPayInItemDto();

        this.updateDto(i);

        i.setOrder(this.order.toProviderDto(includeDetails, includePayIn));

        if (includeDetails && this.getTransfer() != null) {
            i.setTransfer(this.getTransfer().toDto(false));
        }

        return i;
    }

    @Override
    public HelpdeskPayInItemDto toHelpdeskDto(boolean includeDetails, boolean includePayIn) {
        final HelpdeskOrderPayInItemDto i = new HelpdeskOrderPayInItemDto();

        this.updateDto(i);

        i.setOrder(this.order.toHelpdeskDto(includeDetails, includePayIn));
        if (this.getTransfer() != null) {
            i.setTransfer(this.getTransfer().toDto(true));
        }

        return i;
    }

}