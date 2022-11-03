package eu.opertusmundi.common.model.payment;

import eu.opertusmundi.common.model.account.BankAccountDto;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Helper class that holds information generated by the platform and MANGOPAY
 * during the creation and execution of a bankwire PaIn
 */
@Getter
@Setter
@ToString(callSuper = true)
public class BankwirePayInExecutionContext extends PayInExecutionContext<BankwirePayInCommand> {

    protected BankwirePayInExecutionContext(BankwirePayInCommand command) {
        super(command);
    }

    public static BankwirePayInExecutionContext of(BankwirePayInCommand command) {
        return new BankwirePayInExecutionContext(command);
    }

    /**
     * Bank account of the payment provider
     */
    private BankAccountDto bankAccount;

    /**
     * Transaction wire reference as set by the payment provider
     */
    private String wireReference;

    /**
     * Bankwire transfer statement descriptor
     */
    private String statementDescriptor;

}
