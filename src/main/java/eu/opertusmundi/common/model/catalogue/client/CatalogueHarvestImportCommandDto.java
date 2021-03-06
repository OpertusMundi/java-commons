package eu.opertusmundi.common.model.catalogue.client;

import java.util.UUID;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class CatalogueHarvestImportCommandDto {

    @JsonIgnore
    private UUID publisherKey;
    
    @Schema(description = "Catalogue URL")
    @NotBlank
    private String url;

    @ArraySchema(
        arraySchema = @Schema(
            description = "Unique identifiers of harvested catalogue items to import."
        ),
        minItems = 1,
        uniqueItems = true
    )
    @NotEmpty
    private String[] ids;

}
