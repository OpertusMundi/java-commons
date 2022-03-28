package eu.opertusmundi.common.model.payment.helpdesk;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import eu.opertusmundi.common.model.payment.PayInItemDto;
import eu.opertusmundi.common.model.payment.TransferDto;
import io.swagger.v3.oas.annotations.media.DiscriminatorMapping;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type"
)
@JsonSubTypes({
    @Type(name = "ORDER", value = HelpdeskOrderPayInItemDto.class),
    @Type(name = "SUBSCRIPTION_BILLING", value = HelpdeskSubscriptionBillingPayInItemDto.class),
})
@Schema(
    description = "Helpdesk PayIn item",
    required = true,
    discriminatorMapping = {
        @DiscriminatorMapping(value = "ORDER", schema = HelpdeskOrderPayInItemDto.class),
        @DiscriminatorMapping(value = "SUBSCRIPTION_BILLING", schema = HelpdeskSubscriptionBillingPayInItemDto.class)
    }
)
public class HelpdeskPayInItemDto extends PayInItemDto {

    @Schema(description = "Transfer of funds from the buyer's to the seller's wallet")
    @JsonInclude(Include.NON_NULL)
    protected TransferDto transfer;

}
