package eu.opertusmundi.common.model.asset;

import java.io.Serializable;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
public class AssetResourceDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "File size")
    private final Long size;

    @Schema(description = "File name")
    private final String name;

    @Schema(description = "Date of last update")
    private final ZonedDateTime modifiedOn;

    public AssetResourceDto(String name, long size, long modifiedOn) {
        this.name = name;
        this.size = size;

        final Instant t = Instant.ofEpochMilli(modifiedOn);
        this.modifiedOn = ZonedDateTime.ofInstant(t, ZoneOffset.UTC);
    }

}