package eu.opertusmundi.common.service;

import eu.opertusmundi.common.model.analytics.AssetStatisticsDto;

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
    AssetStatisticsDto updateStatisticsPublishAsset(String pid);

    /**
     * Remove records from asset_statistics
     *
     * @param pid
     */
    void updateStatisticsUnpublishAsset(String pid);
}
