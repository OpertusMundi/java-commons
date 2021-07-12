package eu.opertusmundi.common.model.payment.provider;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import eu.opertusmundi.common.model.account.AccountSubscriptionDto;
import eu.opertusmundi.common.model.account.ConsumerDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class ProviderAccountSubscriptionDto extends AccountSubscriptionDto {

    @JsonInclude(Include.NON_NULL)
    private ConsumerDto consumer;

}
