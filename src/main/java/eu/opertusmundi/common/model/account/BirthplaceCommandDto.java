package eu.opertusmundi.common.model.account;

import javax.validation.constraints.NotBlank;

import com.mangopay.core.enumerations.CountryIso;
import com.mangopay.entities.Birthplace;

import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BirthplaceCommandDto {

    @NotBlank
    @Schema(description = "The city of the address", required = true)
    private String city;

    @NotBlank
    @Schema(
        description = "The country of the Address",
        required = true,
        externalDocs = @ExternalDocumentation(url = "https://en.wikipedia.org/wiki/ISO_3166-1_alpha-2")
    )
    private String country;

    public Birthplace toMangoPayBirthplace() {
        final Birthplace b = new Birthplace();;
        b.setCity(city);
        b.setCountry(this.stringToCountryIso(country));
        return b;
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
