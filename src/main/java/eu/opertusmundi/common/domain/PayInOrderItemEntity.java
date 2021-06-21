package eu.opertusmundi.common.domain;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import eu.opertusmundi.common.model.payment.EnumPaymentItemType;
import eu.opertusmundi.common.model.payment.OrderPayInItemDto;
import eu.opertusmundi.common.model.payment.PayInItemDto;
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

    @Override
    public PayInItemDto toDto(boolean includeHelpdeskData) {
        final OrderPayInItemDto i = new OrderPayInItemDto();

        i.setId(id);
        i.setIndex(index);
        i.setOrder(this.order.toDto(includeHelpdeskData, includeHelpdeskData));
        i.setTransfer(this.toTransferDto());
        i.setType(type);

        return i;
    }

}