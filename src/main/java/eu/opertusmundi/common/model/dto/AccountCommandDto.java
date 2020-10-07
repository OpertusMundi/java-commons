package eu.opertusmundi.common.model.dto;

import java.io.Serializable;
import java.util.Set;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.opertusmundi.common.model.EnumAuthProvider;
import eu.opertusmundi.common.model.EnumRole;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class AccountCommandDto extends AccountBaseDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonIgnore
    private boolean active = true;

    @JsonIgnore
    private boolean blocked = false;

    @Schema(description = "User email. Must be unique", required = true)
    @NotEmpty
    private String email;

    @Schema(description = "User first name", required = true, deprecated = true)
    @NotEmpty
    private String firstName;

    @JsonIgnore
    private EnumAuthProvider idpName;

    @JsonIgnore
    private String idpUserAlias;

    @JsonIgnore
    private String idpUserImage;

    @Schema(description = "User last name", required = true, deprecated = true)
    @NotEmpty
    private String lastName;

    @NotEmpty
    @Schema(description = "Account password", example = "s3cr3t", required = true)
    private String password;

    @NotEmpty
    @Schema(description = "Account password verification. Must match property password.", example = "s3cr3t", required = true)
    private String verifyPassword;

    @Schema(description = "Account profile", required = true)
    @NotNull
    @Valid
    private AccountProfileCommandDto profile;

    @JsonIgnore
    protected Set<EnumRole> roles;

}
