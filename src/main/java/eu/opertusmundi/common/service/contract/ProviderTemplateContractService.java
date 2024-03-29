package eu.opertusmundi.common.service.contract;

import java.util.Optional;
import java.util.UUID;

import eu.opertusmundi.common.model.ApplicationException;
import eu.opertusmundi.common.model.EnumSortingOrder;
import eu.opertusmundi.common.model.PageResultDto;
import eu.opertusmundi.common.model.contract.provider.EnumProviderContractSortField;
import eu.opertusmundi.common.model.contract.provider.ProviderContractCommand;
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
     * @param providerKey
     * @param draftKey
     * @return
     * @throws ApplicationException
     */
    ProviderTemplateContractDto publishDraft(UUID providerKey, UUID draftKey) throws ApplicationException;

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
     * @param providerKey
     * @param templateKey
     * @return
     */
    default ProviderTemplateContractDto deactivate(UUID providerKey, UUID templateKey) {
        return this.deactivate(providerKey, templateKey, false);
    }

    /**
     * Mark a contract as inactive
     *
     * After a template becomes inactive, providers can not assign it to an
     * asset.
     *
     * @param providerKey
     * @param templateKey
     * @param force
     * @return
     */
    ProviderTemplateContractDto deactivate(UUID providerKey, UUID templateKey, boolean force);

    /**
     * Create a new draft from an existing template contract
     *
     * @param providerKey
     * @param templateKey
     * @return
     */
    ProviderTemplateContractDto createFromMasterContract(UUID providerKey, UUID templateKey) throws ApplicationException;

    /**
     * Print contract
     *
     * @param command
     * @return
     */
    byte[] print(ProviderContractCommand command);

    /**
     * Update default contracts for the provider with the given key
     *
     * @param providerKey
     */
    void updateDefaultContracts(UUID providerKey);

    /**
     * Accept provider default contract
     *
     * @param providerKey
     * @param contractKey
     * @return
     */
    ProviderTemplateContractDto acceptDefaultContract(UUID providerKey, UUID contractKey);
}
