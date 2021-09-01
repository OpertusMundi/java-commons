package eu.opertusmundi.common.model.account;

import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;

import com.mangopay.core.Address;

import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddressDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private String line1;

    private String line2;

    private String city;

    private String region;

    private String postalCode;

    @Schema(
        description = "The Country of the Address",
        externalDocs = @ExternalDocumentation(url = "https://en.wikipedia.org/wiki/ISO_3166-1_alpha-2")
    )
    private String country;


    public static AddressDto from(Address a) {
        final AddressDto result = new AddressDto();

        result.setCity(a.getCity());
        result.setCountry(a.getCountry().toString());
        result.setLine1(a.getAddressLine1());
        result.setLine2(a.getAddressLine2());
        result.setPostalCode(a.getPostalCode());
        result.setRegion(a.getRegion());

        return result;
    }

    @Override
    public String toString() {
        final String lines = String.format("%s %s", line1, StringUtils.isBlank(line2) ? "" : line2).trim();

        return String.format("%s, %s, %s, %s", lines, postalCode, region, city).trim();
    }

}
