package eu.opertusmundi.common.model.payment;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Helper class that holds information generated by the platform and MANGOPAY
 * during the creation and execution of a free PayIn
 */
@Getter
@Setter
@ToString
public class FreePayInExecutionContext {

    private FreePayInExecutionContext(FreePayInCommand command) {
        this.command = command;
    }

    public static FreePayInExecutionContext of(FreePayInCommand command) {
        return new FreePayInExecutionContext(command);
    }

    @Setter(AccessLevel.PRIVATE)
    private FreePayInCommand command;

    /**
     * Reference number generated by the payment service
     */
    private String referenceNumber;
}
