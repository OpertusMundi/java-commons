package eu.opertusmundi.common.model.account;

import java.io.Serializable;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.opertusmundi.common.model.EnumAccountType;
import eu.opertusmundi.common.model.EnumAuthProvider;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class PlatformAccountCommandDto implements Serializable {

    @Builder
    public PlatformAccountCommandDto(
        boolean active, boolean blocked, String email, EnumAuthProvider idpName, AccountProfileCommandDto profile, String password
    ) {
        this.active   = active;
        this.blocked  = blocked;
        this.email    = email;
        this.idpName  = idpName;
        this.password = password;
        this.profile  = profile;
        this.type     = EnumAccountType.OPERTUSMUNDI;
    }

    private static final long serialVersionUID = 1L;

    @JsonIgnore
    private boolean blocked;

    @JsonIgnore
    private boolean active;

    @JsonIgnore
    private EnumAccountType type;

    @Schema(description = "User email. Must be unique", required = true)
    @NotEmpty
    @Email
    private String email;

    @JsonIgnore
    private EnumAuthProvider idpName;

    @Schema(description = "Account profile", required = true)
    @NotNull
    @Valid
    private AccountProfileCommandDto profile;

    @JsonIgnore
    private String password;

}
