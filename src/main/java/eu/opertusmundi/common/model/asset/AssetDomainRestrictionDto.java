package eu.opertusmundi.common.model.asset;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AssetDomainRestrictionDto {

    @JsonIgnore
    private Integer id;

    @Schema(description = "Domain description")
    private String name;

    @JsonIgnore
    private boolean active;

}
