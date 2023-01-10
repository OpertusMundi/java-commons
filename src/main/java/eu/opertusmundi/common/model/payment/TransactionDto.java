package eu.opertusmundi.common.model.payment;

import java.time.ZonedDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class TransactionDto {

    public TransactionDto(EnumTransactionType transactionType) {
        this.transactionType = transactionType;
    }

    @Hidden
    @JsonInclude(Include.NON_EMPTY)
    private String transactionId;

    @Hidden
    @JsonIgnore
    @Setter(AccessLevel.PRIVATE)
    private EnumTransactionType transactionType;

    @Schema(description = "Transaction platform unique key")
    protected UUID key;

    @Schema(description = "Transaction creation date")
    protected ZonedDateTime createdOn;

    @Schema(description = "Transaction execution date")
    protected ZonedDateTime executedOn;

    @Schema(description = "Transaction status")
    protected EnumTransactionStatus status;

}
