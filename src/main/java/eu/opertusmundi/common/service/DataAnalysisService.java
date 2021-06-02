package eu.opertusmundi.common.service;

import eu.opertusmundi.common.model.analytics.AssetViewQuery;
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

}
