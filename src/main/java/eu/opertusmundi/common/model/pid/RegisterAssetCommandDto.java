package eu.opertusmundi.common.model.pid;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class RegisterAssetCommandDto {

    @NotEmpty
    @JsonProperty("local_id")
    private String localId;

    @NotNull
    @JsonProperty("owner_id")
    private Integer ownerId;

    @NotEmpty
    @JsonProperty("asset_type")
    private String assetType;

    @NotEmpty
    private String description;

}
