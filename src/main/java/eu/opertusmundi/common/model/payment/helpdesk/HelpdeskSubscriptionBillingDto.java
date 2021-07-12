package eu.opertusmundi.common.model.payment.helpdesk;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import eu.opertusmundi.common.model.payment.SubscriptionBillingDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class HelpdeskSubscriptionBillingDto extends SubscriptionBillingDto {

    @JsonInclude(Include.NON_NULL)
    private HelpdeskAccountSubscriptionDto subscription;

}
