package eu.opertusmundi.common.service.contract;

import eu.opertusmundi.common.model.contract.ContractServiceException;
import eu.opertusmundi.common.model.contract.consumer.ConsumerContractCommand;

/**
 * Consumer contracts
 */
public interface ConsumerContractService {

    /**
     * Print contract
     *
     * @param command
     * @throws ContractServiceException
     */
    void print(ConsumerContractCommand command) throws ContractServiceException;

    /**
     * Sign contract
     *
     * @param command
     * @throws ContractServiceException
     */
    void sign(ConsumerContractCommand command) throws ContractServiceException;

}
