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
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class AccountCommandDto extends AccountBaseDto implements Serializable {

    @Builder
    public AccountCommandDto(
        boolean active, boolean blocked, @NotEmpty String email, EnumAuthProvider idpName, @NotEmpty String password,
        @NotNull @Valid AccountProfileCommandDto profile, Set<EnumRole> roles, @NotEmpty String verifyPassword
    ) {
        super(active, blocked);

        this.email          = email;
        this.idpName        = idpName;
        this.password       = password;
        this.profile        = profile;
        this.roles          = roles;
        this.verifyPassword = verifyPassword;
    }

    private static final long serialVersionUID = 1L;

    @Schema(description = "User email. Must be unique", required = true)
    @NotEmpty
    private String email;

    @JsonIgnore
    private EnumAuthProvider idpName;

    @NotEmpty
    @Schema(description = "Account password", example = "s3cr3t", required = true)
    private String password;

    @Schema(description = "Account profile", required = true)
    @NotNull
    @Valid
    private AccountProfileCommandDto profile;

    @JsonIgnore
    protected Set<EnumRole> roles;

    @NotEmpty
    @Schema(description = "Account password verification. Must match property password.", example = "s3cr3t", required = true)
    private String verifyPassword;

}
