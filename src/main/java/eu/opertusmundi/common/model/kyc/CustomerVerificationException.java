package eu.opertusmundi.common.model.kyc;

import eu.opertusmundi.common.model.ServiceException;

public class CustomerVerificationException extends ServiceException {

    private static final long serialVersionUID = 1L;

    public CustomerVerificationException(CustomerVerificationMessageCode code) {
        super(code, "[Customer Verification Service] Operation has failed");       
    }

    public CustomerVerificationException(CustomerVerificationMessageCode code, String message) {
        super(code, message);
    }

    public CustomerVerificationException(CustomerVerificationMessageCode code, String message, Throwable cause) {
        super(code, message, cause);
    }
    
    public CustomerVerificationException(
        CustomerVerificationMessageCode code, String message, Throwable cause, boolean logEntryRequired
    ) {
        super(code, message, cause);
        
        this.logEntryRequired = logEntryRequired;
    }

}