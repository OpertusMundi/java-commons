package eu.opertusmundi.common.model.pid;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AssetRegistrationDto {

    @JsonProperty("local_id")
    private String localId;

    @JsonProperty("owner_id")
    private Integer ownerId;

    @JsonProperty("asset_type")
    private String assetType;

    private String description;

    private Integer id;

    @JsonProperty("topio_id")
    private String topioId;

}
