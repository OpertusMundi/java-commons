package eu.opertusmundi.common.model.asset;

import java.io.Serializable;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.locationtech.jts.util.Assert;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import eu.opertusmundi.common.model.catalogue.client.CatalogueItemDetailsDto;
import eu.opertusmundi.common.model.catalogue.client.EnumAssetType;
import eu.opertusmundi.common.model.catalogue.server.CatalogueResource;
import io.swagger.v3.oas.annotations.Hidden;
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
        @JsonProperty("id") String id,
        @JsonProperty("parentId") String parentId,
        @JsonProperty("category") EnumAssetType category,
        @JsonProperty("crs") String crs,
        @JsonProperty("encoding") String encoding,
        @JsonProperty("fileName") String fileName,
        @JsonProperty("format") String format,
        @JsonProperty("modifiedOn") ZonedDateTime modifiedOn,
        @JsonProperty("path") String path,
        @JsonProperty("size") Long size,
        @JsonProperty("source") EnumResourceSource source
    ) {
        super(id, parentId, EnumResourceType.FILE);

        this.category   = category;
        this.crs        = crs;
        this.format     = format;
        this.fileName   = fileName;
        this.format     = format;
        this.modifiedOn = modifiedOn;
        this.path       = path;
        this.size       = size;
        this.source     = source;
    }

    public FileResourceDto(CatalogueResource r) {
        this.category   = r.getCategory();
        this.crs        = r.getCrs() != null && !r.getCrs().isEmpty() ? r.getCrs().get(0) : null;
        this.encoding   = r.getEncoding();
        this.fileName   = r.getFileName();
        this.format     = r.getFormat();
        this.id         = r.getId();
        this.modifiedOn = r.getModifiedOn();
        this.parentId   = r.getParentId();
        this.size       = r.getSize();
        this.source     = EnumResourceSource.NONE;
        this.type       = r.getType();
    }

    @Schema(description = "File size")
    private Long size;

    @Schema(description = "Asset category computed from the file format")
    private EnumAssetType category;

    @Schema(description = "File name")
    private String fileName;

    @Schema(description = "Date of last update")
    private ZonedDateTime modifiedOn;

    @Schema(description = "File format")
    private String format;

    @Schema(description = "File encoding")
    private String encoding;

    @Schema(description = "Geometry data CRS")
    private String crs;

    @Schema(description = "Resource source")
    private EnumResourceSource source;

    @Schema(description = "The relative path of the file in the user's file system. Available only when source is `FILE_SYSTEM`")
    @JsonInclude(Include.NON_EMPTY)
    private String path;

    @Override
    public void patch(ResourceDto r) {
        Assert.isTrue(r.getType() == EnumResourceType.FILE);

        final FileResourceDto resource = (FileResourceDto) r;
        // Id, type and file name are immutable
        this.category   = resource.category;
        this.crs        = resource.crs;
        this.encoding   = resource.encoding;
        this.format     = resource.format;
        this.modifiedOn = resource.modifiedOn;
        this.path       = resource.path;
        this.size       = resource.size;
        this.source     = resource.source;
    }

    @Override
    public CatalogueResource toCatalogueResource() {
        final CatalogueResource r = CatalogueResource.builder()
            .category(category)
            .encoding(encoding)
            .fileName(fileName)
            .format(format)
            .id(id)
            .modifiedOn(modifiedOn)
            .parentId(parentId)
            .size(size)
            .type(EnumResourceType.FILE)
            .build();

        if (!StringUtils.isBlank(crs)) {
            r.setCrs(Arrays.asList(crs));
        }

        return r;
    }

    /**
     * The path of the file resource
     */
    @JsonIgnore
    @Hidden
    private Path relativePath;

    /**
     * Parent asset
     */
    @JsonIgnore
    @Hidden
    private CatalogueItemDetailsDto asset;

}