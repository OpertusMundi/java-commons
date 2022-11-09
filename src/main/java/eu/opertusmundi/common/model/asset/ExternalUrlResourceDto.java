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
public class ExternalUrlResourceDto extends ResourceDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonCreator
    public ExternalUrlResourceDto(
        @JsonProperty("id") String id,
        @JsonProperty("parentId") String parentId,
        @JsonProperty("category") EnumAssetType category,
        @JsonProperty("crs") String crs,
        @JsonProperty("encoding") String encoding,
        @JsonProperty("fileName") String fileName,
        @JsonProperty("format") String format,
        @JsonProperty("modifiedOn") ZonedDateTime modifiedOn,
        @JsonProperty("source") EnumResourceSource source,
        @JsonProperty("url") String url
    ) {
        super(id, parentId, EnumResourceType.EXTERNAL_URL);

        this.category   = category;
        this.crs        = crs;
        this.encoding   = encoding;
        this.fileName   = fileName;
        this.format     = format;
        this.modifiedOn = modifiedOn;
        this.source     = source;
        this.url        = url;
    }

    public ExternalUrlResourceDto(CatalogueResource r) {
        this.category   = r.getCategory();
        this.crs        = r.getCrs() != null && !r.getCrs().isEmpty() ? r.getCrs().get(0) : null;
        this.encoding   = r.getEncoding();
        this.format     = r.getFormat();
        this.id         = r.getId();
        this.modifiedOn = r.getModifiedOn();
        this.parentId   = r.getParentId();
        this.source     = EnumResourceSource.NONE;
        this.type       = r.getType();
    }

    @Schema(description = "Asset category computed from the file format")
    private EnumAssetType category;

    @Schema(description = "Geometry data CRS")
    private String crs;

    @Schema(description = "File encoding")
    private String encoding;

    @Schema(description = "File name")
    private String fileName;

    @Schema(description = "File format")
    private String format;

    @Schema(description = "Date of last update")
    private ZonedDateTime modifiedOn;

    @Schema(description = "Resource source")
    private EnumResourceSource source;

    @Schema(description = "The resource URL")
    private String url;

    @Override
    public void patch(ResourceDto r) {
        Assert.isTrue(r.getType() == EnumResourceType.EXTERNAL_URL);

        final ExternalUrlResourceDto resource = (ExternalUrlResourceDto) r;
        // Id, type and URL are immutable
        this.category   = resource.category;
        this.crs        = resource.crs;
        this.encoding   = resource.encoding;
        this.fileName   = resource.fileName;
        this.format     = resource.format;
        this.modifiedOn = resource.modifiedOn;
        this.source     = resource.source;
    }

    @Override
    public CatalogueResource toCatalogueResource() {
        final CatalogueResource r = CatalogueResource.builder()
            .category(category)
            .encoding(encoding)
            .endpoint(url)
            .fileName(fileName)
            .format(format)
            .id(id)
            .modifiedOn(modifiedOn)
            .parentId(parentId)
            .type(EnumResourceType.FILE)
            .build();

        if (!StringUtils.isBlank(crs)) {
            r.setCrs(Arrays.asList(crs));
        }

        return r;
    }
}