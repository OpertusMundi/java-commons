package eu.opertusmundi.common.model.payment;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreType;

import eu.opertusmundi.common.model.account.BankAccountDto;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Command for creating a new Bank wire PayIn
 */
@NoArgsConstructor
@Getter
@Setter
@JsonIgnoreType
public class BankwirePayInCommand extends PayInCommand {

    @Builder
    public BankwirePayInCommand(
        UUID userKey, UUID orderKey, BigDecimal debitedFunds, String referenceNumber, String payIn,
        ZonedDateTime createdOn, EnumTransactionStatus status, ZonedDateTime executedOn, String resultCode, String resultMessage,
        String wireReference, BankAccountDto bankAccount
    ) {
        super(userKey, orderKey, debitedFunds, referenceNumber, payIn, createdOn, status, executedOn, resultCode, resultMessage);

        this.wireReference = wireReference;
        this.bankAccount   = bankAccount;
    }

    /**
     * Transaction wire reference as set by the payment provider
     */
    private String wireReference;

    /**
     * Bank account of the payment provider
     */
    private BankAccountDto bankAccount;

}
