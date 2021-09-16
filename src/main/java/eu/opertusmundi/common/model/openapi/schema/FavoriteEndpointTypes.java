package eu.opertusmundi.common.model.openapi.schema;

import eu.opertusmundi.common.model.PageResultDto;
import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.favorite.FavoriteDto;

public class FavoriteEndpointTypes {

    public static class FavoriteCollectionResponse extends RestResponse<PageResultDto<FavoriteDto>> {

    }

}
