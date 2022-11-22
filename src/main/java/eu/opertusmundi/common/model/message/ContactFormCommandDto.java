package eu.opertusmundi.common.model.message;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ContactFormCommandDto {

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
