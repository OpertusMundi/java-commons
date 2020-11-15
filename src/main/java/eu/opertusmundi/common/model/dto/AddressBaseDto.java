package eu.opertusmundi.common.model.dto;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;

import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Valid
@Getter
@Setter
public class AddressBaseDto {

    @NotEmpty
    private String line1;

    private String line2;

    @NotEmpty
    private String city;

    @NotEmpty
    private String region;

    @NotEmpty
    private String postalCode;

    @Schema(
        description = "The Country of the Address",
        externalDocs = @ExternalDocumentation(url = "https://en.wikipedia.org/wiki/ISO_3166-1_alpha-2")
    )
    @NotEmpty
    private String country;

}
