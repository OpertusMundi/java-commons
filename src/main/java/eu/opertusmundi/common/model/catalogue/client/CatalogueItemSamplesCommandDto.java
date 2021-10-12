package eu.opertusmundi.common.model.catalogue.client;

import java.io.Serializable;
import java.util.UUID;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public final class CatalogueItemSamplesCommandDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonIgnore
    private UUID providerKey;

    @JsonIgnore
    private UUID draftKey;

    @Schema(description = "Resource key")
    @NotNull
    private String resourceKey;

    @Schema(description = "Samples as a JSON object")
    @NotNull
    private JsonNode data;

}
