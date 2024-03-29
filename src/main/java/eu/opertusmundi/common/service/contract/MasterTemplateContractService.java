package eu.opertusmundi.common.service.contract;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import eu.opertusmundi.common.model.ApplicationException;
import eu.opertusmundi.common.model.EnumSortingOrder;
import eu.opertusmundi.common.model.PageResultDto;
import eu.opertusmundi.common.model.contract.helpdesk.EnumMasterContractSortField;
import eu.opertusmundi.common.model.contract.helpdesk.MasterContractCommandDto;
import eu.opertusmundi.common.model.contract.helpdesk.MasterContractDto;
import eu.opertusmundi.common.model.contract.helpdesk.MasterContractHistoryDto;
import eu.opertusmundi.common.model.contract.helpdesk.MasterContractHistoryResult;
import eu.opertusmundi.common.model.contract.helpdesk.MasterContractQueryDto;
import eu.opertusmundi.common.model.message.client.ClientContactDto;

/**
 * OpertusMundi Master Template Contracts (MTC)
 */
public interface MasterTemplateContractService {

    /**
     * Find contact information for all providers
     *
     * @param email
     * @return
     */
    List<ClientContactDto> findProviders(String email);

    /**
     * Find all contracts
     *
     * @param query
     * @return
     */
    MasterContractHistoryResult findAllHistory(MasterContractQueryDto query);

    /**
     * Get all contracts
     *
     * @param query
     * @return
     */
    PageResultDto<MasterContractDto> findAll(MasterContractQueryDto query);

    /**
     * Get a contract by its identifier
     *
     * @param id
     * @return
     */
    Optional<MasterContractDto> findOneById(int id);

    /**
     * Get a contract by its key and provider key
     *
     * @param providerKey
     * @param contractKey
     * @return
     */
    Optional<MasterContractDto> findOneByKey(UUID providerKey, UUID contractKey);

    /**
     * Create a new draft from an existing master contract
     *
     * @param userId
     * @param templateId
     * @return
     */
    MasterContractDto createForTemplate(int userId, int templateId) throws ApplicationException;

    /**
     * Create a new cloned draft from an existing master contract with new version history
     *
     * @param userId
     * @param templateId
     * @return
     */
    MasterContractDto cloneFromTemplate(int userId, int templateId) throws ApplicationException;

    /**
     * Disables a master contract
     *
     * @param id
     * @return
     * @throws ApplicationException
     */
    MasterContractHistoryDto deactivate(int id) throws ApplicationException;

    /**
     * Search drafts
     * @param page
     * @param size
     * @param orderBy
     * @param order
     * @return
     */
    PageResultDto<MasterContractDto> findAllDrafts(
        int page,
        int size,
        EnumMasterContractSortField orderBy,
        EnumSortingOrder order
    );

    /**
     * Find draft
     * @param id
     * @return
     */
    MasterContractDto findOneDraft(int id) throws ApplicationException;

    /**
     * Create/Update draft
     *
     * @param command
     * @return
     */
    MasterContractDto updateDraft(MasterContractCommandDto command) throws ApplicationException;

    /**
     * Delete draft
     *
     * @param id
     */
    void deleteDraft(int id) throws ApplicationException;

    /**
     * Publish draft
     *
     * @param id
     * @return
     */
    MasterContractDto publishDraft(int id) throws ApplicationException;

    /**
     * Print contract
     *
     * @param command
     * @return
     */
    byte[] print(int masterContractId) throws IOException;

    /**
     * Set history contract as the default template.
     *
     * <p>
     * A contract must be <code>ACTIVE</code> to become the default contract.
     *
     * @param id
     * @return
     * @throws ApplicationException
     */
    MasterContractDto setDefaultContract(int id) throws ApplicationException;

}
