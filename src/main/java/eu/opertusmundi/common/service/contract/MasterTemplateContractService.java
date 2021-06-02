package eu.opertusmundi.common.service.contract;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import eu.opertusmundi.common.model.contract.MasterContractQueryDto;
import eu.opertusmundi.common.model.contract.MasterTemplateContractCommandDto;
import eu.opertusmundi.common.model.contract.MasterTemplateContractDto;
import eu.opertusmundi.common.model.contract.PrintContractCommandDto;

/**
 * OpertusMundi Master Template Contracts (MTC)
 */
public interface MasterTemplateContractService {

    /**
     * Get all contracts
     *
     * @param query
     * @return
     */
    List<MasterTemplateContractDto> findAll(MasterContractQueryDto query);

    /**
     * Get a contract by its identifier
     *
     * @param id
     * @return
     */
    Optional<MasterTemplateContractDto> findOneById(int id);

    /**
     * Get a contract by its unique key
     *
     * @param key
     * @return
     */
    Optional<MasterTemplateContractDto> findOneByKey(UUID key);

    /**
     * Update or create a contract
     *
     * @param command
     */
    void update(MasterTemplateContractCommandDto command);

    /**
     * Mark a contract as inactive
     *
     * After a contract becomes inactive, providers can not use it for creating
     * new templates.
     *
     * @param id
     */
    void deactivateBy(int id);

    /**
     * Print contract
     *
     * @param command
     * @return
     */
    byte[] print(PrintContractCommandDto command);

}
