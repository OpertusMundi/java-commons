package eu.opertusmundi.common.model.dto;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import eu.opertusmundi.common.model.EnumProviderRegistrationStatus;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Setter
@Getter
public class AccountProfileProviderDraftDto extends AccountProfileProviderBaseDto {

    private static final long serialVersionUID = 1L;

    @Schema(description = "Registration requests status")
    private EnumProviderRegistrationStatus status;

    @Schema(description = "Key for the contract generated during the provider registration")
    UUID contract;

    @Schema(description = "Provider update request creation date")
    private ZonedDateTime createdOn;

    @Schema(description = "Provider update request last modified date")
    private ZonedDateTime modifiedOn;

    @ArraySchema(
        arraySchema = @Schema(
            description = "Registration files"
        ),
        minItems = 0
    )
    @Setter(AccessLevel.PRIVATE)
    private List<ProviderRegistrationFileDto> files = new ArrayList<>();

}
