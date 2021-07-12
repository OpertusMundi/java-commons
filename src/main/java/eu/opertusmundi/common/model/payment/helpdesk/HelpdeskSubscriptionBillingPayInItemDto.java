package eu.opertusmundi.common.model.payment.helpdesk;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class HelpdeskSubscriptionBillingPayInItemDto extends HelpdeskPayInItemDto {

    @Schema(description = "PayIn subscription billing record")
    protected HelpdeskSubscriptionBillingDto subscriptionBilling;

}
