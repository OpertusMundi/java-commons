package eu.opertusmundi.common.model.payment;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import eu.opertusmundi.common.model.account.CustomerDto;
import io.swagger.v3.oas.annotations.ExternalDocumentation;
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
    @Type(name = "CARD_DIRECT", value = CardDirectPayInDto.class),
    @Type(name = "BANKWIRE", value = BankwirePayInDto.class),
})
public abstract class PayInDto {

    @JsonIgnore
    protected Integer id;

    @Schema(description = "PayIn unique key")
    protected UUID key;

    /**
     * Identifier of the workflow definition used for processing this PayIn
     * record
     */
    @JsonInclude(Include.NON_NULL)
    protected String processDefinition;

    /**
     * Identifier of the workflow instance processing this PayIn record
     */
    @JsonInclude(Include.NON_NULL)
    protected String processInstance;

    @ArraySchema(arraySchema = @Schema(
        description = "PayIn payments. A PayIn may include a single order or multiple subscription billing records"),
        minItems = 1,
        uniqueItems = true,
        schema = @Schema(oneOf = {OrderPayInItemDto.class, SubscriptionBillingPayInItemDto.class})
    )
    @JsonInclude(Include.NON_EMPTY)
    protected List<PayInItemDto> items = new ArrayList<>();

    @Schema(description = "The total price of all PayIn items (the debited funds of the PayIn)")
    protected BigDecimal totalPrice;

    @Schema(description = "The total price of all PayIn items excluding tax")
    protected BigDecimal totalPriceExcludingTax;

    @Schema(description = "The total tax for all PayIn items")
    protected BigDecimal totalTax;

    @Schema(
        description = "The currency in ISO 4217 format. Only `EUR` is supported",
        externalDocs = @ExternalDocumentation(url = "https://en.wikipedia.org/wiki/ISO_4217")
    )
    protected String currency;

    @Schema(description = "PayIn creation date")
    protected ZonedDateTime createdOn;

    @Schema(description = "PayIn execution date")
    protected ZonedDateTime executedOn;

    @Schema(description = "Transaction status")
    protected EnumTransactionStatus status;

    @Schema(description = "Date of transaction status last update")
    protected ZonedDateTime statusUpdatedOn;

    @Schema(description = "Payment method")
    protected EnumPaymentMethod paymentMethod;

    /**
     * The payment provider transaction unique identifier
     */
    @JsonIgnore
    protected String payIn;

    @Schema(description = "Platform reference number")
    protected String referenceNumber;

    @JsonInclude(Include.NON_NULL)
    private CustomerDto customer;

    @JsonInclude(Include.NON_EMPTY)
    protected String providerPayIn;

    @JsonInclude(Include.NON_EMPTY)
    protected String providerResultCode;

    @JsonInclude(Include.NON_EMPTY)
    protected String providerResultMessage;

    public void addItem(PayInItemDto i) {
        this.items.add(i);
    }

}
