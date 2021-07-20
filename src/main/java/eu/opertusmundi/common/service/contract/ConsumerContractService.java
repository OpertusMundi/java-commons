package eu.opertusmundi.common.service.contract;

import eu.opertusmundi.common.model.contract.consumer.ConsumerContractCommandDto;
import eu.opertusmundi.common.model.contract.consumer.ConsumerContractDto;
import eu.opertusmundi.common.model.contract.consumer.PrintConsumerContractCommand;
import eu.opertusmundi.common.model.contract.consumer.SignConsumerContractCommand;

/**
 * Consumer contracts
 */
public interface ConsumerContractService {

    /**
     * Create a new contract
     *
     * @param command
     * @return
     */
    ConsumerContractDto createContract(ConsumerContractCommandDto command);

    /**
     * Print contract
     *
     * @param command
     * @return
     */
    ConsumerContractDto print(PrintConsumerContractCommand command);

    /**
     * Sign contract
     *
     * @param command
     * @return
     */
    ConsumerContractDto sign(SignConsumerContractCommand command);

}
