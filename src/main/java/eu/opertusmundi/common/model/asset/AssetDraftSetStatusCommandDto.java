package eu.opertusmundi.common.model.asset;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AssetDraftSetStatusCommandDto {

    @JsonIgnore
    private UUID publisherKey;

    @JsonIgnore
    private UUID assetKey;

    @Schema(description = "Draft new status")
    private EnumProviderAssetDraftStatus status;

}
