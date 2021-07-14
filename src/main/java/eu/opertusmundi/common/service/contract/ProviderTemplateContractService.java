package eu.opertusmundi.common.service.contract;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import eu.opertusmundi.common.model.contract.PrintConsumerContractCommand;
import eu.opertusmundi.common.model.contract.ProviderTemplateContractCommandDto;
import eu.opertusmundi.common.model.contract.ProviderTemplateContractDraftDto;
import eu.opertusmundi.common.model.contract.ProviderTemplateContractDto;
import eu.opertusmundi.common.model.contract.ProviderTemplateContractQuery;

/**
 * Provider template contracts
 */
public interface ProviderTemplateContractService {

	/**
     * Create a new draft contract
     *
     * @param contract dto
     * @return
     */
	ProviderTemplateContractDraftDto createDraft(ProviderTemplateContractDraftDto providerDraftDto);
	
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
     * Update a draft contract
     *
     * @param command
     */
    void updateDraft(ProviderTemplateContractDraftDto draftDto);

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

    /**
     * Update a contract's state (DRAFT-PUBLISHED)
     * @param id, state
     * @return
     */
    void updateState(int id, String state);

    /**
     * Deletes a contract
     * @param id
     * @return
     */
	void delete(int id);

	/**
     * Deletes a draft contract
     * @param id
     * @return
     */
	void deleteDraft(int id);

}
