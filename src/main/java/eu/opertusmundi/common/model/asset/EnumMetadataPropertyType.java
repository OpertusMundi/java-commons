package eu.opertusmundi.common.model.asset;

import org.springframework.http.MediaType;

import lombok.Getter;

public enum EnumMetadataPropertyType {

    PNG(MediaType.IMAGE_PNG_VALUE, "png"),
    JSON(MediaType.APPLICATION_JSON_VALUE, "json"),
    ;

    @Getter
    private final String extension;

    @Getter
    private final String mediaType;

    private EnumMetadataPropertyType(String mediaType, String extension) {
        this.mediaType = mediaType;
        this.extension = extension;
    }

}
