package eu.opertusmundi.common.model.catalogue.client;

import java.util.UUID;

import javax.validation.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class CatalogueHarvestCommandDto {

    @JsonIgnore
    private UUID userKey;

    @Schema(description = "Catalogue type", defaultValue = "CSW")
    private EnumCatalogueType type = EnumCatalogueType.CSW;

    @Schema(description = "Catalogue URL")
    @NotBlank
    private String url;

}
