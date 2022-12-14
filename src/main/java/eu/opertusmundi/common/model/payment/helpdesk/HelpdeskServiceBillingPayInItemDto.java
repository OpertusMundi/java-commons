package eu.opertusmundi.common.model.payment.helpdesk;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class HelpdeskServiceBillingPayInItemDto extends HelpdeskPayInItemDto {

    @Schema(description = "PayIn service billing record")
    protected HelpdeskServiceBillingDto serviceBilling;

}
