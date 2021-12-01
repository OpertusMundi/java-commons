package eu.opertusmundi.common.service.contract;

import java.util.Optional;
import java.util.UUID;

import eu.opertusmundi.common.model.ApplicationException;
import eu.opertusmundi.common.model.EnumSortingOrder;
import eu.opertusmundi.common.model.PageResultDto;
import eu.opertusmundi.common.model.contract.provider.ProviderContractCommand;
import eu.opertusmundi.common.model.contract.provider.EnumProviderContractSortField;
import eu.opertusmundi.common.model.contract.provider.ProviderTemplateContractCommandDto;
import eu.opertusmundi.common.model.contract.provider.ProviderTemplateContractDto;
import eu.opertusmundi.common.model.contract.provider.ProviderTemplateContractQuery;

/**
 * Provider template contracts
 */
public interface ProviderTemplateContractService {

    /**
     * Search drafts
     *
     * @param providerKey
     * @param page
     * @param size
     * @param orderBy
     * @param order
     * @return
     */
    PageResultDto<ProviderTemplateContractDto> findAllDrafts(
        UUID providerKey,
        int page,
        int size,
        EnumProviderContractSortField orderBy,
        EnumSortingOrder order
    );

    /**
     * Find draft by key
     *
     * @param providerKey
     * @param draftKey
     * @return
     * @throws ApplicationException
     */
    ProviderTemplateContractDto findOneDraft(UUID providerKey, UUID draftKey) throws ApplicationException;

    /**
     * Create or update draft contract template
     *
     * @param command
     */
	ProviderTemplateContractDto updateDraft(ProviderTemplateContractCommandDto draftDto);

	/**
     * Delete draft contract template
     *
     * @param providerId
     * @param draftKey
     * @return
     */
    ProviderTemplateContractDto deleteDraft(Integer providerId, UUID draftKey);

    /**
     * Publish draft
     *
     * @param providerId
     * @param draftKey
     * @return
     * @throws ApplicationException
     */
    ProviderTemplateContractDto publishDraft(Integer providerId, UUID draftKey) throws ApplicationException;

    /**
     * Get all active contract templates
     *
     * @param query
     * @return
     */
    PageResultDto<ProviderTemplateContractDto> findAll(ProviderTemplateContractQuery query);

    /**
     * Get a contract by its unique key
     *
     * @param key
     * @return
     */
    Optional<ProviderTemplateContractDto> findOneByKey(Integer providerId, UUID templateKey);

    /**
     * Mark a contract as inactive
     *
     * After a template becomes inactive, providers can not assign it to an
     * asset.
     *
     * @param providerId
     * @param templateKey
     * @return
     */
    ProviderTemplateContractDto deactivate(Integer providerId, UUID templateKey);

    /**
     * Create a new draft from an existing template contract
     *
     * @param userId
     * @param providerKey
     * @param templateKey
     * @return
     */
    ProviderTemplateContractDto createFromMasterContract(int userId, UUID providerKey, UUID templateKey) throws ApplicationException;

    /**
     * Print contract
     *
     * @param command
     * @return
     */
    byte[] print(ProviderContractCommand command);

}
