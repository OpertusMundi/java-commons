package eu.opertusmundi.common.service.contract;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import eu.opertusmundi.common.model.contract.PrintConsumerContractCommand;
import eu.opertusmundi.common.model.contract.ProviderTemplateContractCommandDto;
import eu.opertusmundi.common.model.contract.ProviderTemplateContractDto;
import eu.opertusmundi.common.model.contract.ProviderTemplateContractQuery;

/**
 * Provider template contracts
 */
public interface ProviderTemplateContractService {

    /**
     * Get all contracts
     *
     * @param query
     * @return
     */
    List<ProviderTemplateContractDto> findAll(ProviderTemplateContractQuery query);

    /**
     * Find all asset identifiers who are assigned a contract based on the
     * specified template.
     *
     * @param id
     * @return A list of asset PIDs
     */
    List<String> findAllAssignedAssets(int id);

    /**
     * Find contract template assigned to the specific asset
     *
     * @param id
     * @return
     */
    Optional<ProviderTemplateContractDto> findOneByAsset(String id);

    /**
     * Get a contract by its identifier
     *
     * @param id
     * @return
     */
    Optional<ProviderTemplateContractDto> findOneById(int id);

    /**
     * Get a contract by its unique key
     *
     * @param key
     * @return
     */
    Optional<ProviderTemplateContractDto> findOneByKey(UUID key);

    /**
     * Update or create a contract
     *
     * @param command
     */
    void update(ProviderTemplateContractCommandDto command);

    /**
     * Mark a contract as inactive
     *
     * After a template becomes inactive, providers can not assign it to an
     * asset.
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
    byte[] print(PrintConsumerContractCommand command);

}
