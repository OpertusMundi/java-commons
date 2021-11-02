package eu.opertusmundi.common.model.account;

import java.io.Serializable;
import java.util.UUID;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class JoinVendorCommandDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonIgnore
    private UUID token;

    @Schema(description = "User email. Must match the registered mail", required = true)
    @NotEmpty
    @Email
    private String email;

    @NotEmpty
    @Schema(description = "Account password", example = "s3cr3t", required = true)
    private String password;

    @NotEmpty
    @Schema(description = "Account password verification. Must match property password.", example = "s3cr3t", required = true)
    private String verifyPassword;

}
