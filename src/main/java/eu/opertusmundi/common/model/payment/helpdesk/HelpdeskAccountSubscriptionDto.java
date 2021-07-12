package eu.opertusmundi.common.model.payment.helpdesk;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import eu.opertusmundi.common.model.account.AccountSubscriptionDto;
import eu.opertusmundi.common.model.account.CustomerDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class HelpdeskAccountSubscriptionDto extends AccountSubscriptionDto {

    @JsonInclude(Include.NON_NULL)
    private CustomerDto consumer;

    @JsonInclude(Include.NON_NULL)
    private CustomerDto provider;

}
