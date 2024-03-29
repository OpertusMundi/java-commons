package eu.opertusmundi.common.model.openapi.schema;

import eu.opertusmundi.common.model.PageResultDto;
import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.account.AccountAssetDto;
import eu.opertusmundi.common.model.account.AccountSubscriptionDto;
import eu.opertusmundi.common.model.file.CopyToDriveResultDto;
import io.swagger.v3.oas.annotations.media.Schema;

public class ConsumerEndpointTypes {

    @Schema(description = "Asset collection response")
    public static class AssetCollectionResponse extends RestResponse<PageResultDto<AccountAssetDto>> {

    }

    @Schema(description = "Subscription collection response")
    public static class SubscriptionCollectionResponse extends RestResponse<PageResultDto<AccountSubscriptionDto>> {

    }

    @Schema(description = "Subscription response")
    public static class SubscriptionResponse extends RestResponse<AccountSubscriptionDto> {

    }

    @Schema(description = "File copy operation response")
    public static class CopyToDriveResponse extends RestResponse<CopyToDriveResultDto> {

    }

}
