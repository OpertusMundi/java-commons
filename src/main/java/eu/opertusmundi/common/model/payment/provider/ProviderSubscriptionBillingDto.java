package eu.opertusmundi.common.model.payment.provider;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import eu.opertusmundi.common.model.payment.SubscriptionBillingDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class ProviderSubscriptionBillingDto extends SubscriptionBillingDto {

    @Schema(description = "Consumer subscription")
    @JsonInclude(Include.NON_NULL)
    private ProviderAccountSubscriptionDto subscription;

}
