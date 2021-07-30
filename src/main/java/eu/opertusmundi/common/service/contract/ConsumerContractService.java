package eu.opertusmundi.common.service.contract;

import eu.opertusmundi.common.model.contract.ContractServiceException;
import eu.opertusmundi.common.model.contract.consumer.ConsumerContractDto;
import eu.opertusmundi.common.model.contract.consumer.PrintConsumerContractCommand;
import eu.opertusmundi.common.model.contract.consumer.SignConsumerContractCommand;

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
    void print(PrintConsumerContractCommand command) throws ContractServiceException;

    /**
     * Sign contract
     *
     * @param command
     * @return
     * @throws ContractServiceException
     */
    ConsumerContractDto sign(SignConsumerContractCommand command) throws ContractServiceException;

}
