package eu.opertusmundi.common.model.payment;

import java.io.Serializable;

import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PayInAddressDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private String firstName;

    private String lastName;

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

}
