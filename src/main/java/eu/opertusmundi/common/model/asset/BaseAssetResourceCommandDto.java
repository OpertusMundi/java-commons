package eu.opertusmundi.common.model.asset;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BaseAssetResourceCommandDto {

    /**
     * Publisher key is set by the calling service
     */
    @JsonIgnore
    private UUID publisherKey;

    /**
     * Draft key is set by the calling service
     */
    @JsonIgnore
    private UUID draftKey;

    /**
     * File size is set at the server
     */
    @JsonIgnore
    private Long size;

}
