package eu.opertusmundi.common.model.openapi.schema;

import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.sinergise.CatalogueResponseDto;
import eu.opertusmundi.common.model.sinergise.SubscriptionPlanDto;
import io.swagger.v3.oas.annotations.media.Schema;

public class SentinelHubEndpointTypes {

    @Schema(description = "Catalogue API query result")
    public static class SentinelHubCatalogueResponse extends RestResponse<CatalogueResponseDto> {

    }

    @Schema(description = "Available subscription plans")
    public static class SentinelHubSubscriptionResponse extends RestResponse<SubscriptionPlanDto> {

    }

}
