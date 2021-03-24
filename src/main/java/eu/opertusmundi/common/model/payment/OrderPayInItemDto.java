package eu.opertusmundi.common.model.payment;

import eu.opertusmundi.common.model.order.OrderDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class OrderPayInItemDto extends PayInItemDto {

    @Schema(description = "PayIn order")
    private OrderDto order;

}
