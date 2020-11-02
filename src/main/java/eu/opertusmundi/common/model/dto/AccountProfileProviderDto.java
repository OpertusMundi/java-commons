package eu.opertusmundi.common.model.dto;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class AccountProfileProviderDto extends AccountProfileProviderBaseDto {

    private static final long serialVersionUID = 1L;

    @Schema(description = "Key for the contract generated during the provider registration")
    UUID contract;

    @Schema(description = "Most recent provider update date")
    private ZonedDateTime modifiedOn;

    @Schema(description = "True if public email is verified")
    private boolean emailVerified;

    @Schema(description = "When the public email has been verified")
    private ZonedDateTime emailVerifiedAt;

    @Schema(description = "Provider rating. If there are no ratings, null is returned.")
    private Double rating;

    @Schema(description = "Provider registration date")
    private ZonedDateTime registeredOn;

    @Schema(description = "True if user has accepted the service terms of use")
    private boolean termsAccepted;

    @Schema(description = "When user has accepted the service terms of use")
    private ZonedDateTime termsAcceptedAt;

    @ArraySchema(
        arraySchema = @Schema(
            description = "Registration files"
        ),
        minItems = 0
    )
    private List<AccountProfileProviderFileDto> files = new ArrayList<>();

}
