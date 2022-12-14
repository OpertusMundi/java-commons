package eu.opertusmundi.common.model.payment.helpdesk;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import eu.opertusmundi.common.model.payment.ServiceBillingDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class HelpdeskServiceBillingDto extends ServiceBillingDto {

    @JsonInclude(Include.NON_NULL)
    private HelpdeskAccountSubscriptionDto subscription;

}
