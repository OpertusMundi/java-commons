package eu.opertusmundi.common.model.payment.provider;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import eu.opertusmundi.common.model.account.ConsumerDto;
import eu.opertusmundi.common.model.payment.PayInDto;
import eu.opertusmundi.common.model.payment.RefundDto;
import io.swagger.v3.oas.annotations.media.ArraySchema;
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
    use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "paymentMethod"
)
@JsonSubTypes({
    @Type(name = "CARD_DIRECT", value = ProviderCardDirectPayInDto.class),
    @Type(name = "BANKWIRE", value = ProviderBankwirePayInDto.class),
    @Type(name = "FREE", value = ProviderFreePayInDto.class),
})
@Schema(
    description = "Provider PayIn item",
    required = true,
    discriminatorMapping = {
        @DiscriminatorMapping(value = "CARD_DIRECT", schema = ProviderCardDirectPayInDto.class),
        @DiscriminatorMapping(value = "BANKWIRE", schema = ProviderBankwirePayInDto.class),
        @DiscriminatorMapping(value = "FREE", schema = ProviderFreePayInDto.class)
    }
)
public class ProviderPayInDto extends PayInDto {

    @ArraySchema(arraySchema = @Schema(
        description = "PayIn payments. A PayIn may include a single order or multiple subscription billing records"),
        minItems = 1,
        uniqueItems = true
    )
    @JsonInclude(Include.NON_EMPTY)
    protected List<ProviderPayInItemDto> items = new ArrayList<>();

    @Schema(description = "PayIn consumer customer")
    @JsonInclude(Include.NON_NULL)
    private ConsumerDto consumer;

    @Schema(description = "PayIn refund")
    protected RefundDto refund;

    public void addItem(ProviderPayInItemDto i) {
        this.items.add(i);
    }

}
