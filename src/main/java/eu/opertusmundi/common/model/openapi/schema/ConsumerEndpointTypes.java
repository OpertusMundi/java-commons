package eu.opertusmundi.common.model.openapi.schema;

import eu.opertusmundi.common.model.PageResultDto;
import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.account.AccountAssetDto;
import io.swagger.v3.oas.annotations.media.Schema;

public class ConsumerEndpointTypes {

    @Schema(description = "Asset collection response")
    public static class AssetCollectionResponse extends RestResponse<PageResultDto<AccountAssetDto>> {

    }

}
