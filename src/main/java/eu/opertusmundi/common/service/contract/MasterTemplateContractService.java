package eu.opertusmundi.common.service.contract;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import eu.opertusmundi.common.model.ApplicationException;
import eu.opertusmundi.common.model.EnumSortingOrder;
import eu.opertusmundi.common.model.PageResultDto;
import eu.opertusmundi.common.model.contract.helpdesk.EnumMasterContractSortField;
import eu.opertusmundi.common.model.contract.helpdesk.MasterContractCommandDto;
import eu.opertusmundi.common.model.contract.helpdesk.MasterContractDto;
import eu.opertusmundi.common.model.contract.helpdesk.MasterContractHistoryDto;
import eu.opertusmundi.common.model.contract.helpdesk.MasterContractQueryDto;

/**
 * OpertusMundi Master Template Contracts (MTC)
 */
public interface MasterTemplateContractService {

    /**
     * Find all contract
     *
     * @param query
     * @return
     */
    PageResultDto<MasterContractHistoryDto> findAllHistory(MasterContractQueryDto query);

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
     * Get a contract by its key
     *
     * @param id
     * @return
     */
    Optional<MasterContractDto> findOneByKey(UUID key);

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

}
