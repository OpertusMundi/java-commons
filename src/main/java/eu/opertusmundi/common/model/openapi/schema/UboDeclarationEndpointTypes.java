package eu.opertusmundi.common.model.openapi.schema;

import eu.opertusmundi.common.model.PageResultDto;
import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.kyc.UboDeclarationDto;
import eu.opertusmundi.common.model.kyc.UboDto;
import io.swagger.v3.oas.annotations.media.Schema;

public class UboDeclarationEndpointTypes {

    @Schema(description = "UBO declaration data page response")
    public static class UboDeclarationListResponse extends RestResponse<PageResultDto<UboDeclarationDto>> {

    }

    @Schema(description = "UBO declaration response")
    public static class UboDeclarationResponse extends RestResponse<UboDeclarationDto> {

    }

    @Schema(description = "UBO response")
    public static class UboResponse extends RestResponse<UboDto> {

    }

}
