package eu.opertusmundi.common.model.payment;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class PayInDto extends TransactionDto {

    public PayInDto() {
        super(EnumTransactionType.PAYIN);
    }

    @JsonIgnore
    protected Integer id;

    @JsonIgnore
    protected Integer consumerId;

    @JsonIgnore
    protected UUID consumerKey;

    @JsonIgnore
    protected List<UUID> providerKey;

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

    @Hidden
    @ArraySchema(arraySchema = @Schema(
        description = "PayIn status history records"),
        minItems = 0,
        uniqueItems = true,
        schema = @Schema(implementation = PayInStatusDto.class)
    )
    @JsonInclude(Include.NON_EMPTY)
    private List<PayInStatusDto> statusHistory = new ArrayList<>();

    @Schema(description = "Date of transaction status last update")
    protected ZonedDateTime statusUpdatedOn;

    @Schema(description = "Payment method")
    protected EnumPaymentMethod paymentMethod;

    @Schema(description = "Platform reference number")
    protected String referenceNumber;

    @JsonIgnore
    protected ZonedDateTime invoicePrintedOn;

    @Schema(description = "`true` if the invoice has been created")
    protected boolean invoicePrinted;

    @Schema(description = "PayIn refund")
    @JsonInclude(Include.NON_NULL)
    protected RefundDto refund;

    @Schema(description = "PayIn dispute")
    @JsonInclude(Include.NON_NULL)
    protected DisputeDto dispute;

    public void addStatusHistory(PayInStatusDto h) {
        this.statusHistory.add(h);
    }

}
