package eu.opertusmundi.common.model.account;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import eu.opertusmundi.common.model.EnumAccountType;
import eu.opertusmundi.common.model.EnumAuthProvider;
import eu.opertusmundi.common.model.EnumRole;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class AccountDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "True if the account is active")
    private boolean active;

    @JsonIgnore
    private boolean blocked;

    @JsonIgnore
    private EnumAccountActiveTask activeTask;

    @Schema(hidden = true, description = "Account type")
    @JsonInclude(Include.NON_NULL)
    private EnumAccountType type;

    @Schema(hidden = true, description = "Identifier of the workflow definition used for processing the account registration")
    @JsonInclude(Include.NON_NULL)
    private String processDefinition;

    @Schema(hidden = true, description = "Identifier of the workflow instance processing the account registration")
    @JsonInclude(Include.NON_NULL)
    private String processInstance;

    @Schema(description = "Date of account activation. Activation occurs when the user email is verified")
    private ZonedDateTime activatedAt;

    @Schema(description = "Activation status")
    private EnumActivationStatus activationStatus;

    @Schema(description = "User email. Must be unique")
    private String email;

    @Schema(description = "True if the email address is verified")
    private boolean emailVerified;

    @Schema(description = "Date of email verification")
    private ZonedDateTime emailVerifiedAt;

    @JsonIgnore
    private Integer id;

    @Schema(description = "IDP name used for account registration", example = "Google")
    private EnumAuthProvider idpName;

    @Schema(description = "User name as retrieved by the IDP user info endpoint")
    private String idpUserName;

    @Schema(description = "User image URL as retrieved by the IDP user info endpoint")
    private String idpUserImage;

    @Schema(description = "User unique key")
    private UUID key;

    @JsonIgnore
    private Integer parentId;

    @JsonIgnore
    private UUID parentKey;

    @JsonIgnore
    private String password;

    @Schema(description = "User profile")
    private AccountProfileDto profile;

    @Schema(description = "Date of registration")
    private ZonedDateTime registeredAt;

    @ArraySchema(
        arraySchema = @Schema(
            description = "User roles. Every user has at least role ROLE_USER."
        ),
        minItems = 1
    )
    private Set<EnumRole> roles;

    @Schema(description = "User name (always equal to user email)")
    public String getUsername() {
        return this.email;
    }

    @Schema(hidden = true, description = "Parent vendor account")
    @JsonInclude(Include.NON_NULL)
    private AccountDto parent;

    public boolean hasRole(EnumRole role) {
        if (this.roles == null) {
            return false;
        }
        return this.roles.contains(role);
    }

    @JsonIgnore
    public String getCountry() {
        final CustomerDto customer = Optional.ofNullable(this.profile).map(p -> p.getConsumer()).map(c -> c.getCurrent()).orElse(null);
        if (customer == null) {
            return null;
        }

        switch (customer.getType()) {
            case INDIVIDUAL :
                return ((CustomerIndividualDto) customer).getCountryOfResidence();

            case PROFESSIONAL :
                final String c1 = ((CustomerProfessionalDto) customer).getHeadquartersAddress().getCountry();
                final String c2 = ((CustomerProfessionalDto) customer).getRepresentative().getCountryOfResidence();

                return StringUtils.isBlank(c1) ? c2 : c1;

            default :
                return null;
        }
    }

}
