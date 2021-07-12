package eu.opertusmundi.common.model.payment.consumer;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import eu.opertusmundi.common.model.account.AccountSubscriptionDto;
import eu.opertusmundi.common.model.account.ProviderDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class ConsumerAccountSubscriptionDto extends AccountSubscriptionDto {

    @JsonInclude(Include.NON_NULL)
    private ProviderDto provider;

}
