package eu.opertusmundi.common.model.payment;

import java.time.ZonedDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreType;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Command for creating a new card direct PayIn
 */
@NoArgsConstructor
@Getter
@Setter
@JsonIgnoreType
public class CardDirectPayInCommand extends PayInCommand {

    @Builder
    public CardDirectPayInCommand(
        UUID userKey, UUID orderKey, Integer debitedFunds, String referenceNumber, String payIn,
        ZonedDateTime createdOn, EnumTransactionStatus status, ZonedDateTime executedOn, String resultCode, String resultMessage,
        String cardId, String statementDescriptor
    ) {
        super(userKey, orderKey, debitedFunds, referenceNumber, payIn, createdOn, status, executedOn, resultCode, resultMessage);

        this.cardId              = cardId;
        this.statementDescriptor = statementDescriptor;
    }

    private String cardId;

    private String statementDescriptor;
    
}
