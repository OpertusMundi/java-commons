package eu.opertusmundi.common.model.openapi.schema;

import eu.opertusmundi.common.model.asset.AssetFileAdditionalResourceDto;
import eu.opertusmundi.common.model.asset.AssetUriAdditionalResourceDto;
import io.swagger.v3.oas.annotations.media.Schema;

public class AssetEndpointTypes {

    @Schema(
            oneOf = {
            AssetFileAdditionalResourceDto.class,
            AssetUriAdditionalResourceDto.class,
        }
    )
    public static class AssetAdditionalResource {

    }

    
}
