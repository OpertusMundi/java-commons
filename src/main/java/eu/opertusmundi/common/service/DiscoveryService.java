package eu.opertusmundi.common.service;

import eu.opertusmundi.common.model.discovery.client.ClientJoinableResultDto;
import eu.opertusmundi.common.model.discovery.client.ClientRelatedResultDto;

public interface DiscoveryService {

    /**
     * Gets all assets that are joinable with the given source asset
     *
     * @param id The item unique id
     * @return
     */
    ClientJoinableResultDto findJoinable(String id);


    /**
     * Get all the assets on the path connecting the source and the target tables
     *
     * @param source The id of the asset to get the table from as source
     * @param target The id of the asset to get the table from as target
     * @return
     */
    ClientRelatedResultDto findRelated(String source, String[] target);

}
