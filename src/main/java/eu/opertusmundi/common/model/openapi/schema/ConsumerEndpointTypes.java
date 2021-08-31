package eu.opertusmundi.common.model.openapi.schema;

import eu.opertusmundi.common.model.PageResultDto;
import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.account.AccountAssetDto;
import eu.opertusmundi.common.model.account.AccountSubscriptionDto;
import io.swagger.v3.oas.annotations.media.Schema;

public class ConsumerEndpointTypes {

    @Schema(description = "Asset collection response")
    public static class AssetCollectionResponse extends RestResponse<PageResultDto<AccountAssetDto>> {

    }

    @Schema(description = "Subscription collection response")
    public static class AssetSubscriptionResponse extends RestResponse<PageResultDto<AccountSubscriptionDto>> {

    }

}
