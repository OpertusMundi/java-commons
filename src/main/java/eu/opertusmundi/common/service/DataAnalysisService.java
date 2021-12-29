package eu.opertusmundi.common.service;

import eu.opertusmundi.common.model.analytics.AssetTotalValueQuery;
import eu.opertusmundi.common.model.analytics.AssetViewQuery;
import eu.opertusmundi.common.model.analytics.CoverageQuery;
import eu.opertusmundi.common.model.analytics.DataSeries;
import eu.opertusmundi.common.model.analytics.SalesQuery;

public interface DataAnalysisService {

    /**
     * Executes a query on sales data and returns a data series
     *
     * @param query
     * @return
     */
    DataSeries<?> execute(SalesQuery query);

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

}
