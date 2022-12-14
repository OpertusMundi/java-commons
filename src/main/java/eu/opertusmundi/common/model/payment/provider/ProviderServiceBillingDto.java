package eu.opertusmundi.common.model.payment.provider;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import eu.opertusmundi.common.model.asset.service.UserServiceDto;
import eu.opertusmundi.common.model.payment.ServiceBillingDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class ProviderServiceBillingDto extends ServiceBillingDto {

    @Schema(description = "Consumer subscription")
    @JsonInclude(Include.NON_NULL)
    private ProviderAccountSubscriptionDto subscription;

    @Schema(description = "User service")
    @JsonInclude(Include.NON_NULL)
    private UserServiceDto service;

}
