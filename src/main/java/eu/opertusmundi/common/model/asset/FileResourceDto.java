package eu.opertusmundi.common.model.asset;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.UUID;

import org.locationtech.jts.util.Assert;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import eu.opertusmundi.common.model.catalogue.server.CatalogueResource;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class FileResourceDto extends ResourceDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonCreator
    public FileResourceDto(
        @JsonProperty("id") UUID id,
        @JsonProperty("parentId") UUID parentId,
        @JsonProperty("size") Long size,
        @JsonProperty("category") EnumAssetSourceType category,
        @JsonProperty("fileName") String fileName,
        @JsonProperty("modifiedOn") ZonedDateTime modifiedOn,
        @JsonProperty("format") String format
    ) {
        super(id, parentId, EnumResourceType.FILE);

        this.size       = size;
        this.category   = category;
        this.fileName   = fileName;
        this.modifiedOn = modifiedOn;
        this.format     = format;
    }

    public FileResourceDto(CatalogueResource r) {
        this.category   = r.getCategory();
        this.fileName   = r.getFileName();
        this.format     = r.getFormat();
        this.id         = r.getId();
        this.modifiedOn = r.getModifiedOn();
        this.parentId   = r.getParentId();
        this.size       = r.getSize();
        this.type       = r.getType();
    }

    @Schema(description = "File size")
    private Long size;

    @Schema(description = "Asset category computed from the file format")
    private EnumAssetSourceType category;

    @Schema(description = "File name")
    private String fileName;

    @Schema(description = "Date of last update")
    private ZonedDateTime modifiedOn;

    @Schema(description = "File format")
    private String format;

    @Override
    public void patch(ResourceDto r) {
        Assert.isTrue(r.getType() == EnumResourceType.FILE);

        final FileResourceDto resource = (FileResourceDto) r;
        // Id, type and file name are immutable
        this.size       = resource.size;
        this.modifiedOn = resource.modifiedOn;
        this.category   = resource.category;
        this.format     = resource.format;
    }

    @Override
    public CatalogueResource toCatalogueResource() {
        return CatalogueResource.builder()
            .category(category)
            .fileName(fileName)
            .format(format)
            .id(id)
            .modifiedOn(modifiedOn)
            .parentId(parentId)
            .size(size)
            .type(EnumResourceType.FILE)
            .build();
    }

}