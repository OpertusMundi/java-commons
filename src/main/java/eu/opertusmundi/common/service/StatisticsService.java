package eu.opertusmundi.common.service;

import java.util.Arrays;
import java.util.List;

import eu.opertusmundi.common.model.analytics.AssetStatisticsDto;
import eu.opertusmundi.common.model.catalogue.client.CatalogueItemDto;
import eu.opertusmundi.common.model.catalogue.client.CatalogueItemStatistics;

public interface StatisticsService {

    /**
     * Insert records into asset_statistics
     *
     * If a favorite already exists, the existing record is returned
     *
     * @param  pid
     * @return AssetStatisticsDto
     * @throws
     */
    AssetStatisticsDto updateAssetPublish(CatalogueItemDto item);

    /**
     * Remove records from asset_statistics
     *
     * @param pid
     */
    void updateAssetUnpublish(String pid);

    /**
     * Get statistics for the specified asset identifiers
     *
     * @param pids
     * @return
     */
    List<CatalogueItemStatistics> findAll(List<String> pids);

    /**
     * Get statistics for the specified asset identifier
     *
     * @param pid
     * @return
     */
    default CatalogueItemStatistics findOne(String pid) {
        final List<CatalogueItemStatistics> result = this.findAll(Arrays.asList(new String[]{pid}));
        return result.isEmpty() ? null : result.get(0);
    }

    /**
     * Increase sales counter for asset
     *
     * @param pid
     */
    void increaseSales(String pid);

}
