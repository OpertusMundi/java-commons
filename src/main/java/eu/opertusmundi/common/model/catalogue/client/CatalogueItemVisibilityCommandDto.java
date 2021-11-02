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
public final class CatalogueItemVisibilityCommandDto implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * The publisher key
     *
     * If this is a vendor account (with role `ROLE_VENDOR_PROVIDER`), the
     * publisher key is the unique key of the parent account. If this is a
     * provider account (with role `ROLE_PROVIDER`, this is the unique key of
     * the authenticated user.
     */
    @JsonIgnore
    private UUID publisherKey;

    /**
     * The authenticated user key
     */
    @JsonIgnore
    private UUID ownerKey;

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
