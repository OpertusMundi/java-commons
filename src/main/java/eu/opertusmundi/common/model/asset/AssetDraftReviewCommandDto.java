package eu.opertusmundi.common.model.asset;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AssetDraftReviewCommandDto {

    @JsonIgnore
    private UUID publisherKey;

    @JsonIgnore
    private UUID assetKey;

    private boolean rejected;

    private String reason;

}
