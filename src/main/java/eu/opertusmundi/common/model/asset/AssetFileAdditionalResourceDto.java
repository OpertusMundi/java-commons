package eu.opertusmundi.common.model.asset;

import java.time.ZonedDateTime;

import eu.opertusmundi.common.model.catalogue.server.CatalogueAdditionalResource;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

public class AssetFileAdditionalResourceDto extends AssetAdditionalResourceDto {

    private static final long serialVersionUID = 1L;

    public AssetFileAdditionalResourceDto() {
        super(EnumAssetAdditionalResource.FILE);
    }

    public AssetFileAdditionalResourceDto(String id, String fileName, Long size, String description, ZonedDateTime modifiedOn) {
        this();

        this.id          = id;
        this.fileName    = fileName;
        this.size        = size;
        this.description = description;
        this.modifiedOn  = modifiedOn;
    }

    @Schema(description = "Additional resource file unique identifier")
    @Getter
    private String id;

    @Schema(description = "The description of the file. If not set, the file name is used as text")
    @Getter
    private String description;

    @Schema(description = "The file name")
    @Getter
    private String fileName;

    @Schema(description = "File size")
    @Getter
    @Setter
    private Long size;

    @Schema(description = "Date of last update")
    @Getter
    @Setter
    private ZonedDateTime modifiedOn;

    @Override
    public CatalogueAdditionalResource toCatalogueResource() {
        return CatalogueAdditionalResource.builder()
            .id(id.toString())
            .name(description)
            .value(fileName)
            .type(type)
            .modifiedOn(modifiedOn)
            .size(size)
            .build();
    }

    public void patch(AssetFileAdditionalResourceDto r) {
        // Id and file name are immutable
        this.description = r.description;
        this.size        = r.size;
        this.modifiedOn  = r.modifiedOn;
    }

}
