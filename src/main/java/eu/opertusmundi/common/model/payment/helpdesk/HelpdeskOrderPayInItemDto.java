package eu.opertusmundi.common.model.payment.helpdesk;

import eu.opertusmundi.common.model.order.HelpdeskOrderDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class HelpdeskOrderPayInItemDto extends HelpdeskPayInItemDto {

    @Schema(description = "PayIn order")
    private HelpdeskOrderDto order;

}
