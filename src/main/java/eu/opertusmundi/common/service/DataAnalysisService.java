package eu.opertusmundi.common.service;

import java.util.List;

import org.apache.commons.lang3.tuple.ImmutablePair;

import eu.opertusmundi.common.model.analytics.AssetCountQuery;
import eu.opertusmundi.common.model.analytics.AssetTotalValueQuery;
import eu.opertusmundi.common.model.analytics.AssetTypeEarningsQuery;
import eu.opertusmundi.common.model.analytics.AssetViewQuery;
import eu.opertusmundi.common.model.analytics.CoverageQuery;
import eu.opertusmundi.common.model.analytics.DataSeries;
import eu.opertusmundi.common.model.analytics.SalesQuery;
import eu.opertusmundi.common.model.analytics.SubscribersQuery;
import eu.opertusmundi.common.model.analytics.VendorCountQuery;

public interface DataAnalysisService {

    /**
     * Executes a query on sales data and returns a data series
     *
     * @param query
     * @return
     */
    DataSeries<?> execute(SalesQuery query);
    
    /**
     * Executes a query on sales and returns a data series per asset type
     *
     * @param query
     * @return
     */
	DataSeries<?> execute(AssetTypeEarningsQuery query);
    
    /**
     * Executes a query on subscribers data and returns a data series
     *
     * @param query
     * @return
     */
    DataSeries<?> execute(SubscribersQuery query);

    /**
     * Executes a query on asset view data and returns a data series
     *
     * @param query
     * @return
     */
    DataSeries<?> execute(AssetViewQuery query);

    /**
     * Executes a query on coverage data and returns a data series
     *
     * @param query
     * @return
     */
    DataSeries<?> executeCoverage(CoverageQuery query);

    /**
     * Executes a query on asset pricing models and returns the total value of file assets
     *
     * @param query
     * @return
     */
    DataSeries<?> executeTotalAssetValue(AssetTotalValueQuery query);
    
    /**
     * Executes a query and returns the count of assets
     *
     * @param query
     * @return
     */
	DataSeries<?> executeAssetCount(AssetCountQuery query);

    /**
     * Find popular asset views/searches
     *
     * @param
     * @return
     */
    List<ImmutablePair<String, Integer>> executePopularAssetViewsAndSearches(AssetViewQuery query);

    /**
     * Find popular terms
     *
     * @param
     * @return
     */
    List<ImmutablePair<String, Integer>> executePopularTerms();
    
    /**
     * Executes a query and returns the count of active vendors
     *
     * @param query
     * @return
     */
	DataSeries<?> executeVendorCount(VendorCountQuery query);

}
