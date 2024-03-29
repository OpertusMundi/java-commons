package eu.opertusmundi.common.model.openapi.schema;

import java.util.List;

import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.sinergise.CatalogueResponseDto;
import eu.opertusmundi.common.model.sinergise.SubscriptionPlanDto;
import eu.opertusmundi.common.model.sinergise.client.SentinelHubOpenDataCollection;
import io.swagger.v3.oas.annotations.media.Schema;

public class SentinelHubEndpointTypes {

    @Schema(description = "Catalogue API query result")
    public static class SentinelHubCatalogueResponse extends RestResponse<CatalogueResponseDto> {

    }

    @Schema(description = "Available subscription plans")
    public static class SentinelHubSubscriptionResponse extends RestResponse<List<SubscriptionPlanDto>> {

    }

    @Schema(description = "Supported open data collections")
    public static class SentinelHubOpenDataCollectionResponse extends RestResponse<List<SentinelHubOpenDataCollection>> {

    }

}
