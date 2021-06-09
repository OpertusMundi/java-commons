package eu.opertusmundi.common.model.openapi.schema;

import java.util.Map;

import eu.opertusmundi.common.model.PageResultDto;
import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.asset.AssetDraftDto;
import eu.opertusmundi.common.model.catalogue.client.CatalogueClientCollectionResponse;
import eu.opertusmundi.common.model.catalogue.client.CatalogueItemDetailsDto;
import eu.opertusmundi.common.model.catalogue.client.CatalogueItemDto;
import io.swagger.v3.oas.annotations.media.Schema;

public class CatalogueEndpointTypes {

    @Schema(description = "Asset collection response")
    public static class ItemCollectionResponse extends CatalogueClientCollectionResponse<CatalogueItemDto> {

    }

    @Schema(description = "Draft collection response")
    public static class DraftCollectionResponse extends RestResponse<PageResultDto<AssetDraftDto>> {

    }

    @Schema(description = "Asset response")
    public static class ItemResponse extends RestResponse<CatalogueItemDetailsDto> {

    }

    @Schema(description = "Draft response")
    public static class DraftItemResponse extends RestResponse<AssetDraftDto> {

    }

    @Schema(description = "Harvest import response. The result contains a map with all imported "
                        + "drafts. The key of the map is the initial harvested item unique identifier")
    public static class HarvestImportResponse extends RestResponse<Map<String, AssetDraftDto>> {

    }

}
