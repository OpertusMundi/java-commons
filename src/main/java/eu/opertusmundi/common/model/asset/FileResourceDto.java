package eu.opertusmundi.common.model.asset;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.UUID;

import org.locationtech.jts.util.Assert;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
public class FileResourceDto extends ResourceDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "File size")
    private Long size;

    @Schema(description = "Asset category computed from the file format")
    private EnumAssetSourceType category;
    
    @Schema(description = "File name")
    private final String fileName;

    @Schema(description = "Date of last update")
    private ZonedDateTime modifiedOn;

    @Schema(description = "File format")
    private String format;

    @JsonCreator
    public FileResourceDto(
        @JsonProperty("id") UUID id, 
        @JsonProperty("fileName") String fileName, 
        @JsonProperty("size") long size,
        @JsonProperty("modifiedOn") ZonedDateTime modifiedOn,
        @JsonProperty("category") EnumAssetSourceType category,
        @JsonProperty("format") String format
    ) {
        this.id         = id;
        this.type       = EnumResourceType.FILE;
        this.fileName   = fileName;
        this.size       = size;
        this.modifiedOn = modifiedOn;
        this.category   = category;
        this.format     = format;
    }

    public void patch(ResourceDto r) {
        Assert.isTrue(r.getType() == EnumResourceType.FILE);

        final FileResourceDto resource = (FileResourceDto) r;
        // Id, type and file name are immutable
        this.size       = resource.size;
        this.modifiedOn = resource.modifiedOn;
        this.category   = resource.category;
        this.format     = resource.format;
    }

}