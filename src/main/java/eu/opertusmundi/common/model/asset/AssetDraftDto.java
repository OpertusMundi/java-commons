package eu.opertusmundi.common.model.asset;

import java.util.List;

import eu.opertusmundi.common.model.dto.PublisherDto;
import eu.opertusmundi.common.model.file.FileDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AssetDraftDto extends BaseAssetDraftDto {

    private PublisherDto publisher;

    @Schema(description = "List of files associated to the asset. Files are returned only if a single result is found")
    private List<FileDto> files;

}
