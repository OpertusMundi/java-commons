package eu.opertusmundi.common.model.openapi.schema;

import eu.opertusmundi.common.model.RestResponse;
import io.swagger.v3.oas.annotations.media.Schema;

public class WiGeoGisEndpointTypes {


    @Schema(description = "Login response")
    public static class LoginResponse extends RestResponse<String> {

    }
}
