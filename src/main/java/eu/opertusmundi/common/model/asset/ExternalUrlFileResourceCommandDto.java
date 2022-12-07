package eu.opertusmundi.common.model.asset;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.UUID;

import javax.validation.constraints.NotBlank;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExternalUrlFileResourceCommandDto extends ResourceCommandDto implements Serializable {

    @Builder
    public ExternalUrlFileResourceCommandDto(
        UUID publisherKey, UUID ownerKey, UUID draftKey,
        Long size, String path, String crs, String encoding, String fileName, String format, String url
    ) {
        super(publisherKey, ownerKey, draftKey);
        this.size     = size;
        this.path     = path;
        this.crs      = crs;
        this.encoding = encoding;
        this.fileName = fileName;
        this.format   = format;
        this.url      = url;
    }

    private static final long serialVersionUID = 1L;

    @JsonIgnore
    private Long size;

    /**
     * Absolute path to download temporary file
     */
    @JsonIgnore
    private String path;

    @Schema(description = "Geometry data CRS", example = "EPSG:4326")
    private String crs;

    @Schema(description = "File encoding", example = "UTF-8")
    private String encoding;

    @Schema(
        description = "File name",
        required = true
    )
    @NotBlank
    private String fileName;

    @Schema(description = "File format")
    @NotBlank
    private String format;

    @Schema(description = "A URL to an external resource. Only secure URLs are allowed", required = true)
    private String url;

    public ExternalUrlResourceDto toResource() {
        final var r = new ExternalUrlResourceDto();

        r.setCrs(crs);
        r.setEncoding(encoding);
        r.setFileName(fileName);
        r.setFormat(format);
        r.setId(UUID.randomUUID().toString());
        r.setModifiedOn(ZonedDateTime.now());
        r.setParentId(null);
        r.setSource(EnumResourceSource.NONE);
        r.setType(EnumResourceType.EXTERNAL_URL);
        r.setUrl(url);

        return r;
    }

    /**
     * Initialize the filename from the URL
     *
     * <p>
     * The extension of the filename in the URL must not be empty.
     */
    public void setFileNameFromUrl() {
        if (StringUtils.isBlank(fileName) && !StringUtils.isBlank(url)) {
            final var fileName  = FilenameUtils.getName(url);
            final var extension = FilenameUtils.getExtension(url);
            if (!StringUtils.isBlank(extension)) {
                this.fileName = fileName;
            }
        }
    }
}
