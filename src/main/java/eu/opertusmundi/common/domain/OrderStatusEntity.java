package eu.opertusmundi.common.domain;

import java.time.ZonedDateTime;
import java.util.Optional;

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

import eu.opertusmundi.common.model.order.EnumOrderStatus;
import eu.opertusmundi.common.model.order.OrderStatusDto;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "OrderStatus")
@Table(schema = "`order`", name = "`order_status_hist`")
public class OrderStatusEntity {

    @Id
    @Column(name = "`id`", updatable = false)
    @SequenceGenerator(sequenceName = "`order.order_status_hist_id_seq`", name = "order_status_hist_id_seq", allocationSize = 1)
    @GeneratedValue(generator = "order_status_hist_id_seq", strategy = GenerationType.SEQUENCE)
    @Getter
    private Integer id;

    @NotNull
    @ManyToOne(targetEntity = OrderEntity.class)
    @JoinColumn(name = "`order`", nullable = false)
    @Getter
    @Setter
    private OrderEntity order;

    @NotNull
    @Column(name = "`status`")
    @Enumerated(EnumType.STRING)
    @Getter
    @Setter
    private EnumOrderStatus status;

    @NotNull
    @Column(name = "`status_updated_on`")
    @Getter
    @Setter
    private ZonedDateTime statusUpdatedOn;

    @ManyToOne(targetEntity = AccountEntity.class)
    @JoinColumn(name = "`status_updated_by`")
    @Getter
    @Setter
    private AccountEntity statusUpdatedBy;

    public OrderStatusDto toDto() {
        final OrderStatusDto s = new OrderStatusDto();

        s.setId(id);
        s.setOrderId(order.getId());
        s.setStatus(status);
        s.setStatusUpdatedOn(statusUpdatedOn);

        s.setStatusUpdatedBy(Optional.ofNullable(statusUpdatedBy).map(AccountEntity::toSimpleDto).orElse(null));
        
        return s;
    }

}
