package eu.opertusmundi.common.model.rating;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(description = "Asset rating creation command")
@NoArgsConstructor
public class AssetRatingCommandDto extends BaseRatingCommandDto {

    @JsonIgnore
    @Getter
    @Setter
    private String asset;

}
