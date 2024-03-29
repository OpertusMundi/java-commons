package eu.opertusmundi.common.model.payment;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Helper class that holds information generated by the platform and MANGOPAY
 * during the creation and execution of a card direct PayIn
 */
@Getter
@Setter
@ToString(callSuper = true)
public class CardDirectPayInExecutionContext extends PayInExecutionContext<CardDirectPayInCommand> {

    protected CardDirectPayInExecutionContext(CardDirectPayInCommand command) {
        super(command);
    }

    public static CardDirectPayInExecutionContext of(CardDirectPayInCommand command) {
        return new CardDirectPayInExecutionContext(command);
    }

    /**
     * Card alias from MANGOPAY
     */
    private String cardAlias;

    /**
     * Requests 3DS version
     */
    private String requested3dsVersion;

    /**
     * Applied 3DS version by MANGOPAY
     */
    private String applied3dsVersion;

    /**
     * {@code true} if this is a recurring payment
     */
    private boolean recurring = false;

    /**
     * The MANGOPAY recurring registration identifier
     */
    private String recurringPayinRegistrationId;

    /**
     * Recurring payment type
     */
    private EnumRecurringPaymentType recurringTransactionType;

    /**
     * Frequency at which the recurring payments will be made
     */
    private EnumRecurringPaymentFrequency recurringPaymentFrequency;

    /**
     * Card payment statement descriptor
     */
    private String statementDescriptor;
}
