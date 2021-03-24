package eu.opertusmundi.common.model.payment;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class PayOutDto {

    @JsonIgnore
    private Integer id;

    @Schema(description = "Payout platform unique key")
    private UUID key;

    @JsonIgnore
    private String payOut;

    @Schema(description = "Information about the funds that are being debited from seller's wallet")
    private BigDecimal creditedFunds;

    @Schema(description = "Information about the fees that were taken by the client for this transaction")
    private BigDecimal fees;

    @Schema(
        description = "The currency in ISO 4217 format. Only `EUR` is supported", 
        externalDocs = @ExternalDocumentation(url = "https://en.wikipedia.org/wiki/ISO_4217")
    )
    private String currency;

    @Schema(description = "Transaction status")
    private EnumTransactionStatus status;

    @Schema(description = "Date of creation")
    private ZonedDateTime createdOn;

    @Schema(description = "Date of execution")
    private ZonedDateTime executedOn;

    @Schema(description = "A custom reference that will appear on the user’s bank statement")
    private String bankwireRef;

}
