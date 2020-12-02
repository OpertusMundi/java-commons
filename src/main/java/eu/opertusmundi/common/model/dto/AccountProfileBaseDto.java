package eu.opertusmundi.common.model.dto;

import javax.validation.constraints.Pattern;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccountProfileBaseDto {

    @Schema(description = "User image")
    protected byte[] image;

    @Schema(description = "User image mime type", example = "image/png")
    protected String imageMimeType;

    @Schema(description = "User locale",  defaultValue = "en")
    @Pattern(regexp = "[a-z][a-z]")
    protected String locale;

    @Schema(description = "User phone")
    protected String phone;

}
