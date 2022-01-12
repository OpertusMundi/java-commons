package eu.opertusmundi.common.model.payment;

import java.io.Serializable;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.mangopay.core.Address;
import com.mangopay.core.Billing;
import com.mangopay.core.Shipping;

import eu.opertusmundi.common.util.MangopayUtils;
import eu.opertusmundi.common.validation.IsoCountryCode;
import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PayInAddressCommandDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull
    @Size(min = 0, max = 100)
    private String firstName;

    @NotNull
    @Size(min = 0, max = 100)
    private String lastName;

    @NotNull
    @Size(min = 0, max = 255)
    private String line1;

    @Size(min = 0, max = 255)
    private String line2;

    @NotNull
    @Size(min = 0, max = 255)
    private String city;

    @Size(min = 0, max = 255)
    private String region;

    @NotNull
    @Size(min = 1, max = 50)
    private String postalCode;

    @Schema(
        description = "The Country of the Address",
        externalDocs = @ExternalDocumentation(url = "https://en.wikipedia.org/wiki/ISO_3166-1_alpha-2")
    )
    @NotBlank
    @IsoCountryCode
    private String country;

    private Address toMangoPayAddress() {
        final Address a = new Address();

        a.setAddressLine1(this.line1);
        a.setAddressLine2(this.line2);
        a.setCity(this.city);
        a.setCountry(MangopayUtils.countryFromString(this.country));
        a.setPostalCode(this.postalCode);
        a.setRegion(this.region);

        return a;
    }

    public Billing toMangoPayBilling() {
        final Billing b = new Billing();

        b.setAddress(this.toMangoPayAddress());
        b.setFirstName(firstName);
        b.setLastName(lastName);

        return b;
    }

    public Shipping toMangoPayShipping() {
        final Shipping s = new Shipping();

        s.setAddress(this.toMangoPayAddress());
        s.setFirstName(firstName);
        s.setLastName(lastName);

        return s;
    }

}
