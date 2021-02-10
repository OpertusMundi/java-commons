package eu.opertusmundi.common.model.asset;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import eu.opertusmundi.common.model.catalogue.server.CatalogueResource;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

//@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class AssetResourceDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "File unique identifier")
    private final UUID id;

    @Schema(description = "File size")
    private Long size;

    @Schema(description = "File name")
    private final String fileName;

    @Schema(description = "Date of upload")
    private ZonedDateTime createdOn;

    @Schema(description = "File format")
    private String format;

    @JsonCreator
    public AssetResourceDto(
        @JsonProperty("id") UUID id, 
        @JsonProperty("fileName") String fileName, 
        @JsonProperty("size") long size,
        @JsonProperty("createdOn") ZonedDateTime createdOn, 
        @JsonProperty("format") String format
    ) {
        this.id        = id;
        this.fileName  = fileName;
        this.size      = size;
        this.createdOn = createdOn;
        this.format    = format;
    }

    public CatalogueResource toCatalogueResource() {
        return new CatalogueResource(id.toString(), "", fileName, format);
    }

    public void patch(AssetResourceDto r) {
        // Id and file name are immutable
        this.size      = r.size;
        this.createdOn = r.createdOn;
        this.format    = r.format;
    }

}