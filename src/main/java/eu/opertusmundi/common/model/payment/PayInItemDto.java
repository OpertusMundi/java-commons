package eu.opertusmundi.common.model.payment;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

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
    @Type(name = "ORDER", value = OrderPayInItemDto.class),
    @Type(name = "SUBSCRIPTION_BILLING", value = SubscriptionBillingPayInItemDto.class),
})
public class PayInItemDto {

    @JsonIgnore
    protected Integer id;
    
    @Schema(description = "Invoice line number")
    protected Integer index;

    @Schema(description = "Payment item type")
    protected EnumPaymentItemType type;

    @Schema(description = "Transfer of funds from the buyer's to the seller's wallet")
    @JsonInclude(Include.NON_NULL)
    protected TransferDto transfer;

}
