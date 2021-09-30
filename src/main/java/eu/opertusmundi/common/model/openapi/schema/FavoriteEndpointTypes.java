package eu.opertusmundi.common.model.openapi.schema;

import eu.opertusmundi.common.model.PageResultDto;
import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.favorite.FavoriteDto;
import io.swagger.v3.oas.annotations.media.Schema;

public class FavoriteEndpointTypes {

    @Schema(description = "Favorite collection response")
    public static class FavoriteCollectionResponse extends RestResponse<PageResultDto<FavoriteDto>> {

    }

    @Schema(description = "Favorite item response")
    public static class FavoriteItemResponse extends RestResponse<FavoriteDto> {

    }

}
