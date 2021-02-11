package eu.opertusmundi.common.model.asset;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import eu.opertusmundi.common.model.catalogue.server.CatalogueResource;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
public class AssetResourceDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "File unique identifier")
    private final UUID id;

    @Schema(description = "File size")
    private Long size;

    @Schema(description = "File name")
    private final String fileName;

    @Schema(description = "Date of last update")
    private ZonedDateTime modifiedOn;

    @Schema(description = "File format")
    private String format;

    @JsonCreator
    public AssetResourceDto(
        @JsonProperty("id") UUID id, 
        @JsonProperty("fileName") String fileName, 
        @JsonProperty("size") long size,
        @JsonProperty("modifiedOn") ZonedDateTime modifiedOn, 
        @JsonProperty("format") String format
    ) {
        this.id         = id;
        this.fileName   = fileName;
        this.size       = size;
        this.modifiedOn = modifiedOn;
        this.format     = format;
    }

    public CatalogueResource toCatalogueResource() {
        return new CatalogueResource(id.toString(), "", fileName, format);
    }

    public void patch(AssetResourceDto r) {
        // Id and file name are immutable
        this.size       = r.size;
        this.modifiedOn = r.modifiedOn;
        this.format     = r.format;
    }

}