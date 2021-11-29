package eu.opertusmundi.common.service.integration;

import eu.opertusmundi.common.model.sinergise.CatalogueResponseDto;
import eu.opertusmundi.common.model.sinergise.client.ClientCatalogueQueryDto;
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
     * Query catalogue for free assets
     *
     * @param query
     * @return
     * @throws SentinelHubException
     */
    CatalogueResponseDto search(ClientCatalogueQueryDto query) throws SentinelHubException;

}
