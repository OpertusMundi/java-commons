package eu.opertusmundi.common.model.payment;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

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
    private String id;

    private UUID key;

    @Schema(description = "Funds debited from buyer's wallet and credited to seller's wallet")
    private BigDecimal creditedFunds;

    @Schema(description = "Platform fees")
    private BigDecimal fees;

    @Schema(description = "Transaction status")
    private EnumTransactionStatus status;

    @Schema(description = "Date of creation")
    private ZonedDateTime createdOn;

    @Schema(description = "Date of execution")
    private ZonedDateTime executedOn;

    @Schema(description = "Payment provider result code")
    private String resultCode;

    @Schema(description = "Payment provider result message")
    private String resultMessage;

}
