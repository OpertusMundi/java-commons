package eu.opertusmundi.common.model.contract;

import eu.opertusmundi.common.model.ServiceException;

public class ContractServiceException extends ServiceException {

    private static final long serialVersionUID = 1L;

    public ContractServiceException(ContractMessageCode code) {
        super(code, "[Contract Service] Operation has failed");
    }

    public ContractServiceException(ContractMessageCode code, Throwable cause) {
        super(code, "[Contract Service] Operation has failed", cause);
    }

    public ContractServiceException(ContractMessageCode code, String message) {
        super(code, message);
    }

    public ContractServiceException(ContractMessageCode code, String message, Throwable cause) {
        super(code, message, cause);
    }

}
