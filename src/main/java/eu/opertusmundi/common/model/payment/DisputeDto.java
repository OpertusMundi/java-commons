package eu.opertusmundi.common.model.payment;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DisputeDto {

    @JsonIgnore
    @Hidden
    private Integer id;

    @Schema(description = "Dispute unique identifier")
    private UUID key;

    @JsonInclude(Include.NON_NULL)
    @Schema(description = "Disputed PayIn")
    private PayInDto payin;

    @Schema(description = "When the transaction created")
    private ZonedDateTime creationDate;

    @Schema(description = "The ID of the associated dispute transaction")
    private String transactionId;

    @Schema(description = "The ID of the associated repudiation transaction")
    private String repudiationId;

    @Schema(description = "The type of dispute")
    private EnumDisputeType type;

    @Schema(description = "The status of the dispute")
    private EnumDisputeStatus status;

    @Schema(description = "Used to communicate information about the dispute status to you")
    private String statusMessage;

    @Schema(description = "The deadline by which you must contest the dispute (if you wish to contest it)")
    private ZonedDateTime contestDeadlineDate;

    @Schema(description = "The amount of funds that were disputed")
    private BigDecimal disputedFunds;

    @Schema(description = "The amount you wish to contest")
    private BigDecimal contestedFunds;

    @Schema(description = "The type of reason for the dispute")
    private EnumDisputeReasonType reasonType;

    @Schema(description = "More information about the reason for the dispute")
    private String reasonMessage;

    @Schema(description = "The result code")
    private String resultCode;

    @Schema(description = "A verbal explanation of the ResultCode")
    private String resultMessage;

    @Schema(description = "The initial transaction ID")
    private String initialTransactionId;

    @Schema(description = "The initial Topio transaction key")
    private UUID initialTransactionKey;

    @Schema(description = "The Topio reference number for the linked PayIn")
    private String initialTransactionRefNumber;

    @Schema(description = "The initial transaction type")
    private EnumTransactionType initialTransactionType;

}
