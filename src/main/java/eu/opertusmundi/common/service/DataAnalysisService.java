package eu.opertusmundi.common.service;

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

}
