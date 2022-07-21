package eu.opertusmundi.common.service.analytics;


import eu.opertusmundi.common.model.analytics.DataSeries;
import eu.opertusmundi.common.model.analytics.GoogleAnalyticsQuery;

public interface GoogleAnalyticsService {
	
    /**
     * Executes a Google Analytics query and returns either number of visitors or number of sessions
     *
     * @param query
     * @return
     */
	public DataSeries<?> execute(GoogleAnalyticsQuery query);

}
