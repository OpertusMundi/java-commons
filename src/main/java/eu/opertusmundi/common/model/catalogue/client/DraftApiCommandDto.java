package eu.opertusmundi.common.model.catalogue.client;

import java.io.Serializable;
import java.util.UUID;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type"
)
@JsonSubTypes({
    @Type(name = "ASSET", value = DraftApiFromAssetCommandDto.class),
    @Type(name = "FILE", value = DraftApiFromFileCommandDto.class),
})
@Getter
@Setter
public abstract class DraftApiCommandDto implements Serializable {

    private static final long serialVersionUID = 1L;

    protected DraftApiCommandDto(EnumDraftCommandType type) {
        this.type = type;
    }

    /**
     * The authenticated user id
     */
    @JsonIgnore
    private Integer userId;

    /**
     * The publisher key
     *
     * If this is a vendor account (with role `ROLE_VENDOR_PROVIDER`), the
     * publisher key is the unique key of the parent account. If this is a
     * provider account (with role `ROLE_PROVIDER`), this is the unique key of
     * the authenticated user.
     */
    @JsonIgnore
    private UUID publisherKey;

    /**
     * The authenticated user key
     */
    @JsonIgnore
    private UUID ownerKey;

    @Schema(description = "Command type", required = true)
    @NotNull
    private EnumDraftCommandType type;

    @Schema(description = "Service type", allowableValues = {"WMS", "WFS", "DATA_API"}, required = true)
    @NotNull
    private String serviceType;

    @Schema(description = "A name given to the resource", required = true)
    @NotEmpty
    private String title;

    @Schema(description = "The version of the resource", required = true)
    @NotEmpty
    private String version;

}
