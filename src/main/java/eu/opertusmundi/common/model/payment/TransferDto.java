package eu.opertusmundi.common.model.payment;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TransferDto extends TransactionDto {

    public TransferDto() {
        super(EnumTransactionType.TRANSFER);
    }

    @Schema(description = "Funds debited from buyer's wallet and credited to seller's wallet")
    private BigDecimal creditedFunds;

    @Schema(description = "Platform fees")
    private BigDecimal fees;

    @Schema(hidden = true, description = "Payment provider transfer identifier")
    @JsonInclude(Include.NON_EMPTY)
    private String providerId;

    @Schema(hidden = true, description = "Payment provider result code")
    @JsonInclude(Include.NON_NULL)
    private String resultCode;

    @Schema(hidden = true, description = "Payment provider result message")
    @JsonInclude(Include.NON_NULL)
    private String resultMessage;

}
