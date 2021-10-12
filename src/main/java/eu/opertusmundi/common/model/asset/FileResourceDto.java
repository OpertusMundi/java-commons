package eu.opertusmundi.common.model.asset;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.locationtech.jts.util.Assert;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import eu.opertusmundi.common.model.catalogue.client.EnumAssetType;
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
        @JsonProperty("id") String id,
        @JsonProperty("parentId") String parentId,
        @JsonProperty("size") Long size,
        @JsonProperty("category") EnumAssetType category,
        @JsonProperty("fileName") String fileName,
        @JsonProperty("modifiedOn") ZonedDateTime modifiedOn,
        @JsonProperty("format") String format,
        @JsonProperty("encoding") String encoding,
        @JsonProperty("crs") String crs
    ) {
        super(id, parentId, EnumResourceType.FILE);

        this.size       = size;
        this.category   = category;
        this.fileName   = fileName;
        this.modifiedOn = modifiedOn;
        this.format     = format;
        this.encoding   = encoding;
        this.crs        = crs;
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
        this.encoding   = r.getEncoding();
        this.crs        = r.getCrs() != null && !r.getCrs().isEmpty() ? r.getCrs().get(0) : null;
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

    @Override
    public void patch(ResourceDto r) {
        Assert.isTrue(r.getType() == EnumResourceType.FILE);

        final FileResourceDto resource = (FileResourceDto) r;
        // Id, type and file name are immutable
        this.size       = resource.size;
        this.modifiedOn = resource.modifiedOn;
        this.category   = resource.category;
        this.format     = resource.format;
        this.crs        = resource.crs;
        this.encoding   = resource.encoding;
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

}