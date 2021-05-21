package eu.opertusmundi.common.model.openapi.schema;

import java.math.BigDecimal;

import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.analytics.DataSeries;

public class AnalyticsEndpointTypes {

    public static class LongDataSeries extends RestResponse<DataSeries<Long>> {

    }

    public static class BigDecimalDataSeries extends RestResponse<DataSeries<BigDecimal>> {

    }

}
