package eu.opertusmundi.common.model.catalogue.client;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public final class CatalogueItemProviderCommandDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonIgnore
    private UUID providerKey;

    @JsonIgnore
    private UUID draftKey;

    @Schema(description = "Resource key")
    @NotNull
    private UUID resourceKey;

    @ArraySchema(
        arraySchema = @Schema(
            description = "Controls automated metadata property visibility. Selected properties are hidden."
        ),
        minItems = 0,
        uniqueItems = true
    )
    @NotNull
    private List<String> visibility = new ArrayList<>();

}
