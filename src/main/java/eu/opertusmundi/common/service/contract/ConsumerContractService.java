package eu.opertusmundi.common.service.contract;

import java.util.Optional;

import eu.opertusmundi.common.model.contract.ConsumerContractCommandDto;
import eu.opertusmundi.common.model.contract.ConsumerContractDto;
import eu.opertusmundi.common.model.contract.PrintContractCommandDto;

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
    byte[] print(PrintContractCommandDto command);

}
