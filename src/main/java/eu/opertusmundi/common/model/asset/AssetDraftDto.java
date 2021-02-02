package eu.opertusmundi.common.model.asset;

import java.util.List;

import eu.opertusmundi.common.model.dto.PublisherDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AssetDraftDto extends BaseAssetDraftDto {

    private PublisherDto publisher;

    @Schema(description = "List of resources associated to the asset. Resources are returned only if a single result is found")
    private List<AssetResourceDto> resources;

}
