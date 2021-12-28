package eu.opertusmundi.common.model.kyc;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.UUID;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mangopay.core.enumerations.CountryIso;
import com.mangopay.entities.Ubo;

import eu.opertusmundi.common.model.account.AddressCommandDto;
import eu.opertusmundi.common.model.account.BirthplaceCommandDto;
import eu.opertusmundi.common.model.account.EnumCustomerType;
import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class UboCommandDto {

    @JsonIgnore
    private UUID customerKey;

    @JsonIgnore
    private String uboDeclarationId;

    @JsonIgnore
    private String uboId;

    @JsonIgnore
    private boolean active = true;

    @NotNull
    @Schema(description = "The customer type", required = true)
    private EnumCustomerType customerType;

    @NotBlank
    @Schema(description = "The name of the UBO", required = true)
    private String firstName;

    @NotBlank
    @Schema(description = "The last name of the UBO", required = true)
    private String lastName;

    @Valid
    @NotNull
    @Schema(description = "The address", required = true)
    private AddressCommandDto address;

    @NotBlank
    @Schema(
        description = "The UBO's nationality. ISO 3166-1 alpha-2 format is expected",
        required = true,
        externalDocs = @ExternalDocumentation(url = "https://en.wikipedia.org/wiki/ISO_3166-1_alpha-2")
    )
    private String nationality;

    @NotNull
    @Schema(description = "The date of birth of the UBO", required = true)
    protected ZonedDateTime birthdate;

    @Valid
    @NotNull
    @Schema(description = "The UBO's birthplace", required = true)
    private BirthplaceCommandDto birthplace;

    public Ubo toMangoPayUbo() {
        final Ubo u = new Ubo();

        u.setActive(active);
        if (address != null) {
            u.setAddress(address.toMangoPayAddress());
        }
        if (birthdate != null) {
            u.setBirthday(birthdate.withZoneSameInstant(ZoneOffset.UTC).toEpochSecond());
        }
        if (birthplace != null) {
            u.setBirthplace(birthplace.toMangoPayBirthplace());
        }
        u.setFirstName(firstName);
        u.setId(uboId);
        u.setLastName(lastName);
        u.setNationality(this.stringToCountryIso(nationality));

        return u;
    }

    private CountryIso stringToCountryIso(String value) {
        for (final CountryIso v : CountryIso.values()) {
            if (v.name().equalsIgnoreCase(value)) {
                return v;
            }
        }
        return null;
    }
}
