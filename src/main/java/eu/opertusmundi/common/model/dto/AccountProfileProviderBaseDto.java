package eu.opertusmundi.common.model.dto;

import java.io.Serializable;

import javax.validation.constraints.Email;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccountProfileProviderBaseDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private String additionalInfo;

    private String bankAccountCurrency;
    private String bankAccountHolderName;
    private String bankAccountIban;
    private String bankAccountSwift;

    private String company;
    private String companyType;
    private String country;
    private String countryPhoneCode;

    @Schema(description = "Public email address. When browsing catalogue items, only verified email addresses are returned")
    @Email
    private String email;

    @Schema(description = "Company image")
    private byte[] logoImage;

    @Schema(description = "Company image mime type", example = "image/png")
    private String logoImageMimeType;

    private String phone;
    private String siteUrl;
    private String vat;

}
