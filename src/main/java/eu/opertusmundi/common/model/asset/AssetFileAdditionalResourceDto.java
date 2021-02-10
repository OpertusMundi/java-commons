package eu.opertusmundi.common.model.asset;

import java.time.ZonedDateTime;
import java.util.UUID;

import eu.opertusmundi.common.model.catalogue.server.CatalogueAdditionalResource;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

public class AssetFileAdditionalResourceDto extends AssetAdditionalResourceDto {

    private static final long serialVersionUID = 1L;

    public AssetFileAdditionalResourceDto() {
        super(EnumAssetAdditionalResource.FILE);
    }

    public AssetFileAdditionalResourceDto(UUID id, String fileName, Long size, String description, ZonedDateTime modifiedOn) {
        this();

        this.id          = id;
        this.fileName    = fileName;
        this.size        = size;
        this.description = description;
        this.modifiedOn  = modifiedOn;
    }

    @Schema(description = "Additional resouce file unique identifier")
    @Getter
    private UUID id;

    @Schema(description = "The description of the file. If not set, the file name is used as text")
    @Getter
    private String description;

    @Schema(description = "The file name")
    @Getter
    private String fileName;

    @Schema(description = "File size")
    @Getter
    private Long size;

    @Schema(description = "Date of last update")
    @Getter
    private ZonedDateTime modifiedOn;

    public CatalogueAdditionalResource toCatalogueResource() {
        return new CatalogueAdditionalResource(id.toString(), this.description, type.toString(), this.fileName);
    }

    public void patch(AssetFileAdditionalResourceDto r) {
        // Id and file name are immutable
        this.description = r.description;
        this.size        = r.size;
        this.modifiedOn  = r.modifiedOn;
    }

}
