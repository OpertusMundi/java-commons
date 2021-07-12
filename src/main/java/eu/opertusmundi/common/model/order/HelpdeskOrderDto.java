package eu.opertusmundi.common.model.order;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import eu.opertusmundi.common.model.account.CustomerDto;
import eu.opertusmundi.common.model.payment.PayInDto;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HelpdeskOrderDto extends OrderDto {

    @ArraySchema(arraySchema = @Schema(
        description = "Order items. Currently only a single item is allowed per order"),
        minItems = 1,
        maxItems = 1,
        uniqueItems = true,
        schema = @Schema(implementation = HelpdeskOrderItemDto.class)
    )
    @JsonInclude(Include.NON_EMPTY)
    private List<HelpdeskOrderItemDto> items = new ArrayList<>();

    @Hidden
    @ArraySchema(arraySchema = @Schema(
        description = "Order status history records"),
        minItems = 0,
        uniqueItems = true,
        schema = @Schema(implementation = OrderStatusDto.class)
    )
    @JsonInclude(Include.NON_EMPTY)
    private List<OrderStatusDto> statusHistory = new ArrayList<>();

    @Schema(description = "Order consumer customer")
    @JsonInclude(Include.NON_NULL)
    private CustomerDto consumer;

    @Schema(description = "Order linked PayIn object")
    @JsonInclude(Include.NON_NULL)
    private PayInDto payIn;

    public void addItem(HelpdeskOrderItemDto i) {
        this.items.add(i);
    }

    public void addStatusHistory(OrderStatusDto h) {
        this.statusHistory.add(h);
    }

}
