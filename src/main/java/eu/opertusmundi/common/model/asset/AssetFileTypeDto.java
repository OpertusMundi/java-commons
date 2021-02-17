package eu.opertusmundi.common.model.asset;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AssetFileTypeDto {

    @Schema(description = "Asset category")
    private EnumAssetSourceType category;

    private String format;

    @Schema(description = "Supported file extensions")
    private String[] extensions;

    @Schema(description = "True if multiple files are supported")
    private boolean bundleSupported;

    @Schema(description = "Supported file extensions included in a bundle")
    private String[] bundleExtensions;

    @Schema(description = "Format description")
    private String notes;

}
