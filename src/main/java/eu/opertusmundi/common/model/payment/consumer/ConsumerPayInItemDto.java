package eu.opertusmundi.common.model.payment.consumer;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import eu.opertusmundi.common.model.payment.PayInItemDto;
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
    @Type(name = "ORDER", value = ConsumerOrderPayInItemDto.class),
    @Type(name = "SUBSCRIPTION_BILLING", value = ConsumerSubscriptionBillingPayInItemDto.class),
})
public class ConsumerPayInItemDto extends PayInItemDto {

}
