package eu.opertusmundi.common.model.payment.consumer;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class ConsumerServiceBillingPayInItemDto extends ConsumerPayInItemDto {

    @Schema(description = "PayIn subscription billing record")
    protected ConsumerServiceBillingDto serviceBilling;

}
