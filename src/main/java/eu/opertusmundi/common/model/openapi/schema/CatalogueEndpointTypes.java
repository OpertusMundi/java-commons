package eu.opertusmundi.common.model.openapi.schema;

import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.asset.AssetDraftDto;
import eu.opertusmundi.common.model.catalogue.client.CatalogueClientCollectionResponse;
import eu.opertusmundi.common.model.catalogue.client.CatalogueItemDetailsDto;
import eu.opertusmundi.common.model.catalogue.client.CatalogueItemDraftDto;
import eu.opertusmundi.common.model.catalogue.client.CatalogueItemDto;
import io.swagger.v3.oas.annotations.media.Schema;

public class CatalogueEndpointTypes {

    @Schema(description = "Asset collection response")
    public static class ItemCollectionResponse extends CatalogueClientCollectionResponse<CatalogueItemDto> {

    }

    @Schema(description = "Draft collection response")
    public static class DraftCollectionResponse extends CatalogueClientCollectionResponse<CatalogueItemDraftDto> {

    }

    @Schema(description = "Asset response")
    public class ItemResponse extends RestResponse<CatalogueItemDetailsDto> {

    }

    @Schema(description = "Draft response")
    public class DraftItemResponse extends RestResponse<AssetDraftDto> {

    }

}
