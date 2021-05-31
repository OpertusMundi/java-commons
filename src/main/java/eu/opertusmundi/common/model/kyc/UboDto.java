package eu.opertusmundi.common.model.kyc;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mangopay.entities.Ubo;

import eu.opertusmundi.common.model.account.AddressDto;
import eu.opertusmundi.common.model.account.BirthplaceDto;
import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UboDto {

    @Schema(description = "UBO's unique identifier")
    private String id;
    
    @Schema(description = "The name of the UBO")
    private String firstName;

    @Schema(description = "The last name of the UBO")
    private String lastName;

    @Schema(description = "The address")
    private AddressDto address;

    @Schema(
        description = "The UBO's nationality in ISO 3166-1 alpha-2 format", 
        externalDocs = @ExternalDocumentation(url = "https://en.wikipedia.org/wiki/ISO_3166-1_alpha-2")
    )
    private String nationality;

    @Schema(description = "The date of birth of the UBO")
    protected ZonedDateTime birthdate;

    @Schema(description = "The UBO's birthplace")
    private BirthplaceDto birthplace;

    @JsonIgnore
    private String tag;

    public static UboDto from(Ubo u) {
        final UboDto result = new UboDto();

        result.setAddress(AddressDto.from(u.getAddress()));
        result.setBirthdate(ZonedDateTime.ofInstant(Instant.ofEpochSecond(u.getBirthday()), ZoneOffset.UTC));
        result.setBirthplace(BirthplaceDto.from(u.getBirthplace()));
        result.setFirstName(u.getFirstName());
        result.setId(u.getId());
        result.setLastName(u.getLastName());
        result.setNationality(u.getNationality().toString());
        result.setTag(u.getTag());

        return result;
    }

}
