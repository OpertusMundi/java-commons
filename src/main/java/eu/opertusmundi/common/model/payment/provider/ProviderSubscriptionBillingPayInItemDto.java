package eu.opertusmundi.common.model.payment.provider;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class ProviderSubscriptionBillingPayInItemDto extends ProviderPayInItemDto {

    @Schema(description = "PayIn subscription billing record")
    protected ProviderSubscriptionBillingDto subscriptionBilling;

}
