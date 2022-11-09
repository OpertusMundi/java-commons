package eu.opertusmundi.common.model.openapi.schema;

import java.math.BigDecimal;
import java.util.List;

import org.apache.commons.lang3.tuple.ImmutablePair;

import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.analytics.AssetViewCounterDto;
import eu.opertusmundi.common.model.analytics.DataSeries;

public class AnalyticsEndpointTypes {

    public static class LongDataSeries extends RestResponse<DataSeries<Long>> {

    }

    public static class BigDecimalDataSeries extends RestResponse<DataSeries<BigDecimal>> {

    }

    public static class ListOfImmutablePairs extends RestResponse<List<ImmutablePair<String, Integer>>> {

    }

    public static class ListOfAssetViewCounters extends RestResponse<List<AssetViewCounterDto>> {

    }

}
