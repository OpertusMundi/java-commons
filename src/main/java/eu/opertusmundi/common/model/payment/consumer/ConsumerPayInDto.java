package eu.opertusmundi.common.model.payment.consumer;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import eu.opertusmundi.common.model.payment.PayInDto;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.DiscriminatorMapping;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
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
    @Type(name = "CARD_DIRECT", value = ConsumerCardDirectPayInDto.class),
    @Type(name = "BANKWIRE", value = ConsumerBankwirePayInDto.class),
    @Type(name = "FREE", value = ConsumerFreePayInDto.class),
})
@Schema(
    description = "Consumer PayIn",
    requiredMode = RequiredMode.REQUIRED,
    discriminatorMapping = {
        @DiscriminatorMapping(value = "CARD_DIRECT", schema = ConsumerCardDirectPayInDto.class),
        @DiscriminatorMapping(value = "BANKWIRE", schema = ConsumerBankwirePayInDto.class),
        @DiscriminatorMapping(value = "FREE", schema = ConsumerFreePayInDto.class)
    }
)
public class ConsumerPayInDto extends PayInDto {

    @ArraySchema(arraySchema = @Schema(
        description = "PayIn payments. A PayIn may include a single order or multiple subscription billing records"),
        minItems = 1,
        uniqueItems = true
    )
    @JsonInclude(Include.NON_EMPTY)
    protected List<ConsumerPayInItemDto> items = new ArrayList<>();

    public void addItem(ConsumerPayInItemDto i) {
        this.items.add(i);
    }

}
