package eu.opertusmundi.common.service.integration;

import java.util.List;

import eu.opertusmundi.common.model.sinergise.CatalogueResponseDto;
import eu.opertusmundi.common.model.sinergise.SubscriptionPlanDto;
import eu.opertusmundi.common.model.sinergise.client.ClientCatalogueQueryDto;
import eu.opertusmundi.common.model.sinergise.client.SentinelHubOpenDataCollection;
import eu.opertusmundi.common.model.sinergise.server.AccountTypeDto;
import eu.opertusmundi.common.model.sinergise.server.ContractDto;
import eu.opertusmundi.common.model.sinergise.server.CreateContractCommandDto;
import eu.opertusmundi.common.model.sinergise.server.CreateContractResponse;
import eu.opertusmundi.common.model.sinergise.server.GroupDto;
import eu.opertusmundi.common.model.sinergise.server.SentinelHubException;

public interface SentinelHubService {

    /**
     * Request token
     *
     * @return
     * @throws SentinelHubException if operation has failed
     */
    String requestToken() throws SentinelHubException;

    /**
     * List groups
     *
     * @return
     */
    List<GroupDto> getGroups();

    /**
     * List account types
     *
     * @return
     */
    List<AccountTypeDto> getAccountTypes();

    /**
     * List the user contracts on the specified group
     *
     * @return
     */
    List<ContractDto> getContracts();

    /**
     * Check if a contract exists for the specified user name
     *
     * @param userName
     * @return
     */
    boolean contractExists(String userName);

    /**
     * Crate new contract
     *
     * @param command
     * @return
     * @throws SentinelHubException
     */
    CreateContractResponse createContract(CreateContractCommandDto command) throws SentinelHubException;

    /**
     * Query catalogue for free assets
     *
     * @param query
     * @return
     * @throws SentinelHubException
     */
    CatalogueResponseDto search(ClientCatalogueQueryDto query) throws SentinelHubException;

    /**
     * Get all available subscription plans
     *
     * @return
     */
    List<SubscriptionPlanDto> getSubscriptionPlans();

    /**
     * Get all supported open data collections
     *
     * @return
     */
    List<SentinelHubOpenDataCollection> getOpenDataCollections();

}
