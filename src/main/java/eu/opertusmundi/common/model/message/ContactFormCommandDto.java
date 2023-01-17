package eu.opertusmundi.common.model.message;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;

import eu.opertusmundi.common.validation.IsoCountryCode;
import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ContactFormCommandDto {

    @NotNull
    @Schema(description = "Contact form type")
    private EnumContactFormType type;

    @Length(max = 64)
    @Schema(maxLength = 64)
    private String companyName;

    @Length(max = 64)
    @Schema(maxLength = 64)
    private String firstName;

    @Length(max = 64)
    @Schema(maxLength = 64)
    private String lastName;

    @NotEmpty
    @Length(max = 120)
    @Schema(maxLength = 120)
    private String email;

    @Schema(
        description = "Country code",
        externalDocs = @ExternalDocumentation(url = "https://en.wikipedia.org/wiki/ISO_3166-1_alpha-2")
    )
    @IsoCountryCode
    private String countryCode;

    @Length(max = 4)
    @Schema(maxLength = 4)
    private String phoneCountryCode;

    @Length(max = 14)
    @Schema(maxLength = 14)
    private String phoneNumber;

    @NotEmpty
    private String message;

    @NotNull
    private boolean privacyTermsAccepted;

}
