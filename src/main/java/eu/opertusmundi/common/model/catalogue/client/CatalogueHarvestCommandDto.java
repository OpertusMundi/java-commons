package eu.opertusmundi.common.model.catalogue.client;

import javax.validation.constraints.NotBlank;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class CatalogueHarvestCommandDto {

    @Schema(description = "Catalogue type", defaultValue = "CSW")
    private EnumCatalogueType type = EnumCatalogueType.CSW;

    @Schema(description = "Catalogue URL")
    @NotBlank
    private String url;

}
