package eu.opertusmundi.common.model.payment.helpdesk;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import eu.opertusmundi.common.model.account.CustomerDto;
import eu.opertusmundi.common.model.payment.PayInDto;
import io.swagger.v3.oas.annotations.media.ArraySchema;
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
    @Type(name = "CARD_DIRECT", value = HelpdeskCardDirectPayInDto.class),
    @Type(name = "BANKWIRE", value = HelpdeskBankwirePayInDto.class),
    @Type(name = "FREE", value = HelpdeskFreePayInDto.class),
})
public class HelpdeskPayInDto extends PayInDto {

    @Schema(description = "Identifier of the workflow definition used for processing this PayIn record")
    @JsonInclude(Include.NON_NULL)
    protected String processDefinition;

    @Schema(description = "Identifier of the workflow instance processing this PayIn record")
    @JsonInclude(Include.NON_NULL)
    protected String processInstance;

    @ArraySchema(arraySchema = @Schema(
        description = "PayIn payments. A PayIn may include a single order or multiple subscription billing records"),
        minItems = 1,
        uniqueItems = true,
        schema = @Schema(oneOf = {HelpdeskOrderPayInItemDto.class, HelpdeskSubscriptionBillingPayInItemDto.class})
    )
    @JsonInclude(Include.NON_EMPTY)
    protected List<HelpdeskPayInItemDto> items = new ArrayList<>();

    @Schema(description = "PayIn consumer customer")
    @JsonInclude(Include.NON_NULL)
    private CustomerDto consumer;

    @Schema(description = "Payment provider identifier for PayIn")
    @JsonInclude(Include.NON_EMPTY)
    protected String providerPayIn;

    @Schema(description = "Payment provider transaction result code")
    @JsonInclude(Include.NON_EMPTY)
    protected String providerResultCode;

    @Schema(description = "Payment provider transaction result message")
    @JsonInclude(Include.NON_EMPTY)
    protected String providerResultMessage;

    @Schema(description = "Requested 3DS version")
    private String requested3dsVersion;

    @Schema(description = "Applied 3DS version")
    private String applied3dsVersion;

    public void addItem(HelpdeskPayInItemDto i) {
        this.items.add(i);
    }

}
