package eu.opertusmundi.common.model.openapi.schema;

import eu.opertusmundi.common.model.PageResultDto;
import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.kyc.KycDocumentDto;
import io.swagger.v3.oas.annotations.media.Schema;

public class KycDocumentEndpointTypes {

    @Schema(description = "KYC document data page response")
    public static class KycDocumentListResponse extends RestResponse<PageResultDto<KycDocumentDto>> {

    }

    @Schema(description = "KYC document response")
    public static class KycDocumentResponse extends RestResponse<KycDocumentDto> {

    }

}
