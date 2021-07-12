package eu.opertusmundi.common.model.payment.consumer;

import eu.opertusmundi.common.model.order.ConsumerOrderDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class ConsumerOrderPayInItemDto extends ConsumerPayInItemDto {

    @Schema(description = "Order")
    private ConsumerOrderDto order;

}
