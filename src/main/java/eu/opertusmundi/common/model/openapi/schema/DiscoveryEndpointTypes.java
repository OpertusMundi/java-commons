package eu.opertusmundi.common.model.openapi.schema;

import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.discovery.client.ClientJoinableResultDto;
import eu.opertusmundi.common.model.discovery.client.ClientRelatedResultDto;
import io.swagger.v3.oas.annotations.media.Schema;

public class DiscoveryEndpointTypes {

    @Schema(description = "Joinable assets response")
    public static class JoinableResponse extends RestResponse<ClientJoinableResultDto> {

    }

    @Schema(description = "Related assets response")
    public static class RelatedResponse extends RestResponse<ClientRelatedResultDto> {

    }

}
