package eu.opertusmundi.common.model.payment;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import eu.opertusmundi.common.model.account.CustomerDto;
import eu.opertusmundi.common.model.account.CustomerProfessionalDto;
import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RefundDto {

    @JsonIgnore
    @Hidden
    private Integer id;

    @Schema(description = "Refund unique identifier")
    private UUID key;

    @Schema(description = "Topio reference number")
    private String referenceNumber;

    @Schema(description = "Topio consumer")
    @JsonInclude(Include.NON_NULL)
    private CustomerDto consumer;

    @Schema(description = "Topio provider")
    @JsonInclude(Include.NON_NULL)
    private CustomerProfessionalDto provider;

    @Schema(description = "The amount of the debited funds")
    private BigDecimal debitedFunds;

    @Schema(description = "The amount of credited funds")
    private BigDecimal creditedFunds;

    @Schema(description = "The amount of client (Topio) fees")
    private BigDecimal fees;

    @Schema(
        description = "The currency in ISO-4217 format",
        externalDocs = @ExternalDocumentation(url = "https://en.wikipedia.org/wiki/ISO_4217")
    )
    private String currency;

    @JsonInclude(Include.NON_EMPTY)
    @Schema(description = "The ID of the wallet that was debited", hidden = true)
    private String debitedWalletId;

    @JsonInclude(Include.NON_EMPTY)
    @Schema(description = "The ID of the wallet where money will be credited", hidden = true)
    private String creditedWalletId;

    @JsonInclude(Include.NON_EMPTY)
    @Schema(description = "A user's ID", hidden = true)
    private String authorId;

    @JsonInclude(Include.NON_EMPTY)
    @Schema(description = "The user ID who is credited (defaults to the owner of the wallet)", hidden = true)
    private String creditedUserId;

    @Schema(description = "When the transaction created")
    private ZonedDateTime creationDate;

    @Schema(description = "When the transaction happened")
    private ZonedDateTime executionDate;

    @Schema(description = "The result code")
    private String resultCode;

    @Schema(description = "A verbal explanation of the `ResultCode`")
    private String resultMessage;

    @JsonInclude(Include.NON_EMPTY)
    @Schema(description = "The transaction ID", hidden = true)
    private String transactionId;

    @Schema(description = "The status of the transaction")
    private EnumTransactionStatus transactionStatus;

    @Schema(description = "The nature of the transaction")
    private EnumTransactionNature transactionNature;

    @Schema(description = "The type of the transaction")
    private EnumTransactionType transactionType;

    @JsonInclude(Include.NON_EMPTY)
    @Schema(description = "The initial transaction ID", hidden = true)
    private String initialTransactionId;

    @Schema(description = "The initial Topio transaction key")
    private UUID initialTransactionKey;
    
    @Schema(description = "The initial transaction type")
    private EnumTransactionType initialTransactionType;

    @Schema(description = "The type of reason for refusal")
    private EnumRefundReasonType refundReasonType;

    @Schema(description = "The message accompanying a refusal")
    private String refundReasonMessage;

}
