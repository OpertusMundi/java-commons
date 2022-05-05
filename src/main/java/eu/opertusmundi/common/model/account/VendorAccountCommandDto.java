package eu.opertusmundi.common.model.account;

import java.io.Serializable;
import java.util.Set;
import java.util.UUID;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.opertusmundi.common.model.EnumAccountType;
import eu.opertusmundi.common.model.EnumVendorRole;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class VendorAccountCommandDto implements Serializable {

    @Builder
    public VendorAccountCommandDto(
        String email, AccountProfileCommandDto profile, Set<EnumVendorRole> roles
    ) {
        this.email   = email;
        this.profile = profile;
        this.roles   = roles;
        this.type    = EnumAccountType.VENDOR;
    }

    private static final long serialVersionUID = 1L;

    @JsonIgnore
    private EnumAccountType type;

    @Schema(description = "User unique email. This property cannot be updated after it is set", required = true)
    @NotEmpty
    @Email
    private String email;

    @JsonIgnore
    private UUID key;

    @JsonIgnore
    private UUID parentKey;

    @Schema(description = "Account profile", required = true)
    @NotNull
    @Valid
    private AccountProfileCommandDto profile;

    @ArraySchema(
        arraySchema = @Schema(
            description = "Vendor account roles"
        ),
        minItems = 0,
        uniqueItems = true
    )
    @NotNull
    private Set<EnumVendorRole> roles;

    @JsonIgnore
    private String password;

}
