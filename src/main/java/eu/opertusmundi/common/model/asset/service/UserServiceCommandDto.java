package eu.opertusmundi.common.model.asset.service;

import java.io.Serializable;
import java.util.UUID;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public final class UserServiceCommandDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonIgnore
    private UUID serviceKey;

    /**
     * The authenticated user key
     */
    @JsonIgnore
    private UUID ownerKey;

    /**
     * The account parent's key
     *
     * If this is a vendor account (with role `ROLE_VENDOR_PROVIDER`), the
     * publisher key is the unique key of the parent account. If this is a
     * provider account (with role `ROLE_PROVIDER`), this is the unique key of
     * the authenticated user.
     */
    @JsonIgnore
    private UUID parentKey;

    /**
     * The authenticated user name (email)
     */
    @JsonIgnore
    private String userName;

    @JsonIgnore
    private String fileName;

    @JsonIgnore
    private Long fileSize;

    @Schema(description = "An abstract of the resource")
    @JsonProperty("abstract")
    private String abstractText;

    @Schema(description = "Geometry data CRS")
    @NotEmpty
    private String crs;

    @Schema(description = "File encoding")
    private String encoding;

    @Schema(description = "File format")
    @NotEmpty
    private String format;

    @Schema(description = "Path to user's file system")
    @NotEmpty
    private String path;

    @Schema(description = "Service type")
    @NotNull
    private EnumUserServiceType serviceType;

    @Schema(description = "A name given to the resource")
    @NotEmpty
    private String title;

    @Schema(description = "The version of the resource")
    @NotEmpty
    private String version;

}
