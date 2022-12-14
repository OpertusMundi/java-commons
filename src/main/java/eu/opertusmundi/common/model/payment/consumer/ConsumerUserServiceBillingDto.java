package eu.opertusmundi.common.model.payment.consumer;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import eu.opertusmundi.common.model.payment.ServiceBillingDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class ConsumerUserServiceBillingDto extends ServiceBillingDto {

    @Schema(description = "Consumer subscription")
    @JsonInclude(Include.NON_NULL)
    private ConsumerAccountSubscriptionDto subscription;

}
