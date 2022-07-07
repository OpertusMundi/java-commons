package eu.opertusmundi.common.model.openapi.schema;

import eu.opertusmundi.common.model.PageResultDto;
import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.asset.service.UserServiceDto;
import io.swagger.v3.oas.annotations.media.Schema;

public class UserServiceEndpointTypes {

    @Schema(description = "Service collection response")
    public static class ServiceCollectionResponse extends RestResponse<PageResultDto<UserServiceDto>> {
    }

    @Schema(description = "Service response")
    public static class ServiceResponse extends RestResponse<UserServiceDto> {
    }

    @Schema(description = "Service resource metadata")
    public static class JsonNodeMetadata extends Object {
    }

}
