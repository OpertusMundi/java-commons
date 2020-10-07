package eu.opertusmundi.common.model.dto;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class AccountProfileDto extends AccountProfileBaseDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @ArraySchema(
        arraySchema = @Schema(
            description = "User addreses"
        ),
        minItems = 0
    )
    private List<AddressDto> addresses;

    @Schema(description = "Profile creation date")
    private ZonedDateTime createdOn;

    @Schema(description = "True if public email is verified")
    private boolean emailVerified;

    @Schema(description = "When the public email has been verified")
    private ZonedDateTime emailVerifiedAt;

    @Schema(description = "User first name")
    private String firstName;

    @Schema(description = "User last name")
    private String lastName;

    @Schema(description = "Profile most recent update date")
    private ZonedDateTime modifiedOn;

    @Schema(description = "Date of provider (publisher) registration")
    private ZonedDateTime providerVerifiedAt;

    @Schema(description = "Provider rating. If there are no ratings, null is returned.")
    private Double rating;

    @Schema(description = "True if user has accepted the service terms of use")
    private boolean termsAccepted;

    @Schema(description = "When user has accepted the service terms of use")
    private ZonedDateTime termsAcceptedAt;

    @JsonIgnore
    public String getFullName() {
        if (!StringUtils.isBlank(this.firstName)) {
            if (!StringUtils.isBlank(this.lastName)) {
                return this.firstName + " " + this.lastName;
            }
            return this.firstName;
        }
        return "";
    }

}
