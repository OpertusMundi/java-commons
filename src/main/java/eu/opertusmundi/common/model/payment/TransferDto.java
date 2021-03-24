package eu.opertusmundi.common.model.payment;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class TransferDto {

    @JsonIgnore
    protected String id;

    @Schema(description = "Funds debited from buyer's wallet and credited to seller's wallet")
    protected BigDecimal creditedFunds;

    @Schema(description = "Platform fees")
    protected BigDecimal fees;

    @Schema(description = "Transaction status")
    protected EnumTransactionStatus status;

    @Schema(description = "Date of creation")
    protected ZonedDateTime createdOn;

    @Schema(description = "Date of execution")
    protected ZonedDateTime executedOn;

}
