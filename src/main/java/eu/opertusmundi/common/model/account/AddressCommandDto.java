package eu.opertusmundi.common.model.account;

import java.io.Serializable;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

import com.mangopay.core.Address;
import com.mangopay.core.enumerations.CountryIso;

import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddressCommandDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank
    @Size(min = 1, max = 255)
    private String line1;

    @Size(min = 0, max = 255)
    private String line2;

    @NotBlank
    @Size(min = 1, max = 255)
    private String city;

    @NotEmpty
    @Size(min = 1, max = 255)
    private String region;

    @NotBlank
    @Size(min = 1, max = 50)
    private String postalCode;

    @Schema(
        description = "The Country of the Address",
        externalDocs = @ExternalDocumentation(url = "https://en.wikipedia.org/wiki/ISO_3166-1_alpha-2")
    )
    @NotBlank
    private String country;

    public Address toMangoPayAddress() {
        final Address a = new Address();

        a.setAddressLine1(this.getLine1());
        a.setAddressLine2(this.getLine2());
        a.setCity(this.getCity());
        a.setCountry(this.stringToCountryIso(this.getCountry()));
        a.setPostalCode(this.getPostalCode());
        a.setRegion(this.getRegion());

        return a;
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
