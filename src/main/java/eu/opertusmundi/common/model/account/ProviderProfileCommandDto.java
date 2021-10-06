package eu.opertusmundi.common.model.account;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public final class ProviderProfileCommandDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonIgnore
    private Integer userId;

    @Schema(description = "Additional customer information")
    private String additionalInfo;

    @Schema(description = "Company type description")
    private String companyType;

    @Schema(description = "Company log image using Base64 encoding")
    private byte[] logoImage;

    @Schema(description = "Company log image MIME type")
    private String logoImageMimeType;

    @Schema(description = "Provider contract phone")
    private String phone;

    @Schema(description = "Provider site URL")
    private String siteUrl;

}
