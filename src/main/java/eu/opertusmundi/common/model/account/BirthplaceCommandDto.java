package eu.opertusmundi.common.model.account;

import javax.validation.constraints.NotBlank;

import com.mangopay.entities.Birthplace;

import eu.opertusmundi.common.util.MangopayUtils;
import eu.opertusmundi.common.validation.IsoCountryCode;
import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BirthplaceCommandDto {

    @Schema(description = "The city of the address", required = true)
    @NotBlank
    private String city;

    @Schema(
        description = "The country of the Address",
        required = true,
        externalDocs = @ExternalDocumentation(url = "https://en.wikipedia.org/wiki/ISO_3166-1_alpha-2")
    )
    @NotBlank
    @IsoCountryCode
    private String country;

    public Birthplace toMangoPayBirthplace() {
        final Birthplace b = new Birthplace();
        b.setCity(city);
        b.setCountry(MangopayUtils.countryFromString(country));
        return b;
    }

}
