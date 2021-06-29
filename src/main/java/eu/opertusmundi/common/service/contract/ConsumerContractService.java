package eu.opertusmundi.common.service.contract;

import java.util.Optional;

import eu.opertusmundi.common.model.contract.ConsumerContractCommandDto;
import eu.opertusmundi.common.model.contract.ConsumerContractDto;
import eu.opertusmundi.common.model.contract.PrintConsumerContractCommand;
import eu.opertusmundi.common.model.contract.SignConsumerContractCommand;

/**
 * Consumer contracts
 */
public interface ConsumerContractService {

    /**
     * Find contract for an asset (file or subscription) linked to the user
     * account
     *
     * @param id
     * @return
     */
    Optional<ConsumerContractDto> findAssetContract(String id);

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
