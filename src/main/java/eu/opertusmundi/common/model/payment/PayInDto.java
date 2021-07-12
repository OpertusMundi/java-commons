package eu.opertusmundi.common.model.payment;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
public abstract class PayInDto {

    @JsonIgnore
    protected Integer id;

    /**
     * The payment provider transaction unique identifier
     */
    @JsonIgnore
    protected String payIn;

    @Schema(description = "PayIn unique key")
    protected UUID key;

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

    @Schema(description = "Platform reference number")
    protected String referenceNumber;

}
