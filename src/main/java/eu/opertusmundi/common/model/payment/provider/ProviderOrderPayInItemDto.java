package eu.opertusmundi.common.model.payment.provider;

import eu.opertusmundi.common.model.order.ProviderOrderDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class ProviderOrderPayInItemDto extends ProviderPayInItemDto {

    @Schema(description = "PayIn order")
    private ProviderOrderDto order;

}
